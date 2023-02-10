package de.tum.in.www1.artemis.service.testdata;

import static de.tum.in.www1.artemis.security.Role.STUDENT;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import de.tum.in.www1.artemis.domain.User;
import de.tum.in.www1.artemis.repository.UserRepository;
import de.tum.in.www1.artemis.security.Role;
import de.tum.in.www1.artemis.service.user.UserCreationService;
import de.tum.in.www1.artemis.service.user.UserService;
import de.tum.in.www1.artemis.web.rest.errors.EmailAlreadyUsedException;
import de.tum.in.www1.artemis.web.rest.errors.LoginAlreadyUsedException;
import de.tum.in.www1.artemis.web.rest.vm.ManagedUserVM;

// TODO: add method descriptions

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

    private void prepareUserDTO(ManagedUserVM userDto) {
        userDto.setCreatedBy("system");
        userDto.setLastModifiedBy("system");
        // needs to be mutable --> new HashSet<>(Set.of(...))
        userDto.setAuthorities(new HashSet<>(Set.of(STUDENT.getAuthority())));
        userDto.setGroups(new HashSet<>());
    }

    // TODO: all checks really necessary? this is originally from createUser API call
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

    // TODO: not sure if there is an easier approach to createUsers? This is how also the internal admin is created.
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

    public List<User> createTestdataUsers(Role role, int count) {
        List<User> users = new ArrayList<>(count);
        for (int i = 1; i <= count; i++) {
            User currentUser = createTestdataUser("test_" + role.toPrettyString().toLowerCase() + "_" + i);
            users.add(currentUser);
        }
        return users;
    }

    // TODO: add this to UserService?
    /**
     * add the users to the specified group and update in VCS (like GitLab) if used
     *
     * @param users  the users
     * @param group the group
     * @param role the role
     */
    public void addUsersToGroup(List<User> users, String group, Role role) {
        users.forEach(user -> userService.addUserToGroup(user, group, role));
    }
}
