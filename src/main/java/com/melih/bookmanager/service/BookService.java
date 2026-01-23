package com.melih.bookmanager.service;

import com.melih.bookmanager.api.model.Book;
import com.melih.bookmanager.exception.BookAlreadyExistsException;
import com.melih.bookmanager.exception.BookNotFoundException;
import com.melih.bookmanager.repository.book.InMemoryBookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class BookService {
    private final InMemoryBookRepository bookRepository;

    @Autowired
    // Constructor with instant enrichment
    public BookService(InMemoryBookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    public Book getBookByIsbn(String isbn) {
        Optional<Book> book = bookRepository.findByIsbn(isbn);
        if (book.isPresent()) {
            return book.get();
        }
        throw new BookNotFoundException(isbn);
    }

    public void addBook(Book book) {
        if(bookRepository.existsByISBN(book.getISBN())) {
            throw new BookAlreadyExistsException(book.getISBN());
        }
        bookRepository.save(book);
    }

    public void addBooksBulk(List<Book> books) {
        for (Book book : books) {
            if(bookRepository.existsByISBN(book.getISBN())) {
                throw new BookAlreadyExistsException(book.getISBN());
            }
        }

        for (Book book : books) {
            bookRepository.save(book);
        }
    }

    public void removeBook(String isbn) {
        if(bookRepository.existsByISBN(isbn)) {
            throw new BookNotFoundException(isbn);
        }
        bookRepository.delete(isbn);
    }

    public void removeBooksBulk(List<String> isbnList) {
        for (String isbn : isbnList) {
            if (bookRepository.existsByISBN(isbn)) {
                throw new BookNotFoundException(isbn);
            }
        }
        for (String isbn : isbnList) {
            bookRepository.delete(isbn);
        }
    }

    public void updateBook(Book book) {
        if (!bookRepository.existsByISBN(book.getISBN())) {
            throw new BookNotFoundException(book.getISBN());
        }
        bookRepository.save(book);
    }

    public void updateBooksBulk(List<Book> books) {
        for (Book book : books) {
            if (!bookRepository.existsByISBN(book.getISBN())) {
                throw new BookNotFoundException(book.getISBN());
            }
        }
        for (Book book : books) {
            bookRepository.save(book);
        }
    }

    // Generate a list of Dummy-Books for testing purposes
    public static List<Book> generateDummyBooks() {
        List<Book> books = new ArrayList<Book>();
        String[] ISBNs = {
                "978-3-16-148410-0",
                "978-0-545-01022-1",
                "978-1-86197-876-9",
                "978-3-8273-1542-8",
                "978-0-306-40615-7",
                "978-3-423-28213-0",
                "978-1-4028-9462-6",
                "978-3-596-19113-0",
                "978-0-14-044926-6",
                "978-3-86680-192-9"
        };
        String[] titles = {
                "Der Wind am Ende der Welt",
                "Das Schweigen der alten Eiche",
                "Hafen der verlorenen Träume",
                "Die mechanische Rose: Ein Steampunk-Abenteuer",
                "Letzter Halt: Wien Hauptbahnhof",
                "Sterne über Island",
                "Der Schatten des Uhrmachers",
                "Die vergessene Bibliothek von Palermo",
                "Jenseits des Horizonts wartet das Licht",
                "Bittersüße Mandeln"
        };
        String[] authors = {
                "Franz Kafka",
                "Hermann Hesse",
                "Haruki Murakami",
                "Albert Camus",
                "Friedrich Nietzsche"
        };
        int[] pages = {300, 350, 402, 508, 117, 958};

        for (int i = 0; i < ISBNs.length; i++) {
            String uniqueISBN = ISBNs[i];

            String randomTitle = titles[ThreadLocalRandom.current().nextInt(titles.length)];
            String randomAuthor = authors[ThreadLocalRandom.current().nextInt(authors.length)];
            int randomPages = pages[ThreadLocalRandom.current().nextInt(pages.length)];

            books.add(new Book(uniqueISBN, randomTitle, randomAuthor, randomPages));
        }
        return books;
    }
}
