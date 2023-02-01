package de.tum.in.www1.artemis;

import static de.tum.in.www1.artemis.security.Role.STUDENT;

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import de.tum.in.www1.artemis.security.SecurityUtils;
import de.tum.in.www1.artemis.service.user.UserCreationService;
import de.tum.in.www1.artemis.web.rest.vm.ManagedUserVM;

@Component
@Profile("testdata")
public class TestdataLoader implements ApplicationRunner {

    private final Logger log = LoggerFactory.getLogger(TestdataLoader.class);

    private final UserCreationService userCreationService;

    @Autowired
    public TestdataLoader(UserCreationService userCreationService) {
        this.userCreationService = userCreationService;
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
        userCreationService.createUser(userDto);
    }

    public void run(ApplicationArguments args) {
        createUser("test_student_1");
    }
}
