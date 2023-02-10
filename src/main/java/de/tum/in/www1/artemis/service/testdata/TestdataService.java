package de.tum.in.www1.artemis.service.testdata;

import java.util.List;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import de.tum.in.www1.artemis.domain.Course;
import de.tum.in.www1.artemis.domain.User;
import de.tum.in.www1.artemis.repository.UserRepository;
import de.tum.in.www1.artemis.security.Role;
import de.tum.in.www1.artemis.security.SecurityUtils;
import de.tum.in.www1.artemis.service.CourseService;

// TODO: add method descriptions

/**
 * Service class for managing testdata.
 */
@Service
@ConditionalOnProperty(value = "artemis.testdata.enabled")
public class TestdataService {

    private final Logger log = LoggerFactory.getLogger(TestdataService.class);

    @Value("${artemis.testdata.generate-testdata-on-startup:#{false}}")
    private Boolean generateTestdataOnStartup;

    private final TestdataUserService testdataUserService;

    private final TestdataCourseService testdataCourseService;

    private final CourseService courseService;

    private final DataSource dataSource;

    private final UserRepository userRepository;

    public TestdataService(TestdataUserService testdataUserService, TestdataCourseService testdataCourseService, CourseService courseService,
            ObjectProvider<DataSource> dataSourceObjectProvider, UserRepository userRepository) {
        this.testdataUserService = testdataUserService;
        this.testdataCourseService = testdataCourseService;
        this.courseService = courseService;
        this.dataSource = dataSourceObjectProvider.getIfUnique();
        this.userRepository = userRepository;
    }

    // TODO: maybe own flag in database?
    /**
     * checks if the Artemis database just contains the internal administrator user
     */
    private boolean artemisDatabaseJustContainsInternalAdmin() {
        long usersCount = userRepository.count();
        // TODO: also check if it's really the internal admin?
        if (usersCount == 1) {
            return true;
        }
        return false;
    }

    @EventListener(ApplicationReadyEvent.class)
    // this should be executed after inserting/updating the internal administrator
    @Order(20)
    public void populateDBWithInitialTestserverTestdata(ApplicationReadyEvent event) {
        if (generateTestdataOnStartup && artemisDatabaseJustContainsInternalAdmin()) {
            log.info("Started the Testserver Testdata Database Initialization.");
            // authenticate so that db queries are possible
            SecurityUtils.setAuthorizationObject();
            // create static TestdataUsers
            List<User> studentTestdataUsers = testdataUserService.createTestdataUsers(Role.STUDENT, 5);
            List<User> teachingAssistantTestdataUsers = testdataUserService.createTestdataUsers(Role.TEACHING_ASSISTANT, 5);
            // create static TestdataCourse
            Course testcourse = testdataCourseService.createTestdataCourse("Testcourse", "tec", true);
            testdataUserService.addUsersToGroup(studentTestdataUsers, testcourse.getStudentGroupName(), Role.STUDENT);
            testdataUserService.addUsersToGroup(teachingAssistantTestdataUsers, testcourse.getTeachingAssistantGroupName(), Role.TEACHING_ASSISTANT);
        }
        else if (!artemisDatabaseJustContainsInternalAdmin()) {
            log.debug("The internal database contains either more or less users than just the internal admin!");
        }
    }

}
