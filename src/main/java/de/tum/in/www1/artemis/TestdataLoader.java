package de.tum.in.www1.artemis;

import static de.tum.in.www1.artemis.security.Role.STUDENT;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import de.tum.in.www1.artemis.security.SecurityUtils;
import de.tum.in.www1.artemis.service.user.UserCreationService;
import de.tum.in.www1.artemis.web.rest.vm.ManagedUserVM;

@Component
@ConditionalOnProperty(value = "artemis.testdata.enabled")
public class TestdataLoader implements ApplicationRunner {

    private final Logger log = LoggerFactory.getLogger(TestdataLoader.class);

    private final UserCreationService userCreationService;

    private final DataSource dataSource;

    @Value("${artemis.testdata.generate-testdata-on-startup:#{false}}")
    private Boolean generateTestdataOnStartup;

    @Autowired
    public TestdataLoader(UserCreationService userCreationService, ObjectProvider<DataSource> dataSourceObjectProvider) {
        this.userCreationService = userCreationService;
        this.dataSource = dataSourceObjectProvider.getIfUnique();
    }

    private void createUser(String username) {
        // authenticate so that db queries are possible
        SecurityUtils.setAuthorizationObject();
        log.info("Create Testdata user {}", username);
        ManagedUserVM userDto = new ManagedUserVM();
        userDto.setLogin(username);
        userDto.setPassword(username);
        userDto.setActivated(true);
        userDto.setFirstName(username + "_firstname");
        userDto.setLastName(username + "_lastname");
        userDto.setEmail(username + "@artemis.example");
        userDto.setLangKey("en");
        userDto.setCreatedBy("system");
        userDto.setLastModifiedBy("system");
        // needs to be mutable --> new HashSet<>(Set.of(...))
        userDto.setAuthorities(new HashSet<>(Set.of(STUDENT.getAuthority())));
        userDto.setGroups(new HashSet<>());
        this.userCreationService.createUser(userDto);
    }

    // TODO: maybe refactor this into LiquibaseConfig where it's from when the prototype works
    // TODO: maybe check this differently?
    private boolean artemisDatabaseExists() {
        try (var statement = dataSource.getConnection().createStatement()) {
            statement.executeQuery("SELECT * FROM DATABASECHANGELOG;");
            var result = statement.executeQuery("SELECT latest_version FROM artemis_version;");
            statement.closeOnCompletion();
            if (result.next()) {
                return true;
            }
        }
        catch (SQLException e) {
        }
        return false;
    }

    public void run(ApplicationArguments args) {
        log.info("Started the TestdataLoader");
        if (generateTestdataOnStartup && !artemisDatabaseExists()) {
            log.info("HUHUAHAHAHAHAHAHAHAHAHAHHAHAHAHAHAH");
            createUser("test_student_1");
        }
    }
}
