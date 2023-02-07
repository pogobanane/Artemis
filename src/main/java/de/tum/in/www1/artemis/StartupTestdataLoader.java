package de.tum.in.www1.artemis;

import java.sql.SQLException;

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
import de.tum.in.www1.artemis.service.testdata.TestdataUserService;

@Component
@ConditionalOnProperty(value = "artemis.testdata.enabled")
public class StartupTestdataLoader implements ApplicationRunner {

    private final Logger log = LoggerFactory.getLogger(StartupTestdataLoader.class);

    private final TestdataUserService testdataUserService;

    private final DataSource dataSource;

    @Value("${artemis.testdata.generate-testdata-on-startup:#{false}}")
    private Boolean generateTestdataOnStartup;

    @Autowired
    public StartupTestdataLoader(TestdataUserService testdataUserService, ObjectProvider<DataSource> dataSourceObjectProvider) {
        this.testdataUserService = testdataUserService;
        this.dataSource = dataSourceObjectProvider.getIfUnique();
    }

    // TODO: maybe refactor this into LiquibaseConfig where it's from when the prototype works
    // TODO: maybe check this differently?
    private boolean artemisDatabaseInitialized() {
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
        if (generateTestdataOnStartup && !artemisDatabaseInitialized()) {
            // authenticate so that db queries are possible
            SecurityUtils.setAuthorizationObject();
            testdataUserService.createTestdataUser("test_student_1");
        }
    }
}
