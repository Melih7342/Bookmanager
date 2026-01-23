package com.melih.bookmanager.service;

import com.melih.bookmanager.api.model.Book;
import com.melih.bookmanager.exception.BookAlreadyExistsException;
import com.melih.bookmanager.exception.BookNotFoundException;
import com.melih.bookmanager.repository.book.BookRepository;
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

    public Optional<Book> getBookByISBN(String isbn) {
        Optional optional = Optional.empty();
        for(Book book : books) {
            if(book.getISBN().equals(isbn)) {
                optional = Optional.of(book);
            }
        }
        return optional;
    }

    public boolean existsByISBN(String isbn) {
        return this.books.stream().anyMatch(b -> b.getISBN().equals(isbn));
    }

    public void addBook(Book book) {
        if(existsByISBN(book.getISBN())) {
            throw new BookAlreadyExistsException(book.getISBN());
        }
        books.add(book);
    }

    public void addBooksBulk(List<Book> books) {
        for (Book book : books) {
            if(existsByISBN(book.getISBN())) {
                throw new BookAlreadyExistsException(book.getISBN());
            }
        }

        for (Book book : books) {
            addBook(book);
        }
    }

    public void removeBook(String isbn) {
        // Would be more efficient with HashTable
        Optional<Book> bookToDelete = this.books.stream().filter(b -> b.getISBN().equals(isbn)).findFirst();
        if(bookToDelete.isEmpty()) {
            throw new BookNotFoundException(isbn);
        }
        books.remove(bookToDelete.get());
    }

    public void removeBooksBulk(List<String> isbnList) {
        for (String isbn : isbnList) {
            if (!existsByISBN(isbn)) {
                throw new BookNotFoundException(isbn);
            }
        }
        for (String isbn : isbnList) {
            removeBook(isbn);
        }
    }

    public void updateBook(Book book) {
        Optional<Book> foundBook = this.books.stream().filter(b -> b.getISBN().equals(book.getISBN()))
                .findFirst();
        if (foundBook.isEmpty()) {
            throw new BookNotFoundException(book.getISBN());
        }
        Book bookToUpdate = foundBook.get();
        bookToUpdate.setTitle(book.getTitle());
        bookToUpdate.setAuthor(book.getAuthor());
        bookToUpdate.setPages(book.getPages());
    }

    public void updateBooksBulk(List<Book> books) {
        for (Book book : books) {
            if (!existsByISBN(book.getISBN())) {
                throw new BookNotFoundException(book.getISBN());
            }
        }
        for (Book book : books) {
            updateBook(book);
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

        for (int i = 0; i < titles.length; i++) {
            String randomISBN = ISBNs[ThreadLocalRandom.current().nextInt(ISBNs.length)];
            String randomTitle = titles[ThreadLocalRandom.current().nextInt(titles.length)];
            String randomAuthor = authors[ThreadLocalRandom.current().nextInt(authors.length)];
            int randomPages = pages[ThreadLocalRandom.current().nextInt(pages.length)];

            Book book = new Book(randomISBN, randomTitle, randomAuthor, randomPages);
            books.add(book);
        }
        return books;
    }
}
