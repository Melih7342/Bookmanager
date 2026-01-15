package models;

import java.util.ArrayList;
import java.util.List;

public class Book {
    private String ISBN;
    private String title;
    private String author;
    private int pages;

    public Book(String ISBN, String title, String author, int pages) {
        this.ISBN = ISBN;
        this.title = title;
        this.author = author;
        this.pages = pages;
    }

    // Getters
    public String getISBN() {
        return ISBN;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public int getPages() {
        return pages;
    }

    // Setters
    public void setISBN(String ISBN) {
        this.ISBN = ISBN;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setPages(int pages) {
        this.pages = pages;
    }

    // For a quick overview
    public String toString() {
        return "ISBN: " + ISBN + "\nTitle: " + title + "\nAuthor: " + author + "\nPages: " + pages;
    }

    // Generate a list of Dummy-Books for testing purposes
    public List<Book> generateDummyBooks() {
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
        return books;
    }
}
