package de.tum.in.www1.artemis.service.testdata;

import static de.tum.in.www1.artemis.security.Role.STUDENT;

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import de.tum.in.www1.artemis.domain.User;
import de.tum.in.www1.artemis.repository.UserRepository;
import de.tum.in.www1.artemis.service.user.UserCreationService;
import de.tum.in.www1.artemis.service.user.UserService;
import de.tum.in.www1.artemis.web.rest.errors.EmailAlreadyUsedException;
import de.tum.in.www1.artemis.web.rest.errors.LoginAlreadyUsedException;
import de.tum.in.www1.artemis.web.rest.vm.ManagedUserVM;

/**
 * Service class for managing test users.
 */
@Service
@ConditionalOnProperty(value = "artemis.testdata.enabled")
public class TestdataUserService {

    private final Logger log = LoggerFactory.getLogger(TestdataUserService.class);

    private final UserCreationService userCreationService;

    private final UserService userService;

    private final UserRepository userRepository;

    public TestdataUserService(UserCreationService userCreationService, UserService userService, UserRepository userRepository) {
        this.userCreationService = userCreationService;
        this.userService = userService;
        this.userRepository = userRepository;
    }

    private ManagedUserVM prepareUserDTO(ManagedUserVM userDto) {
        userDto.setCreatedBy("system");
        userDto.setLastModifiedBy("system");
        // needs to be mutable --> new HashSet<>(Set.of(...))
        userDto.setAuthorities(new HashSet<>(Set.of(STUDENT.getAuthority())));
        userDto.setGroups(new HashSet<>());
        return userDto;
    }

    private User validateUser(ManagedUserVM userDto) {
        this.userService.checkUsernameAndPasswordValidityElseThrow(userDto.getLogin(), userDto.getPassword());

        if (userRepository.findOneByLogin(userDto.getLogin().toLowerCase()).isPresent()) {
            throw new LoginAlreadyUsedException();
        }
        else if (userRepository.findOneByEmailIgnoreCase(userDto.getEmail()).isPresent()) {
            throw new EmailAlreadyUsedException();
        }
        else {
            return userCreationService.createUser(userDto);
        }
    }

    public User createTestdataUser(String username) {
        log.debug("Create Testdata user {}", username);
        ManagedUserVM userDto = new ManagedUserVM();
        userDto.setLogin(username);
        userDto.setPassword(username);
        userDto.setFirstName(username + "_firstname");
        userDto.setLastName(username + "_lastname");
        userDto.setEmail(username + "@artemis.example");
        return validateUser(userDto);
    }

    public User createTestdataUser(String firstname, String lastname) {
        String username = String.format("%s.%s", firstname, lastname);
        ManagedUserVM userDto = new ManagedUserVM();
        userDto.setLogin(username);
        userDto.setPassword(username);
        userDto.setFirstName(firstname);
        userDto.setLastName(lastname);
        userDto.setEmail(username.toLowerCase() + "@artemis.example");
        prepareUserDTO(userDto);
        return validateUser(userDto);
    }

}
