package com.melih.bookmanager.api.controller;

import com.melih.bookmanager.api.model.Book;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.melih.bookmanager.service.BookService;
import java.util.List;

@RestController
@RequestMapping("/books")
public class BookController {
    private BookService bookService;

    @Autowired
    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

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
    public ResponseEntity<Book> getBookByISBN(@PathVariable String isbn) {
        return bookService.getBookByISBN(isbn)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Add a new Book
    @PostMapping
    public ResponseEntity<Book> addBook(@RequestBody Book newBook) {
        bookService.addBook(newBook);
        return ResponseEntity.status(HttpStatus.CREATED).body(newBook);
    }

    // Add a list of books at once
    @PostMapping("/bulk")
    public ResponseEntity<String> addBooksBulk(@RequestBody List<Book> books) {
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

    @DeleteMapping("/bulk")
    public ResponseEntity<String> deleteBooksBulk(@RequestBody List<String> isbn) {
        bookService.removeBooksBulk(isbn);
        return ResponseEntity.ok(String.format("Successfully deleted %d books", isbn.size()));
    }


}
