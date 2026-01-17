package service;

import model.Book;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BookService {
    private List<Book> books = new ArrayList<Book>();

    // Default constructor
    public BookService() {}

    // Constructor with instant enrichment
    public BookService(List<Book> books) {
        this.books = books;
    }

    public List<Book> getBooks() {
        return books;
    }
    public boolean addBook(Book book) {
        boolean exists = this.books.stream().anyMatch(b -> b.getISBN().equals(book.getISBN()));

        if (exists) {
            return false;
        }
        books.add(book);
        return true;
    }
    public boolean removeBook(Book book) {
        boolean exists = this.books.stream().anyMatch(b -> b.getISBN().equals(book.getISBN()));
        if (!exists) {
            return false;
        }
        books.remove(book);
        return true;
    }
    public boolean updateBook(Book book) {
        Optional<Book> foundBook = this.books.stream().filter(b -> b.getISBN().equals(book.getISBN()))
                .findFirst();
        if (foundBook.isEmpty()) {
            return false;
        }
        Book bookToUpdate = foundBook.get();
        bookToUpdate.setTitle(book.getTitle());
        bookToUpdate.setAuthor(book.getAuthor());
        bookToUpdate.setPages(book.getPages());
        return true;
    }
}
