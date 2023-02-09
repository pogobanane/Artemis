package de.tum.in.www1.artemis.service.testdata;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import de.tum.in.www1.artemis.domain.Course;
import de.tum.in.www1.artemis.repository.CourseRepository;
import de.tum.in.www1.artemis.service.CourseService;
import de.tum.in.www1.artemis.service.OnlineCourseConfigurationService;

/**
 * Service class for managing test users.
 */
@Service
@ConditionalOnProperty(value = "artemis.testdata.enabled")
public class TestdataCourseService {

    private final Logger log = LoggerFactory.getLogger(TestdataCourseService.class);

    private final CourseRepository courseRepository;

    private final OnlineCourseConfigurationService onlineCourseConfigurationService;

    private final CourseService courseService;

    public TestdataCourseService(CourseRepository courseRepository, OnlineCourseConfigurationService onlineCourseConfigurationService, CourseService courseService) {
        this.courseRepository = courseRepository;
        this.onlineCourseConfigurationService = onlineCourseConfigurationService;
        this.courseService = courseService;
    }

    public Course createTestdataCourse(String title, String shortName, boolean testCourse) {
        log.debug("Create Testdata course {}", title);
        Course course = new Course();
        course.setTitle(title);
        course.setShortName(shortName);
        course.setTestCourse(testCourse);

        course.validateShortName();

        List<Course> coursesWithSameShortName = courseRepository.findAllByShortName(course.getShortName());
        if (!coursesWithSameShortName.isEmpty()) {
            String error = "A course with the same short name already exists. Please choose a different short name.";
            log.error(error);
            throw new RuntimeException(error);
        }

        course.validateRegistrationConfirmationMessage();
        course.validateComplaintsAndRequestMoreFeedbackConfig();
        course.validateOnlineCourseAndRegistrationEnabled();
        course.validateAccuracyOfScores();
        if (!course.isValidStartAndEndDate()) {
            String error = "For Courses, the start date has to be before the end date.";
            log.error(error);
            throw new RuntimeException(error);
        }

        if (course.isOnlineCourse()) {
            onlineCourseConfigurationService.createOnlineCourseConfiguration(course);
        }

        courseService.createOrValidateGroups(course);

        course = courseRepository.save(course);

        return course;
    }

}
