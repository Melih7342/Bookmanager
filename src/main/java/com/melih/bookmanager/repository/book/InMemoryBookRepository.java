package com.melih.bookmanager.repository.book;

import com.melih.bookmanager.api.model.Book;
import org.springframework.stereotype.Repository;
import static com.melih.bookmanager.service.BookService.generateDummyBooks;

import java.util.*;

@Repository
public class InMemoryBookRepository implements BookRepository {
    private Map<String, Book> books = new HashMap<>();

    // For testing
    public InMemoryBookRepository() {
        List<Book> dummyBooks = generateDummyBooks();
        for(Book book : dummyBooks) {
            this.books.put(book.getISBN(), book);
        }
    }

    @Override
    public List<Book> findAll() {
        return new ArrayList<>(books.values());
    }

    @Override
    public void save(Book book) {
        books.put(book.getISBN(), book);
    }

    @Override
    public void delete(String isbn) {
        books.remove(isbn);
    }

    public Optional<Book> findByIsbn(String isbn) {
        return Optional.ofNullable(books.get(isbn));
    }

    public boolean existsByISBN(String isbn) {
        return books.containsKey(isbn);
    }

    public void clear() {
        this.books.clear();
    }
}
