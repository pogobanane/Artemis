package de.tum.in.www1.artemis.web.rest.testdata;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import de.tum.in.www1.artemis.security.annotations.EnforceAdmin;
import de.tum.in.www1.artemis.service.testdata.TestdataService;
import de.tum.in.www1.artemis.web.rest.dto.TestdataCourseWithExercisesDTO;

@RestController
@ConditionalOnProperty(value = "artemis.testdata.enabled")
@RequestMapping("/api/testdata")
public class TestdataResource {

    private final Logger log = LoggerFactory.getLogger(TestdataResource.class);

    private final TestdataService testdataService;

    public TestdataResource(TestdataService testdataService) {
        this.testdataService = testdataService;
    }

    /**
     * POST /course-with-exercises : create a new testdata course with each exercise
     *
     * @param course a minimized CourseDTO just for the use case to generate Testdata
     * @return the ResponseEntity with status 201 (Created) and with body the new course
     */
    @PostMapping(value = "course-with-exercises")
    @EnforceAdmin
    public ResponseEntity<Void> createCourseWithExercises(@RequestBody TestdataCourseWithExercisesDTO course) {
        log.debug("REST request to save testdata Course : {}", course.title());

        testdataService.createTestdataCourseWithExercises(course.title(), course.shortName());

        return ResponseEntity.ok().body(null);
    }
}
