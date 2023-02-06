package de.tum.in.www1.artemis.service.testdata;

import static de.tum.in.www1.artemis.security.Role.STUDENT;

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import de.tum.in.www1.artemis.security.SecurityUtils;
import de.tum.in.www1.artemis.service.user.UserCreationService;
import de.tum.in.www1.artemis.web.rest.vm.ManagedUserVM;

/**
 * Service class for managing test users.
 */
@Service
@ConditionalOnProperty(value = "artemis.testdata.enabled")
public class TestdataUserService {

    private final Logger log = LoggerFactory.getLogger(TestdataUserService.class);

    private UserCreationService userCreationService;

    public TestdataUserService(UserCreationService userCreationService) {
        this.userCreationService = userCreationService;
    }

    public void createTestdataUser(String username) {
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

}
