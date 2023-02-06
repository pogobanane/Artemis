package de.tum.in.www1.artemis.web.rest.testdata;

import static de.tum.in.www1.artemis.security.Role.STUDENT;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import de.tum.in.www1.artemis.domain.User;
import de.tum.in.www1.artemis.repository.UserRepository;
import de.tum.in.www1.artemis.security.annotations.EnforceAdmin;
import de.tum.in.www1.artemis.service.user.UserCreationService;
import de.tum.in.www1.artemis.service.user.UserService;
import de.tum.in.www1.artemis.web.rest.errors.*;
import de.tum.in.www1.artemis.web.rest.vm.ManagedUserVM;
import net.datafaker.Faker;

@RestController
@ConditionalOnProperty(value = "artemis.testdata.enabled")
@RequestMapping("/api/testdata")
public class TestdataUserResource {

    private final Logger log = LoggerFactory.getLogger(TestdataUserResource.class);

    private final UserService userService;

    private final UserRepository userRepository;

    private final UserCreationService userCreationService;

    private final Faker faker = new Faker();

    public TestdataUserResource(UserService userService, UserRepository userRepository, UserCreationService userCreationService) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.userCreationService = userCreationService;
    }

    @GetMapping("random-users")
    @EnforceAdmin
    public ResponseEntity<List<User>> createRandomUsers() {
        int numberOfRandomUsers = 5;
        List<String> fakeFirstnames = faker.<String>collection(() -> faker.name().firstName()).len(numberOfRandomUsers).generate();
        List<String> fakeLastnames = faker.<String>collection(() -> faker.name().lastName()).len(numberOfRandomUsers).generate();
        List<User> randomUsers = new ArrayList<>();

        for (int i = 0; i < numberOfRandomUsers; i++) {
            String firstname = fakeFirstnames.get(i);
            String lastname = fakeLastnames.get(i);
            String username = String.format("%s.%s", firstname, lastname);
            ManagedUserVM userDto = new ManagedUserVM();
            userDto.setLogin(username);
            userDto.setPassword(username);
            userDto.setActivated(true);
            userDto.setFirstName(firstname);
            userDto.setLastName(lastname);
            userDto.setEmail(username.toLowerCase() + "@artemis.example");
            userDto.setLangKey("en");
            userDto.setCreatedBy("system");
            userDto.setLastModifiedBy("system");
            // needs to be mutable --> new HashSet<>(Set.of(...))
            userDto.setAuthorities(new HashSet<>(Set.of(STUDENT.getAuthority())));
            userDto.setGroups(new HashSet<>());

            log.debug("REST request to save generated User : {}", userDto);
            this.userService.checkUsernameAndPasswordValidityElseThrow(userDto.getLogin(), userDto.getPassword());

            if (userRepository.findOneByLogin(userDto.getLogin().toLowerCase()).isPresent()) {
                throw new LoginAlreadyUsedException();
            }
            else if (userRepository.findOneByEmailIgnoreCase(userDto.getEmail()).isPresent()) {
                throw new EmailAlreadyUsedException();
            }
            else {
                User newUser = userCreationService.createUser(userDto);
                randomUsers.add(newUser);
            }
        }
        return ResponseEntity.ok(randomUsers);
    }
}
