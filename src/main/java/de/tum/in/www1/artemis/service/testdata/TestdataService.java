package de.tum.in.www1.artemis.service.testdata;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import de.tum.in.www1.artemis.domain.Course;
import de.tum.in.www1.artemis.domain.TextExercise;
import de.tum.in.www1.artemis.service.CourseService;
import de.tum.in.www1.artemis.service.TextExerciseService;

/**
 * Service class for managing test data.
 */
@Service
@ConditionalOnProperty(value = "artemis.testdata.enabled")
public class TestdataService {

    private final Logger log = LoggerFactory.getLogger(TestdataService.class);

    private final CourseService courseService;

    private final TextExerciseService textExerciseService;

    public TestdataService(CourseService courseService, TextExerciseService textExerciseService) {
        this.courseService = courseService;
        this.textExerciseService = textExerciseService;
    }

    /**
     * Creates a testdata course
     *
     * @param courseTitle the course title
     * @param courseShortName the course shortname
     */
    public void createTestdataCourseWithExercises(String courseTitle, String courseShortName) {
        log.debug("Create Testdata course with exercises {}", courseTitle);
        Course course = new Course();
        course.setTitle(courseTitle);
        course.setShortName(courseShortName);
        course.setRegistrationEnabled(false);
        course.setTestCourse(true);
        course.setPresentationScore(0);

        course = courseService.createCourse(course, null);

        TextExercise textExercise = new TextExercise();
        textExercise.setTitle(courseTitle + " pre-generated Text Exercise");
        textExercise.setMaxPoints(10.0);
        textExercise.setCourse(course);

        textExercise = textExerciseService.createTextExercise(textExercise);
    }
}
