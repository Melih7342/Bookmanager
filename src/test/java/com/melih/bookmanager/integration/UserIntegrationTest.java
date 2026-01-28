package com.melih.bookmanager.integration;

import com.melih.bookmanager.api.model.User;
import com.melih.bookmanager.repository.user.InMemoryUserRepository;
import com.melih.bookmanager.utils.UserAuthenticationRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

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
    private InMemoryUserRepository userRepository;

    @BeforeEach
    void setup() {
        userRepository.save(new User("jeff", "Spring123!"));
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
        assertThat(userRepository.findByUsername("new_user")).isPresent();
    }

    @Test
    void givenInvalidPassword_whenLoginUser_thenReturnUnauthorized() throws Exception {
        // GIVEN
        UserAuthenticationRequest request = new UserAuthenticationRequest("jeff", "InvalidPassword55?");

        // WHEN
        mockMvc.perform(post("/user/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

}
