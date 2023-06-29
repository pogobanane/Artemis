package de.tum.in.www1.artemis.lecture;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URISyntaxException;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.test.context.support.WithMockUser;

import com.fasterxml.jackson.core.JsonProcessingException;

import de.tum.in.www1.artemis.AbstractSpringIntegrationBambooBitbucketJiraTest;
import de.tum.in.www1.artemis.competency.CompetencyUtilService;
import de.tum.in.www1.artemis.course.CourseUtilService;
import de.tum.in.www1.artemis.domain.*;
import de.tum.in.www1.artemis.domain.competency.Competency;
import de.tum.in.www1.artemis.domain.competency.LearningPath;
import de.tum.in.www1.artemis.exercise.ExerciseUtilService;
import de.tum.in.www1.artemis.exercise.textexercise.TextExerciseUtilService;
import de.tum.in.www1.artemis.participation.ParticipationUtilService;
import de.tum.in.www1.artemis.repository.*;
import de.tum.in.www1.artemis.service.LearningPathService;
import de.tum.in.www1.artemis.user.UserUtilService;
import de.tum.in.www1.artemis.util.PageableSearchUtilService;

class LearningPathIntegrationTest extends AbstractSpringIntegrationBambooBitbucketJiraTest {

    private static final String TEST_PREFIX = "learningpathintegration";

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LearningPathService learningPathService;

    @Autowired
    private UserUtilService userUtilService;

    @Autowired
    private CourseUtilService courseUtilService;

    @Autowired
    CompetencyUtilService competencyUtilService;

    @Autowired
    PageableSearchUtilService pageableSearchUtilService;

    @Autowired
    LearningPathRepository learningPathRepository;

    @Autowired
    ExerciseUtilService exerciseUtilService;

    @Autowired
    TextExerciseUtilService textExerciseUtilService;

    @Autowired
    ParticipationUtilService participationUtilService;

    @Autowired
    LectureRepository lectureRepository;

    @Autowired
    LectureUtilService lectureUtilService;

    @Autowired
    GradingCriterionRepository gradingCriterionRepository;

    private Course course;

    private Competency[] competencies;

    private TextExercise textExercise;

    private final int NUMBER_OF_STUDENTS = 5;

    private final String STUDENT_OF_COURSE = TEST_PREFIX + "student1";

    private final String TUTOR_OF_COURSE = TEST_PREFIX + "tutor1";

    private final String EDITOR_OF_COURSE = TEST_PREFIX + "editor1";

    private final String INSTRUCTOR_OF_COURSE = TEST_PREFIX + "instructor1";

    private User studentNotInCourse;

    @BeforeEach
    void setupTestScenario() throws Exception {
        participantScoreScheduleService.activate();

        userUtilService.addUsers(TEST_PREFIX, NUMBER_OF_STUDENTS, 1, 1, 1);

        // Add users that are not in the course
        studentNotInCourse = userUtilService.createAndSaveUser(TEST_PREFIX + "student1337");
        userUtilService.createAndSaveUser(TEST_PREFIX + "instructor1337");

        course = courseUtilService.createCoursesWithExercisesAndLectures(TEST_PREFIX, true, true, 1).get(0);
        competencies = competencyUtilService.createCompetencies(course, 5);

        textExercise = textExerciseUtilService.createIndividualTextExercise(course, past(1), future(1), future(2));
        List<GradingCriterion> gradingCriteria = exerciseUtilService.addGradingInstructionsToExercise(textExercise);
        gradingCriterionRepository.saveAll(gradingCriteria);
        participationUtilService.addAssessmentWithFeedbackWithGradingInstructionsForExercise(textExercise, STUDENT_OF_COURSE);

        Lecture lecture = new Lecture();
        lecture.setDescription("Test Lecture");
        lecture.setCourse(course);
        lectureRepository.save(lecture);

        final var textUnit = lectureUtilService.createTextUnit();
        lectureUtilService.addLectureUnitsToLecture(lecture, List.of(textUnit));

    }

    private void enableLearningPathsForTestingCourse() {
        course = courseRepository.findWithEagerLearningPathsAndCompetenciesByIdElseThrow(course.getId());
        learningPathService.generateLearningPaths(course);
        course.setLeanringPathsEnabled(true);
        course = courseRepository.save(course);
    }

    private ZonedDateTime now() {
        return ZonedDateTime.now();
    }

    private ZonedDateTime past(long days) {
        return now().minusDays(days);
    }

    private ZonedDateTime future(long days) {
        return now().plusDays(days);
    }

    private void testAllPreAuthorize() throws Exception {
        request.putWithResponseBody("/api/courses/" + course.getId() + "/learning-paths/enable", null, Course.class, HttpStatus.FORBIDDEN);
        final var search = pageableSearchUtilService.configureSearch("");
        request.getSearchResult("/api/courses/" + course.getId() + "/learning-paths", HttpStatus.FORBIDDEN, LearningPath.class, pageableSearchUtilService.searchMapping(search));
    }

    private Competency createCompetencyRESTCall() throws Exception {
        final var competencyToCreate = new Competency();
        competencyToCreate.setTitle("CompetencyToCreateTitle");
        competencyToCreate.setCourse(course);
        return request.postWithResponseBody("/api/courses/" + course.getId() + "/competencies", competencyToCreate, Competency.class, HttpStatus.CREATED);
    }

    private Competency importCompetencyRESTCall() throws Exception {
        final var course2 = courseUtilService.createCourse();
        final var competencyToImport = competencyUtilService.createCompetency(course2);
        return request.postWithResponseBody("/api/courses/" + course.getId() + "/competencies/import", competencyToImport, Competency.class, HttpStatus.CREATED);
    }

    private Competency updateCompetencyRESTCall() throws Exception {
        competencies[0].setTitle("Updated Title");
        return request.putWithResponseBody("/api/courses/" + course.getId() + "/competencies", competencies[0], Competency.class, HttpStatus.OK);
    }

    private void deleteCompetencyRESTCall(Competency competency) throws Exception {
        request.delete("/api/courses/" + course.getId() + "/competencies/" + competency.getId(), HttpStatus.OK);
    }

    @Test
    @WithMockUser(username = STUDENT_OF_COURSE, roles = "USER")
    void testAll_asStudent() throws Exception {
        this.testAllPreAuthorize();
    }

    @Test
    @WithMockUser(username = TUTOR_OF_COURSE, roles = "TA")
    void testAll_asTutor() throws Exception {
        this.testAllPreAuthorize();
    }

    @Test
    @WithMockUser(username = EDITOR_OF_COURSE, roles = "EDITOR")
    void testAll_asEditor() throws Exception {
        this.testAllPreAuthorize();
    }

    @Test
    @WithMockUser(username = INSTRUCTOR_OF_COURSE, roles = "INSTRUCTOR")
    void testEnableLearningPaths() throws Exception {
        request.putWithResponseBody("/api/courses/" + course.getId() + "/learning-paths/enable", course, Course.class, HttpStatus.OK);
        final var updatedCourse = courseRepository.findWithEagerLearningPathsAndCompetenciesByIdElseThrow(course.getId());
        assertThat(updatedCourse.getLearningPathsEnabled()).as("should enable LearningPaths").isTrue();
        assertThat(updatedCourse.getLearningPaths()).isNotNull();
        assertThat(updatedCourse.getLearningPaths().size()).as("should create LearningPath for each student").isEqualTo(NUMBER_OF_STUDENTS);
        updatedCourse.getLearningPaths().forEach(
                lp -> assertThat(lp.getCompetencies().size()).as("LearningPath (id={}) should have be linked to all Competencies", lp.getId()).isEqualTo(competencies.length));
    }

    @Test
    @WithMockUser(username = INSTRUCTOR_OF_COURSE, roles = "INSTRUCTOR")
    void testEnableLearningPathsAlreadyEnabled() throws Exception {
        course.setLeanringPathsEnabled(true);
        courseRepository.save(course);
        request.putWithResponseBody("/api/courses/" + course.getId() + "/learning-paths/enable", course, Course.class, HttpStatus.BAD_REQUEST);
    }

    @Test
    @WithMockUser(username = TEST_PREFIX + "student1337", roles = "USER")
    void testGenerateLearningPathOnEnrollment() throws Exception {
        course.setEnrollmentEnabled(true);
        course.setEnrollmentStartDate(past(1));
        course.setEnrollmentEndDate(future(1));
        course = courseRepository.save(course);
        enableLearningPathsForTestingCourse();

        this.setupEnrollmentRequestMocks();

        final var updatedUser = request.postWithResponseBody("/api/courses/" + course.getId() + "/enroll", null, User.class, HttpStatus.OK);
        final var updatedUserWithLearningPaths = userRepository.findWithLearningPathsByIdElseThrow(updatedUser.getId());
        assertThat(updatedUserWithLearningPaths.getLearningPaths()).isNotNull();
        assertThat(updatedUserWithLearningPaths.getLearningPaths().size()).as("should create LearningPath for student").isEqualTo(1);
    }

    private void setupEnrollmentRequestMocks() throws JsonProcessingException, URISyntaxException {
        jiraRequestMockProvider.enableMockingOfRequests();
        jiraRequestMockProvider.mockAddUserToGroupForMultipleGroups(Set.of(course.getStudentGroupName()));
        bitbucketRequestMockProvider.enableMockingOfRequests();
        bitbucketRequestMockProvider.mockUpdateUserDetails(studentNotInCourse.getLogin(), studentNotInCourse.getEmail(), studentNotInCourse.getName());
        bitbucketRequestMockProvider.mockAddUserToGroups();
    }

    @Test
    @WithMockUser(username = INSTRUCTOR_OF_COURSE, roles = "INSTRUCTOR")
    void testGetLearningPathsOnPageForCourseLearningPathsDisabled() throws Exception {
        final var search = pageableSearchUtilService.configureSearch("");
        request.getSearchResult("/api/courses/" + course.getId() + "/learning-paths", HttpStatus.BAD_REQUEST, LearningPath.class, pageableSearchUtilService.searchMapping(search));
    }

    @Test
    @WithMockUser(username = INSTRUCTOR_OF_COURSE, roles = "INSTRUCTOR")
    void testGetLearningPathsOnPageForCourseEmpty() throws Exception {
        enableLearningPathsForTestingCourse();
        final var search = pageableSearchUtilService.configureSearch(STUDENT_OF_COURSE + "SuffixThatAllowsTheResultToBeEmpty");
        final var result = request.getSearchResult("/api/courses/" + course.getId() + "/learning-paths", HttpStatus.OK, LearningPath.class,
                pageableSearchUtilService.searchMapping(search));
        assertThat(result.getResultsOnPage()).isNullOrEmpty();
    }

    @Test
    @WithMockUser(username = INSTRUCTOR_OF_COURSE, roles = "INSTRUCTOR")
    void testGetLearningPathsOnPageForCourseExactlyStudent() throws Exception {
        enableLearningPathsForTestingCourse();
        final var search = pageableSearchUtilService.configureSearch(STUDENT_OF_COURSE);
        final var result = request.getSearchResult("/api/courses/" + course.getId() + "/learning-paths", HttpStatus.OK, LearningPath.class,
                pageableSearchUtilService.searchMapping(search));
        assertThat(result.getResultsOnPage()).hasSize(1);
    }

    @Test
    @WithMockUser(username = INSTRUCTOR_OF_COURSE, roles = "INSTRUCTOR")
    void testAddCompetencyToLearningPathsOnCreateCompetency() throws Exception {
        enableLearningPathsForTestingCourse();

        final var createdCompetency = createCompetencyRESTCall();

        final var student = userRepository.findOneByLogin(STUDENT_OF_COURSE).orElseThrow();
        final var learningPathOptional = learningPathRepository.findWithEagerCompetenciesByCourseIdAndUserId(course.getId(), student.getId());
        assertThat(learningPathOptional).isPresent();
        assertThat(learningPathOptional.get().getCompetencies()).as("should contain new competency").contains(createdCompetency);
        assertThat(learningPathOptional.get().getCompetencies().size()).as("should not remove old competencies").isEqualTo(competencies.length + 1);
        final var oldCompetencies = Set.of(competencies[0], competencies[1], competencies[2], competencies[3], competencies[4]);
        assertThat(learningPathOptional.get().getCompetencies()).as("should not remove old competencies").containsAll(oldCompetencies);
    }

    @Test
    @WithMockUser(username = INSTRUCTOR_OF_COURSE, roles = "INSTRUCTOR")
    void testAddCompetencyToLearningPathsOnImportCompetency() throws Exception {
        enableLearningPathsForTestingCourse();

        final var importedCompetency = importCompetencyRESTCall();

        final var student = userRepository.findOneByLogin(STUDENT_OF_COURSE).orElseThrow();
        final var learningPathOptional = learningPathRepository.findWithEagerCompetenciesByCourseIdAndUserId(course.getId(), student.getId());
        assertThat(learningPathOptional).isPresent();
        assertThat(learningPathOptional.get().getCompetencies()).as("should contain new competency").contains(importedCompetency);
        assertThat(learningPathOptional.get().getCompetencies().size()).as("should not remove old competencies").isEqualTo(competencies.length + 1);
        final var oldCompetencies = Set.of(competencies[0], competencies[1], competencies[2], competencies[3], competencies[4]);
        assertThat(learningPathOptional.get().getCompetencies()).as("should not remove old competencies").containsAll(oldCompetencies);
    }

    @Test
    @WithMockUser(username = INSTRUCTOR_OF_COURSE, roles = "INSTRUCTOR")
    void testRemoveCompetencyFromLearningPathsOnDeleteCompetency() throws Exception {
        enableLearningPathsForTestingCourse();

        deleteCompetencyRESTCall(competencies[0]);

        final var student = userRepository.findOneByLogin(STUDENT_OF_COURSE).orElseThrow();
        final var learningPathOptional = learningPathRepository.findWithEagerCompetenciesByCourseIdAndUserId(course.getId(), student.getId());
        assertThat(learningPathOptional).isPresent();
        assertThat(learningPathOptional.get().getCompetencies()).as("should not contain deleted competency").doesNotContain(competencies[0]);
        final var nonDeletedCompetencies = Set.of(competencies[1], competencies[2], competencies[3], competencies[4]);
        assertThat(learningPathOptional.get().getCompetencies().size()).as("should contain competencies that have not been deleted").isEqualTo(nonDeletedCompetencies.size());
        assertThat(learningPathOptional.get().getCompetencies()).as("should contain competencies that have not been deleted").containsAll(nonDeletedCompetencies);
    }

    @Test
    @WithMockUser(username = INSTRUCTOR_OF_COURSE, roles = "INSTRUCTOR")
    void testUpdateLearningPathProgressOnCreateCompetency() throws Exception {
        enableLearningPathsForTestingCourse();
        final var createdCompetency = createCompetencyRESTCall();
        // TODO
    }

    @Test
    @WithMockUser(username = INSTRUCTOR_OF_COURSE, roles = "INSTRUCTOR")
    void testUpdateLearningPathProgressOnUpdateCompetency() throws Exception {
        enableLearningPathsForTestingCourse();
        // TODO
    }

    @Test
    @WithMockUser(username = INSTRUCTOR_OF_COURSE, roles = "INSTRUCTOR")
    void testUpdateLearningPathProgressOnImportCompetency() throws Exception {
        enableLearningPathsForTestingCourse();
        final var importedCompetency = importCompetencyRESTCall();
        // TODO
    }

    @Test
    @WithMockUser(username = INSTRUCTOR_OF_COURSE, roles = "INSTRUCTOR")
    void testUpdateLearningPathProgressOnDeleteCompetency() throws Exception {
        enableLearningPathsForTestingCourse();
        deleteCompetencyRESTCall(competencies[0]);
        // TODO
    }

    @Test
    @WithMockUser(username = INSTRUCTOR_OF_COURSE, roles = "INSTRUCTOR")
    void testUpdateLearningPathProgress() {
        enableLearningPathsForTestingCourse();
        // TODO
    }
}
