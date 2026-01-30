package com.melih.bookmanager.api.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CollectionIdJdbcTypeCode;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "app_user")
@Getter @Setter
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    private String role;

    private boolean active = true;

    @ManyToMany
    @JoinTable(
            name = "user_currently_reading",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "book_isbn")
    )
    private List<Book> currentlyReading = new ArrayList<>();

    @ManyToMany
    @JoinTable(
            name = "user_read_books",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "book_isbn")
    )
    private List<Book> readBooks = new ArrayList<>();

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }
}
