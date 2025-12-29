package com.example.library.controller;

import com.example.library.model.Book;
import com.example.library.service.BookService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;


//todo pagination, filtering, sorting

@RestController
@RequestMapping("/api/books")
@CrossOrigin(origins = "*")
public class BookController {
    private static final Logger logger = LoggerFactory.getLogger(BookController.class);
    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    // get all books
    @GetMapping
    public ResponseEntity<List<Book>> getAll() {
        logger.info("GET request: Fetching all books");
        try {
            List<Book> books = bookService.getAllBooks();
            logger.info("Successfully retrieved {} books", books.size());
            return ResponseEntity.ok(books);
        } catch (Exception e) {
            logger.error("Error retrieving all books", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to retrieve books");
        }
    }

    // get book by ID
    @GetMapping("/{id}")
    public ResponseEntity<Book> getOne(@PathVariable Integer id) {
        logger.info("GET request: Fetching book with id: {}", id);
        try {
            if (id == null || id <= 0) {
                logger.warn("Invalid book ID: {}", id);
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid book ID");
            }

            Book book = bookService.getBookById(id)
                    .orElseThrow(() -> {
                        logger.warn("Book not found with id: {}", id);
                        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found");
                    });
            logger.info("Successfully retrieved book: {}", book.getTitle());
            return ResponseEntity.ok(book);
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error retrieving book with id: {}", id, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to retrieve book");
        }
    }

    // Create a new book
    @PostMapping
    public ResponseEntity<Book> create(@Valid @RequestBody Book book) {
        logger.info("POST request: Creating new book with title: {}", book != null ? book.getTitle() : "null");
        try {
            if (book == null) {
                logger.warn("Book data is null");
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Book data is required");
            }

            Book savedBook = bookService.createBook(book);
            logger.info("Successfully created book with ID: {}", savedBook.getId());
            return new ResponseEntity<>(savedBook, HttpStatus.CREATED);
        } catch (ResponseStatusException e) {
            throw e;
        } catch (IllegalArgumentException e) {
            logger.error("Invalid book data provided", e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid book data: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Error creating book", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to create book");
        }
    }

    // Create multiple books
    @PostMapping("/bulk")
    public ResponseEntity<List<Book>> createMany(@Valid @RequestBody List<Book> books) {
        logger.info("POST request: Creating {} books in bulk", books != null ? books.size() : 0);
        try {
            if (books == null || books.isEmpty()) {
                logger.warn("Book list is null or empty");
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Book list cannot be empty");
            }

            if (books.size() > 100) {
                logger.warn("Bulk create request exceeded limit: {} books", books.size());
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Maximum 100 books can be created at once");
            }

            List<Book> savedBooks = bookService.createMultipleBooks(books);
            logger.info("Successfully created {} books", savedBooks.size());
            return new ResponseEntity<>(savedBooks, HttpStatus.CREATED);
        } catch (ResponseStatusException e) {
            throw e;
        } catch (IllegalArgumentException e) {
            logger.error("Invalid book data in bulk creation", e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid book data: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Error creating multiple books", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to create books");
        }
    }

    // Update an existing book
    @PutMapping("/{id}")
    public ResponseEntity<Book> update(@PathVariable Integer id, @Valid @RequestBody Book updated) {
        logger.info("PUT request: Updating book with id: {}", id);
        try {
            if (id == null || id <= 0) {
                logger.warn("Invalid book ID: {}", id);
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid book ID");
            }

            if (updated == null) {
                logger.warn("Updated book data is null");
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Book data is required");
            }

            Book savedBook = bookService.updateBook(id, updated);
            logger.info("Successfully updated book with id: {}", id);
            return ResponseEntity.ok(savedBook);
        } catch (ResponseStatusException e) {
            throw e;
        } catch (IllegalArgumentException e) {
            logger.error("Book not found for update with id: {}", id, e);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (Exception e) {
            logger.error("Error updating book with id: {}", id, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to update book");
        }
    }

    // Delete a book
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        logger.info("DELETE request: Deleting book with id: {}", id);
        try {
            if (id == null || id <= 0) {
                logger.warn("Invalid book ID for deletion: {}", id);
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid book ID");
            }

            bookService.deleteBook(id);
            logger.info("Successfully deleted book with id: {}", id);
            return ResponseEntity.noContent().build();
        } catch (ResponseStatusException e) {
            throw e;
        } catch (IllegalArgumentException e) {
            logger.error("Book not found for deletion with id: {}", id, e);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (Exception e) {
            logger.error("Error deleting book with id: {}", id, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to delete book");
        }
    }
}
