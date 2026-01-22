package com.melih.bookmanager.service;

import com.melih.bookmanager.api.model.Book;
import static com.melih.bookmanager.service.BookService.generateDummyBooks;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class BookTests {
    private BookService bookService;

    @BeforeEach
    void setUp() {
        // Initial books for test cases
        List<Book> books = new ArrayList<>(List.of(
                new Book("978-3-16-148410-0", "Der Wind am Ende der Welt", "Franz Kafka", 300),
                new Book("978-0-545-01022-1", "Das Schweigen der alten Eiche", "Hermann Hesse", 350),
                new Book("978-1-86197-876-9", "Hafen der verlorenen Träume", "Haruki Murakami", 400),
                new Book("978-3-8273-1542-8", "Die mechanische Rose: Ein Steampunk-Abenteuer", "Albert Camus", 508),
                new Book("978-0-306-40615-7", "Letzter Halt: Wien Hauptbahnhof", "Friedrich Nietzsche", 958)
        ));
        this.bookService = new BookService(books);
    }

    @Test
    void givenExistingIsbn_whenGetBookByISBN_thenReturnsCorrectBook() {
        // GIVEN
        String isbn = "978-3-16-148410-0";

        // WHEN
        Optional<Book> result = bookService.getBookByISBN(isbn);

        // THEN
        assertThat(result.isPresent()).isTrue();

        Book bookToFind = new Book("978-3-16-148410-0", "Der Wind am Ende der Welt", "Franz Kafka", 300);
        Book foundBook = result.get();

        assertThat(bookToFind).isEqualTo(foundBook);
    }

    @Test
    void givenNonExistentIsbn_whenGetBookByISBN_thenReturnsEmptyOptional() {
        // GIVEN
        String nonExistentIsbn = "999-3-16-148410-5";

        // WHEN
        Optional<Book> result = bookService.getBookByISBN(nonExistentIsbn);

        // THEN
        assertThat(result.isPresent()).isFalse();
    }

    @Test
    void givenExistingIsbn_whenCheckingExistence_thenReturnsTrue() {
        // GIVEN
        String existingIsbn = "978-3-16-148410-0";

        // WHEN
        boolean exists = bookService.existsByISBN(existingIsbn);

        // THEN
        assertThat(exists).isTrue();
    }

    @Test
    void givenNonExistentIsbn_whenCheckingExistence_thenReturnsFalse() {
        // GIVEN
        String nonExistingIsbn = "999-3-16-148410-5";

        // WHEN
        boolean exists = bookService.existsByISBN(nonExistingIsbn);

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
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Book already exists")
                .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                .isEqualTo(HttpStatus.CONFLICT);
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
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("exists")
                .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                .isEqualTo(HttpStatus.CONFLICT);
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
                .isInstanceOf(ResponseStatusException.class);

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
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Book not found")
                .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void givenNotExistingIsbn_whenRemoveBook_thenNoBookIsRemoved() {
        // GIVEN
        String nonExistingIsbn = "999";
        int initialSize = bookService.getAllBooks().size();

        // WHEN & THEN
        // Catch the exception, so we can check, if the initial size hasn't changed
       assertThatThrownBy(() -> bookService.removeBook(nonExistingIsbn))
               .isInstanceOf(ResponseStatusException.class);

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
                .isInstanceOf(ResponseStatusException.class);

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
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("not found")
                .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void givenBookWithExistingIsbn_whenUpdateBook_thenBookIsUpdated() {
        // GIVEN
        Book updateData = new Book("978-3-16-148410-0", "New title", "New author", 240);

        // WHEN
        bookService.updateBook(updateData);
        Book result = bookService.getBookByISBN(updateData.getISBN()).get();

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
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("not found")
                .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void givenBookWithNonExistingIsbn_whenUpdateBook_thenSizeDoesNotChange() {
        // GIVEN
        Book updateData = new Book("999", "New title", "New author", 123);
        int initialSize = bookService.getAllBooks().size();

        // WHEN & THEN
        assertThatThrownBy(() -> bookService.updateBook(updateData))
                .isInstanceOf(ResponseStatusException.class);

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
        Book resultOne = bookService.getBookByISBN(updateData.get(0).getISBN()).get();
        Book resultTwo = bookService.getBookByISBN(updateData.get(1).getISBN()).get();

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
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("not found")
                .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void givenBooksWithOneNonExistingIsbn_whenUpdateBulk_thenNoBooksAreUpdated() {
        // GIVEN
        String existingIsbn = "978-3-16-148410-0";
        Book originalBook = bookService.getBookByISBN(existingIsbn).get();

        List<Book> updateData = new ArrayList<>(List.of(
                new Book(existingIsbn, "New Title", "New Author", 301),
                new Book("999", "I don't exist", "Someone", 100)
        ));

        // WHEN & THEN
        assertThatThrownBy(() -> bookService.updateBooksBulk(updateData))
                .isInstanceOf(ResponseStatusException.class);

        Optional<Book> resultOne = bookService.getBookByISBN(existingIsbn);
        assertThat(resultOne).isPresent();
        assertThat(resultOne.get()).usingRecursiveComparison().isEqualTo(originalBook);

        Optional<Book> resultTwo = bookService.getBookByISBN("999");
        assertThat(resultTwo).isEmpty();
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