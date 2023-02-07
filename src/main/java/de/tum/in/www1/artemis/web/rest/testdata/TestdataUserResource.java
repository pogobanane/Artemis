package de.tum.in.www1.artemis.web.rest.testdata;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import de.tum.in.www1.artemis.domain.User;
import de.tum.in.www1.artemis.security.annotations.EnforceAdmin;
import de.tum.in.www1.artemis.service.testdata.TestdataUserService;
import de.tum.in.www1.artemis.web.rest.errors.*;
import net.datafaker.Faker;

@RestController
@ConditionalOnProperty(value = "artemis.testdata.enabled")
@RequestMapping("/api/testdata")
public class TestdataUserResource {

    private final Logger log = LoggerFactory.getLogger(TestdataUserResource.class);

    private final TestdataUserService testdataUserService;

    private final Faker faker = new Faker();

    public TestdataUserResource(TestdataUserService testdataUserService) {
        this.testdataUserService = testdataUserService;
    }

    @GetMapping("random-users")
    @EnforceAdmin
    public ResponseEntity<List<User>> createRandomUsers() {
        int numberOfRandomUsers = 5;
        List<String> fakeFirstnames = faker.<String>collection(() -> faker.name().firstName()).len(numberOfRandomUsers).generate();
        List<String> fakeLastnames = faker.<String>collection(() -> faker.name().lastName()).len(numberOfRandomUsers).generate();
        List<User> randomUsers = new ArrayList<>();

        for (int i = 0; i < numberOfRandomUsers; i++) {
            String randomFirstname = fakeFirstnames.get(i);
            String randomLastname = fakeLastnames.get(i);
            User newUser = testdataUserService.createTestdataUser(randomFirstname, randomLastname);
            randomUsers.add(newUser);
        }
        return ResponseEntity.ok(randomUsers);
    }
}
