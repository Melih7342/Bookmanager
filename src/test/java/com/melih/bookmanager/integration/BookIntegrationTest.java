package com.melih.bookmanager.integration;

import com.melih.bookmanager.api.model.Book;
import com.melih.bookmanager.repository.book.BookRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org. springframework.http.MediaType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class BookIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BookRepository bookRepository;

    @BeforeEach
    void setUp() {
        bookRepository.deleteAll();
        bookRepository.save(new Book("978-3-16-148410-20", "Der Wind am Ende der Welt", "Franz Kafka", 300));
        bookRepository.save(new Book("978-0-545-01022-33", "Das Schweigen der alten Eiche", "Hermann Hesse", 350));
    }

    @AfterEach
    void tearDown() {
        bookRepository.deleteAll();
    }

    @Test
    @WithMockUser(username = "testuser")
    void whenGetAllBooks_thenOk() throws Exception {
        // WHEN
        mockMvc.perform(get("/books"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "testuser")
    void createBook_AndRetrieveIt() throws Exception {
        Book newBook = new Book("978-3-16-148410-0", "The Great Gatsby", "F. Scott Fitzgerald", 126);

        // 1. Save book
        mockMvc.perform(post("/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newBook)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"));

        // 2. Retrieve book
        mockMvc.perform(get("/books/978-3-16-148410-0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("The Great Gatsby"));
    }
}
