package com.melih.bookmanager.service;

import com.melih.bookmanager.api.model.Book;
import com.melih.bookmanager.api.model.User;
import com.melih.bookmanager.exception.Book.BookNotFoundException;
import com.melih.bookmanager.exception.User.BadCredentialsException;
import com.melih.bookmanager.exception.User.InactiveAccountException;
import com.melih.bookmanager.exception.User.UsernameAlreadyExistsException;
import com.melih.bookmanager.repository.book.BookRepository;
import com.melih.bookmanager.repository.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private final String username = "jeff";
    private final String password = "Spring123";
    private final String encodedPassword = "encodedSpring123";

    @BeforeEach
    void setUp() {
        testUser = new User(username, encodedPassword);
    }

    @Test
    void givenExistingUsername_whenGetUserByUsername_thenReturnRightUser() {
        // GIVEN
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(testUser));

        // WHEN
        User result = userService.getUserByUsername(username);

        // THEN
        assertThat(result.getUsername()).isEqualTo(username);
        verify(userRepository).findByUsername(username);
    }

    @Test
    void givenNewUsername_whenRegister_thenNewUserIsRegistered() {
        // WHEN
        userService.register(username, password);

        // THEN
        verify(userRepository).save(argThat(user -> user.getUsername().equals(username)));
        verify(passwordEncoder).encode(password);
    }

    @Test
    void givenExistingUsername_whenRegister_thenThrowUsernameAlreadyExistsException() {
        // WHEN
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(testUser));

        // THEN
        assertThatThrownBy(() -> userService.register(username, password))
                .isInstanceOf(UsernameAlreadyExistsException.class);
    }

    @Test
    void givenValidCredentials_whenLogin_thenUserIsAuthenticated() {
        // GIVEN
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(password, encodedPassword)).thenReturn(true);

        // WHEN & THEN
        assertThatCode(() -> userService.login(username, password))
                .doesNotThrowAnyException();
    }

    @Test
    void givenFalsePassword_whenLogin_thenThrowBadCredentialsException() {
        // GIVEN
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        // WHEN & THEN
        assertThatThrownBy(() -> userService.login(username, "wrongPassword"))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void givenDeactivatedAccount_whenLogin_thenThrowInactiveAccountException() {
        // GIVEN
        testUser.setActive(false);
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(password, encodedPassword)).thenReturn(true);

        // WHEN & THEN
        assertThatThrownBy(() -> userService.login(username, password))
                .isInstanceOf(InactiveAccountException.class);
    }

    @Test
    void givenValidCredentials_whenDeactivateAccount_thenAccountIsDeactivated() {
        // GIVEN
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(password, encodedPassword)).thenReturn(true);

        // WHEN
        userService.deactivateAccount(username, password);

        // THEN
        verify(userRepository).save(argThat(user -> !user.isActive()));
    }

    @Test
    void givenValidCredentials_whenChangePassword_thenPasswordChanged() {
        // GIVEN
        String newPassword = "newSecret123";
        String newEncoded = "encodedNewSecret";

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(password, encodedPassword)).thenReturn(true);
        when(passwordEncoder.encode(newPassword)).thenReturn(newEncoded);

        // WHEN
        userService.changePassword(username, password, newPassword);

        // THEN
        verify(userRepository).save(argThat(user -> user.getPassword().equals(newEncoded)));
    }

    @Test
    void givenUserReadingBook_whenMarkAsRead_thenListsAreUpdated() {
        // GIVEN
        String isbn = "123";
        Book book = new Book(isbn, "Title", "Author", 100);

        testUser.getCurrentlyReading().add(book);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(testUser));
        when(bookRepository.findById(isbn)).thenReturn(Optional.of(book));

        // WHEN
        userService.markAsRead(username, isbn);

        // THEN
        assertThat(testUser.getReadBooks()).contains(book);
        assertThat(testUser.getCurrentlyReading()).doesNotContain(book);

        // Verification
        verify(userRepository).findByUsername(username);
        verify(bookRepository).findById(isbn);
    }

    @Test
    void givenNonExistingBook_whenMarkAsRead_thenThrowException() {
        // GIVEN
        String nonExistingIsbn = "999";
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(testUser));
        when(bookRepository.findById(nonExistingIsbn)).thenReturn(Optional.empty());

        // WHEN & THEN
        assertThatThrownBy(() -> userService.markAsRead(username, nonExistingIsbn))
                .isInstanceOf(BookNotFoundException.class);

        assertThat(testUser.getReadBooks()).isEmpty();
    }

}
