package com.melih.bookmanager.repository.book;

import com.melih.bookmanager.api.model.Book;

import java.util.List;
import java.util.Optional;

public interface BookRepository {
    List<Book> findAll();
    Optional<Book> findByIsbn(String isbn);

    void save(Book book);
    void delete(String isbn);
}
