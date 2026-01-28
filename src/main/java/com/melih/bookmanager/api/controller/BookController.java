package com.melih.bookmanager.api.controller;

import com.melih.bookmanager.api.model.Book;
import com.melih.bookmanager.repository.book.InMemoryBookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.melih.bookmanager.service.BookService;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/books")
public class BookController {
    private final BookService bookService;

    // Get all books
    @GetMapping
    public ResponseEntity<List<Book>> getAllBooks() {
        List<Book> books = bookService.getAllBooks();

        if (books.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(books);
    }

    // Get a specific book with its ISBN
    @GetMapping("/{isbn}")
    public Book getBookByISBN(@PathVariable String isbn) {
        return bookService.getBookByIsbn(isbn);
    }

    // Add a new Book
    @PostMapping
    public ResponseEntity<Book> createBook(@RequestBody Book newBook) {
        bookService.addBook(newBook);
        ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{isbn}")
                .buildAndExpand(newBook.getISBN())
                .toUri();
        return ResponseEntity.status(HttpStatus.CREATED).body(newBook);
    }

    // Add a list of books at once
    @PostMapping("/bulk")
    public ResponseEntity<String> createBooksBulk(@RequestBody List<Book> books) {
        bookService.addBooksBulk(books);
        // Only number of added books in body to save performance
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(String.format("Successfully added %d books", books.size()));
    }

    // Delete a specific book by isbn
    @DeleteMapping("/{isbn}")
    public ResponseEntity<String> deleteBook(@PathVariable String isbn) {
        bookService.removeBook(isbn);
        return ResponseEntity.noContent().build();
    }

    // Delete bulk of books
    @DeleteMapping("/bulk")
    public ResponseEntity<String> deleteBooksBulk(@RequestBody List<String> isbn) {
        bookService.removeBooksBulk(isbn);
        return ResponseEntity.ok(String.format("Successfully deleted %d books", isbn.size()));
    }

    // Update one book
    @PutMapping
    public ResponseEntity<String> updateBook(@RequestBody Book book) {
        bookService.updateBook(book);
        return ResponseEntity.noContent().build();
    }

    // Update bulk of books
    @PutMapping("/bulk")
    public ResponseEntity<String> updateBooksBulk(@RequestBody List<Book> books) {
        bookService.updateBooksBulk(books);
        return ResponseEntity.ok(String.format("Successfully updated %d books", books.size()));
    }
}
