package com.melih.bookmanager.service;

import com.melih.bookmanager.api.model.User;
import com.melih.bookmanager.repository.user.InMemoryUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.List;

public class UserServiceTests {
    private InMemoryUserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private UserService userService;

    @BeforeEach
    void setUp() {
        this.userRepository = new InMemoryUserRepository();
        this.passwordEncoder = new BCryptPasswordEncoder();

        List<User> testUsers = new ArrayList<>(List.of(
                new User("jeff", "Spring123"),
                new User("thomas", "Summer55!"),
                new User("carla10", "Cheesecake99")
        ));

        for (User user : testUsers) {
            userRepository.save(user);
        }

        this.userService = new UserService(userRepository, passwordEncoder);
    }


}
