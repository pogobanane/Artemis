package de.tum.in.www1.artemis.service.testdata;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import de.tum.in.www1.artemis.domain.Course;
import de.tum.in.www1.artemis.domain.FileUploadExercise;
import de.tum.in.www1.artemis.domain.TextExercise;
import de.tum.in.www1.artemis.domain.modeling.ModelingExercise;
import de.tum.in.www1.artemis.service.*;

/**
 * Service class for managing test data.
 */
@Service
@ConditionalOnProperty(value = "artemis.testdata.enabled")
public class TestdataService {

    private final Logger log = LoggerFactory.getLogger(TestdataService.class);

    private final CourseService courseService;

    private final TextExerciseService textExerciseService;

    private final ModelingExerciseService modelingExerciseService;

    private final FileUploadExerciseService fileUploadExerciseService;

    public TestdataService(CourseService courseService, TextExerciseService textExerciseService, ModelingExerciseService modelingExerciseService,
            FileUploadExerciseService fileUploadExerciseService) {
        this.courseService = courseService;
        this.textExerciseService = textExerciseService;
        this.modelingExerciseService = modelingExerciseService;
        this.fileUploadExerciseService = fileUploadExerciseService;
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

        ModelingExercise modelingExercise = new ModelingExercise();
        modelingExercise.setTitle(courseTitle + " pre-generated Modeling Exercise");
        modelingExercise.setMaxPoints(10.0);
        modelingExercise.setCourse(course);
        modelingExercise = modelingExerciseService.createModelingExercise(modelingExercise);

        FileUploadExercise fileUploadExercise = new FileUploadExercise();
        fileUploadExercise.setTitle(courseTitle + " pre-generated File Upload Exercise");
        fileUploadExercise.setMaxPoints(10.0);
        fileUploadExercise.setCourse(course);
        fileUploadExercise = fileUploadExerciseService.createFileUploadExercise(fileUploadExercise);

    }
}
