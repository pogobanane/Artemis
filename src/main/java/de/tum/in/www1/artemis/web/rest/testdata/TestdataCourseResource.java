package de.tum.in.www1.artemis.web.rest.testdata;

import java.net.URI;
import java.net.URISyntaxException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import de.tum.in.www1.artemis.domain.Course;
import de.tum.in.www1.artemis.security.annotations.EnforceAdmin;
import de.tum.in.www1.artemis.service.testdata.TestdataCourseService;
import de.tum.in.www1.artemis.web.rest.dto.TestdataCourseDTO;

@RestController
@ConditionalOnProperty(value = "artemis.testdata.enabled")
@RequestMapping("/api/testdata")
public class TestdataCourseResource {

    private final Logger log = LoggerFactory.getLogger(TestdataCourseResource.class);

    private final TestdataCourseService testdataCourseService;

    public TestdataCourseResource(TestdataCourseService testdataCourseService) {
        this.testdataCourseService = testdataCourseService;
    }

    /**
     * POST /courses : create a new testdata course with each exercise
     *
     * @param course a minimized CourseDTO just for the use case to generate Testdata
     * @return the ResponseEntity with status 201 (Created) and with body the new course
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping(value = "courses")
    @EnforceAdmin
    public ResponseEntity<Course> createCourse(@RequestBody TestdataCourseDTO course) throws URISyntaxException {
        log.debug("REST request to save testdata Course : {}", course.title());

        Course result = testdataCourseService.createTestdataCourse(course.title(), course.shortName());

        return ResponseEntity.created(new URI("/api/courses/" + result.getId())).body(result);
    }
}
