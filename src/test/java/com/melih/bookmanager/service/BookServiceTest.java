package com.melih.bookmanager.service;

import com.melih.bookmanager.api.model.Book;
import static com.melih.bookmanager.service.BookService.generateDummyBooks;

import com.melih.bookmanager.exception.Book.BookAlreadyExistsException;
import com.melih.bookmanager.exception.Book.BookNotFoundException;
import com.melih.bookmanager.repository.book.InMemoryBookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class BookServiceTest {
    private BookService bookService;
    private InMemoryBookRepository bookRepository;

    @BeforeEach
    void setUp() {
        // Initialize repository and service
        this.bookRepository = new InMemoryBookRepository();
        // Initial books for test cases
        List<Book> books = new ArrayList<>(List.of(
                new Book("978-3-16-148410-0", "Der Wind am Ende der Welt", "Franz Kafka", 300),
                new Book("978-0-545-01022-1", "Das Schweigen der alten Eiche", "Hermann Hesse", 350),
                new Book("978-1-86197-876-9", "Hafen der verlorenen Träume", "Haruki Murakami", 400),
                new Book("978-3-8273-1542-8", "Die mechanische Rose: Ein Steampunk-Abenteuer", "Albert Camus", 508),
                new Book("978-0-306-40615-7", "Letzter Halt: Wien Hauptbahnhof", "Friedrich Nietzsche", 958)
        ));
        for (Book book : books) {
            this.bookRepository.save(book);
        }
        this.bookService = new BookService(bookRepository);
    }

    @Test
    void givenExistingIsbn_whenGetBookByISBN_thenReturnsCorrectBook() {
        // GIVEN
        String isbn = "978-3-16-148410-0";

        // WHEN
        Book result = bookService.getBookByIsbn(isbn);

        // THEN
        Book bookToFind = new Book("978-3-16-148410-0", "Der Wind am Ende der Welt", "Franz Kafka", 300);
        assertThat(bookToFind).isEqualTo(result);
    }

    @Test
    void givenNonExistentIsbn_whenGetBookByISBN_thenThrowsException() {
        // GIVEN
        String nonExistentIsbn = "999";

        // WHEN & THEN
        assertThatThrownBy(() -> bookService.getBookByIsbn(nonExistentIsbn))
                .isInstanceOf(BookNotFoundException.class);
    }

    @Test
    void givenExistingIsbn_whenCheckingExistence_thenReturnsTrue() {
        // GIVEN
        String existingIsbn = "978-3-16-148410-0";

        // WHEN
        boolean exists = bookRepository.existsByISBN(existingIsbn);

        // THEN
        assertThat(exists).isTrue();
    }

    @Test
    void givenNonExistentIsbn_whenCheckingExistence_thenReturnsFalse() {
        // GIVEN
        String nonExistingIsbn = "999-3-16-148410-5";

        // WHEN
        boolean exists = bookRepository.existsByISBN(nonExistingIsbn);

        // THEN
        assertThat(exists).isFalse();
    }

    @Test
    void givenNewBook_whenAddingBook_thenBookIsPersistedAndSizeIncreases() {
        // GIVEN
        int initialSize = bookService.getAllBooks().size();
        Book newBook = new Book("999-3-16-148410-5", "Faust", "Goethe", 92);

        // WHEN
        bookService.addBook(newBook);

        // THEN
        List<Book> books = bookService.getAllBooks();
        assertThat(books)
                .contains(newBook)
                .hasSize(initialSize + 1);
    }

    @Test
    void givenDuplicateIsbn_whenAddingBook_thenThrowsConflictException() {
        // GIVEN
        Book existingBook = new Book("978-3-16-148410-0", "Der Wind am Ende der Welt", "Franz Kafka", 300);

        // WHEN & THEN
        assertThatThrownBy(() -> bookService.addBook(existingBook))
                .isInstanceOf(BookAlreadyExistsException.class);
    }

    @Test
    void givenMultipleNewBooks_whenAddingBulk_thenAllBooksArePersisted() {
        // GIVEN
        List<Book> newBooks = new ArrayList<>(List.of(
                new Book("999", "Schirach", "Terror", 255),
                new Book("888", "Haruki Murakami", "Kafka on the shore", 987)
        ));
        int initialSize = bookService.getAllBooks().size();
        int newBooksSize = newBooks.size();

        // WHEN
        bookService.addBooksBulk(newBooks);
        List<Book> books = bookService.getAllBooks();

        //THEN
        assertThat(books)
                .containsAll(newBooks)
                .hasSize(initialSize + newBooksSize);
    }

    @Test
    void givenOneExistingBook_whenAddingBulk_thenExceptionIsThrown() {
        // GIVEN
        Book existingBook = new Book("978-3-16-148410-0", "Der Wind am Ende der Welt", "Franz Kafka", 300);
        Book nonExistingBook = new Book("888", "Haruki Murakami", "Kafka on the shore", 987);

        List<Book> toBeAddedBooks = new ArrayList<>(List.of(existingBook, nonExistingBook));

        // WHEN & THEN
        assertThatThrownBy(() -> bookService.addBooksBulk(toBeAddedBooks))
                .isInstanceOf(BookAlreadyExistsException.class);
    }
    @Test
    void givenOneExistingBook_whenAddingBulk_thenNoBooksArePersisted() {
        // GIVEN
        Book existingBook = new Book("978-3-16-148410-0", "Der Wind am Ende der Welt", "Franz Kafka", 300);
        Book nonExistingBook = new Book("888", "Haruki Murakami", "Kafka on the shore", 987);
        List<Book> toBeAddedBooks = new ArrayList<>(List.of(nonExistingBook, existingBook));
        int initialSize = bookService.getAllBooks().size();

        // WHEN & THEN: Catch the exception
        assertThatThrownBy(() -> bookService.addBooksBulk(toBeAddedBooks))
                .isInstanceOf(BookAlreadyExistsException.class);

        // THEN: Check if no books were added
        List<Book> booksAfterFailedAction = bookService.getAllBooks();
        assertThat(booksAfterFailedAction).hasSize(initialSize);
    }

    @Test
    void givenExistingIsbn_whenRemoveBook_thenBookIsRemoved() {
        // GIVEN
        String existingIsbn = "978-3-16-148410-0";
        int initialSize = bookService.getAllBooks().size();

        // WHEN
        bookService.removeBook(existingIsbn);
        List<Book> books = bookService.getAllBooks();

        // THEN
        assertThat(books)
                .noneMatch(i -> i.getISBN().equals(existingIsbn))
                .hasSize(initialSize - 1);
    }

    @Test
    void givenNotExistingIsbn_whenRemoveBook_thenExceptionIsThrown() {
        // GIVEN
        String nonExistingIsbn = "999";

        // WHEN & THEN
        assertThatThrownBy(() -> bookService.removeBook(nonExistingIsbn))
                .isInstanceOf(BookNotFoundException.class);
    }

    @Test
    void givenNotExistingIsbn_whenRemoveBook_thenNoBookIsRemoved() {
        // GIVEN
        String nonExistingIsbn = "999";
        int initialSize = bookService.getAllBooks().size();

        // WHEN & THEN
        // Catch the exception, so we can check, if the initial size hasn't changed
       assertThatThrownBy(() -> bookService.removeBook(nonExistingIsbn))
               .isInstanceOf(BookNotFoundException.class);

       List<Book> books = bookService.getAllBooks();

       assertThat(books).hasSize(initialSize);
    }

    @Test
    void givenBulkOfExistingIsbns_whenRemoveBulk_thenBulkIsRemoved() {
        // GIVEN
        List<String> existingIsbns = new ArrayList<>(List.of("978-3-16-148410-0", "978-0-545-01022-1"));
        int initialSize = bookService.getAllBooks().size();

        // WHEN
        bookService.removeBooksBulk(existingIsbns);
        List<Book> booksAfterRemoving = bookService.getAllBooks();

        // THEN
        assertThat(booksAfterRemoving)
                .extracting(Book::getISBN)
                .doesNotContainAnyElementsOf(existingIsbns)
                .hasSize(initialSize - existingIsbns.size());
    }

    @Test
    void givenNoneExistingIsbn_whenRemoveBulk_thenNoBookIsRemoved() {
        // GIVEN
        String existingIsbn = "978-3-16-148410-0";
        String nonExistingIsbn = "999";
        List<String> mixedIsbns = new ArrayList<>(List.of(existingIsbn, nonExistingIsbn));
        int initialSize = bookService.getAllBooks().size();

        // WHEN & THEN
        assertThatThrownBy(() -> bookService.removeBooksBulk(mixedIsbns))
                .isInstanceOf(BookNotFoundException.class);

        List<Book> booksAfterRemoving = bookService.getAllBooks();

        assertThat(booksAfterRemoving).hasSize(initialSize);
    }

    @Test
    void givenNoneExistingIsbn_whenRemoveBulk_thenExceptionIsThrown() {
        // GIVEN
        String existingIsbn = "978-3-16-148410-0";
        String nonExistingIsbn = "999";
        List<String> isbns = new ArrayList<>(List.of(existingIsbn, nonExistingIsbn));

        // WHEN & THEN
        assertThatThrownBy(() -> bookService.removeBooksBulk(isbns))
                .isInstanceOf(BookNotFoundException.class);
    }

    @Test
    void givenBookWithExistingIsbn_whenUpdateBook_thenBookIsUpdated() {
        // GIVEN
        Book updateData = new Book("978-3-16-148410-0", "New title", "New author", 240);

        // WHEN
        bookService.updateBook(updateData);
        Book result = bookService.getBookByIsbn(updateData.getISBN());

        // THEN
        assertThat(result).usingRecursiveComparison().isEqualTo(updateData);
    }

    @Test
    void givenBookWithExistingIsbn_whenUpdateBook_thenSizeDoesNotChange() {
        // GIVEN
        Book updateData = new Book("978-3-16-148410-0", "New title", "New author", 240);
        int initialSize = bookService.getAllBooks().size();

        // WHEN
        bookService.updateBook(updateData);
        List<Book> books = bookService.getAllBooks();

        // THEN
        assertThat(books).hasSize(initialSize);
    }

    @Test
    void givenBookWithNonExistingIsbn_whenUpdateBook_thenExceptionIsThrown() {
        // GIVEN
        Book updateData = new Book("999", "New title", "New author", 123);

        // WHEN & THEN
        assertThatThrownBy(() -> bookService.updateBook(updateData))
                .isInstanceOf(BookNotFoundException.class);
    }

    @Test
    void givenBookWithNonExistingIsbn_whenUpdateBook_thenSizeDoesNotChange() {
        // GIVEN
        Book updateData = new Book("999", "New title", "New author", 123);
        int initialSize = bookService.getAllBooks().size();

        // WHEN & THEN
        assertThatThrownBy(() -> bookService.updateBook(updateData))
                .isInstanceOf(BookNotFoundException.class);

        List<Book> books = bookService.getAllBooks();
        assertThat(books).hasSize(initialSize);
    }

    @Test
    void givenBooksWithExistingIsbns_whenUpdateBulk_thenBookIsUpdated() {
        // GIVEN
        List<Book> updateData = new ArrayList<>(List.of(
                new Book("978-3-16-148410-0", "New Title", "New Author", 301),
                new Book("978-0-545-01022-1", "New Title", "New Author", 351)
        ));

        // WHEN
        bookService.updateBooksBulk(updateData);
        Book resultOne = bookService.getBookByIsbn(updateData.get(0).getISBN());
        Book resultTwo = bookService.getBookByIsbn(updateData.get(1).getISBN());

        // THEN
        assertThat(resultOne).usingRecursiveComparison().isEqualTo(updateData.get(0));
        assertThat(resultTwo).usingRecursiveComparison().isEqualTo(updateData.get(1));
    }

    @Test
    void givenBooksWithOneNonExistingIsbn_whenUpdateBulk_thenExceptionIsThrown() {
        // GIVEN
        List<Book> updateData = new ArrayList<>(List.of(
                new Book("978-3-16-148410-0", "New Title", "New Author", 301),
                new Book("999", "New Title", "New Author", 351)
        ));

        // WHEN & THEN
        assertThatThrownBy(() -> bookService.updateBooksBulk(updateData))
                .isInstanceOf(BookNotFoundException.class);
    }

    @Test
    void givenBooksWithOneNonExistingIsbn_whenUpdateBulk_thenBookAreUpdated() {
        // GIVEN
        String existingIsbn = "978-3-16-148410-0";
        Book originalBook = bookService.getBookByIsbn(existingIsbn);

        List<Book> updateData = new ArrayList<>(List.of(
                new Book(existingIsbn, "New Title", "New Author", 301),
                new Book("999", "I don't exist", "Someone", 100)
        ));

        // WHEN & THEN
        assertThatThrownBy(() -> bookService.updateBooksBulk(updateData))
                .isInstanceOf(BookNotFoundException.class);

        Book resultOne = bookService.getBookByIsbn(existingIsbn);
        assertThat(resultOne).usingRecursiveComparison().isEqualTo(originalBook);
    }

    @Test
    void givenEmptyBookList_whenGenerateDummies_then10BooksAreGenerated() {
        // GIVEN
        List<Book> books;

        // WHEN
        books = generateDummyBooks();

        // THEN
        assertThat(books).hasSize(10);
    }
}