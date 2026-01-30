package com.melih.bookmanager.integration;

import com.melih.bookmanager.api.model.User;
import com.melih.bookmanager.repository.user.UserRepository;
import com.melih.bookmanager.utils.UserAuthenticationRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class UserIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setup() {
        userRepository.deleteAll();
        User jeff = new User("jeff", passwordEncoder.encode("Spring123!"));
        userRepository.save(jeff);
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
    }

    @Test
    void givenValidCredentials_whenRegisterUser_thenReturnCreatedAndSaveUser() throws Exception {
        // GIVEN
        UserAuthenticationRequest request = new UserAuthenticationRequest("new_user", "SafePassword123");

        // WHEN
        mockMvc.perform(post("/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"));

        // THEN
        Optional<User> savedUser = userRepository.findByUsername("new_user");
        assertThat(savedUser).isPresent();
        assertThat(savedUser.get().getPassword()).startsWith("$2a$");
    }

    @Test
    void givenValidCredentials_whenLoginUser_thenReturnOk() throws Exception {
        // GIVEN
        UserAuthenticationRequest request = new UserAuthenticationRequest("jeff", "Spring123!");

        // WHEN & THEN
        mockMvc.perform(post("/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void givenInvalidPassword_whenLoginUser_thenReturnUnauthorized() throws Exception {
        // GIVEN
        UserAuthenticationRequest request = new UserAuthenticationRequest("jeff", "InvalidPassword55?");

        // WHEN & THEN
        mockMvc.perform(post("/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
}
