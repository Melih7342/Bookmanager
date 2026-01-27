package com.melih.bookmanager.service;

import com.melih.bookmanager.api.model.User;
import com.melih.bookmanager.exception.User.BadCredentialsException;
import com.melih.bookmanager.exception.User.InactiveAccountException;
import com.melih.bookmanager.exception.User.UsernameAlreadyExistsException;
import com.melih.bookmanager.repository.user.InMemoryUserRepository;
import com.melih.bookmanager.utils.UserResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

public class UserServiceTests {
    private InMemoryUserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private UserService userService;

    @BeforeEach
    void setUp() {
        this.userRepository = new InMemoryUserRepository();
        this.passwordEncoder = new BCryptPasswordEncoder();

        List<User> testUsers = new ArrayList<>(List.of(
                new User("jeff", passwordEncoder.encode("Spring123")),
                new User("thomas", passwordEncoder.encode("Summer55!")),
                new User("carla10", passwordEncoder.encode("Cheesecake99"))
        ));

        for (User user : testUsers) {
            userRepository.save(user);
        }

        this.userService = new UserService(userRepository, passwordEncoder);
    }

    @Test
    void givenExistingUsername_whenGetUserByUsername_thenReturnRightUser() {
        // GIVEN
        String username = "jeff";

        // WHEN
        User result = userService.getUserByUsername(username);

        // THEN
        assertThat(result.getUsername()).isEqualTo("jeff");
        assertThat(result.isActive()).isTrue();

        assertThat(result.getCurrentlyReading())
                .isNotNull()
                .isEmpty();

        assertThat(result.getReadBooks())
                .isNotNull()
                .isEmpty();
    }

    @Test
    void givenNonExistingUsername_whenGetUserByUsername_thenExceptionIsThrown() {
        // GIVEN
        String nonExistingUsername = "IdoNotExist";

        // WHEN & THEN
        assertThatThrownBy(() -> userService.getUserByUsername(nonExistingUsername))
                .isInstanceOf(UsernameNotFoundException.class);
    }

    @Test
    void givenExistingUsername_whenGetUserProfile_thenReturnRightUserProfile() {
        // GIVEN
        String username = "jeff";

        // WHEN
        UserResponse result = userService.getUserProfile(username);

        // THEN
        assertThat(result.getUsername()).isEqualTo("jeff");

        assertThat(result.getCurrentlyReading())
                .isNotNull()
                .isEmpty();

        assertThat(result.getReadBooks())
                .isNotNull()
                .isEmpty();
    }

    @Test
    void givenNonExistingUsername_whenGetUserProfile_thenExceptionIsThrown() {
        // GIVEN
        String nonExistingUsername = "IdoNotExist";

        // WHEN & THEN
        assertThatThrownBy(() -> userService.getUserProfile(nonExistingUsername))
                .isInstanceOf(UsernameNotFoundException.class);
    }

    @Test
    void givenNewUsername_whenRegister_thenNewUserIsRegistered() {
        // GIVEN
        String username = "samantha";
        String password = "JokerArkham1";

        // WHEN
        userService.register(username, password);

        // THEN
        assertThat(userRepository.existsByUsername(username));
    }

    @Test
    void givenExistingUsername_whenRegister_thenExceptionIsThrown() {
        // GIVEN
        String username = "jeff";
        String password = "JokerArkham1";

        // WHEN & THEN
        assertThatThrownBy(() -> userService.register(username, password))
                .isInstanceOf(UsernameAlreadyExistsException.class);
    }

    @Test
    void givenValidCredentials_whenLogin_thenUserIsAuthenticated() {
        // GIVEN
        String username = "jeff";
        String password = "Spring123";

        // WHEN & THEN
        assertThatCode(() -> userService.login(username, password))
                .doesNotThrowAnyException();
    }

    @Test
    void givenFalsePassword_whenLogin_thenThrowBadCredentialsException() {
        // GIVEN
        String username = "jeff";
        String password = "JokerArkham1";

        // WHEN & THEN
        assertThatThrownBy(() -> userService.login(username, password))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void givenNonExistingUsername_whenLogin_thenThrowBadCredentialsException() {
        // GIVEN
        String username = "alexandra12";
        String password = "Spring123";

        // WHEN & THEN
        assertThatThrownBy(() -> userService.login(username, password))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void givenDeactivatedAccount_whenLogin_thenThrowInactiveAccountException() {
        // GIVEN
        String username = "jeff";
        String password = "Spring123";

        Optional<User> user = userRepository.findByUsername(username); // find the corresponding user
        user.ifPresent(u -> u.setActive(false)); // set the field "active" to false

        // WHEN & THEN
        assertThatThrownBy(() -> userService.login(username, password))
                .isInstanceOf(InactiveAccountException.class);
    }

    @Test
    void givenValidCredentials_whenDeactivateAccount_thenAccountIsDeactivated() {
        // GIVEN
        String username = "jeff";
        String password = "Spring123";

        // WHEN
        userService.deactivateAccount(username, password);

        // THEN
        User updatedUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new AssertionError("User should exist in repository"));

        assertThat(updatedUser.isActive()).isFalse();

    }

    @Test
    void givenNonExistingUsername_whenDeactivateAccount_thenThrowBadCredentialsException() {
        // GIVEN
        String username = "alexandra12";
        String password = "Spring123";

        // WHEN & THEN
        assertThatThrownBy(() -> userService.deactivateAccount(username, password))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void givenFalsePassword_whenDeactivateAccount_thenThrowBadCredentialsException() {
        // GIVEN
        String username = "jeff";
        String password = "JokerArkham1";

        // WHEN & THEN
        assertThatThrownBy(() -> userService.deactivateAccount(username, password))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void givenValidCredentials_whenChangePassword_thenPasswordChanged() {
        // GIVEN
        String username = "jeff";
        String oldPassword = "Spring123";
        String newPassword = "JeffsNewPassword";

        // WHEN
        userService.changePassword(username, oldPassword, newPassword);

        // THEN
        User jeff = userRepository.findByUsername("jeff")
                .orElseThrow(() -> new AssertionError("User should exist in repository"));
        assertThat(passwordEncoder.matches(newPassword, jeff.getPassword()));
    }

    @Test
    void givenNonExistingUsername_whenChangePassword_thenThrowBadCredentialsException() {
        // GIVEN
        String username = "alexandra12";
        String oldPassword = "Spring123";
        String newPassword = "JokerArkham1";

        // WHEN & THEN
        assertThatThrownBy(() -> userService.changePassword(username, oldPassword, newPassword))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void givenFalsePassword_whenChangePassword_thenThrowBadCredentialsException() {
        // GIVEN
        String username = "jeff";
        String oldPassword = "JokerArkham1";
        String newPassword = "JeffsNewPassword";

        // WHEN & THEN
        assertThatThrownBy(() -> userService.changePassword(username, oldPassword, newPassword))
                .isInstanceOf(BadCredentialsException.class);
    }
}
