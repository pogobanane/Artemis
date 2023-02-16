package de.tum.in.www1.artemis.service.testdata;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import de.tum.in.www1.artemis.domain.Course;
import de.tum.in.www1.artemis.service.CourseService;

/**
 * Service class for managing test courses.
 */
@Service
@ConditionalOnProperty(value = "artemis.testdata.enabled")
public class TestdataCourseService {

    private final Logger log = LoggerFactory.getLogger(TestdataCourseService.class);

    private final CourseService courseService;

    public TestdataCourseService(CourseService courseService) {
        this.courseService = courseService;
    }

    /**
     * Creates a testdata course
     *
     * @param title the course title
     * @param shortName the course shortname
     * @return the created testdata course
     */
    public Course createTestdataCourse(String title, String shortName) {
        log.debug("Create Testdata course {}", title);
        Course course = new Course();
        course.setTitle(title);
        course.setShortName(shortName);
        course.setRegistrationEnabled(false);
        course.setTestCourse(true);
        course.setPresentationScore(0);

        course = courseService.createCourse(course, null);

        return course;
    }
}
