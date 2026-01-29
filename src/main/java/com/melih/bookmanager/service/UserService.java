package com.melih.bookmanager.service;

import com.melih.bookmanager.api.model.Book;
import com.melih.bookmanager.api.model.User;
import com.melih.bookmanager.exception.User.BadCredentialsException;
import com.melih.bookmanager.exception.User.InactiveAccountException;
import com.melih.bookmanager.repository.book.BookRepository;
import com.melih.bookmanager.repository.user.UserRepository;
import com.melih.bookmanager.utils.UserResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class UserService {
    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final PasswordEncoder passwordEncoder;

    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(username));
    }

    public UserResponse getUserProfile(String username) {
        User user = getUserByUsername(username);

        UserResponse userResponse = new UserResponse();

        // build the user response
        userResponse.setId(user.getId());
        userResponse.setUsername(user.getUsername());
        userResponse.setCurrentlyReading(user.getCurrentlyReading());
        userResponse.setReadBooks(user.getReadBooks());
        userResponse.setRole(user.getRole());

        return userResponse;
    }


    public void register(String username, String password) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole("ROLE_USER");

        userRepository.save(user);
    }

    public void login(String username, String password) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(BadCredentialsException::new);

        if (!user.isActive()) {
            throw new InactiveAccountException();
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BadCredentialsException();
        }
    }

    public void deactivateAccount(String username, String password) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BadCredentialsException(username));

        if (passwordEncoder.matches(password, user.getPassword())) {
            user.setActive(false);
            userRepository.save(user);
        } else {
            throw new BadCredentialsException();
        }
    }

    public void changePassword(String username, String oldPassword, String newPassword) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(BadCredentialsException::new);

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BadCredentialsException();
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Transactional
    public void markAsRead(String username, String isbn) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Book book = bookRepository.findById(isbn)
                .orElseThrow(() -> new RuntimeException("Book not found"));

        user.getReadBooks().add(book);
        user.getCurrentlyReading().remove(book);
    }
}
