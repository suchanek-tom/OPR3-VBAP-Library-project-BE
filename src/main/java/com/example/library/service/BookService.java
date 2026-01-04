package com.example.library.service;

import com.example.library.model.Author;
import com.example.library.model.Book;
import com.example.library.repository.BookRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class BookService {
    private static final Logger logger = LoggerFactory.getLogger(BookService.class);
    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    /**
     * Get all books from the database with pagination
     */
    public Page<Book> getAllBooks(Pageable pageable) {
        logger.debug("Service: Fetching books with pagination - page: {}, size: {}", 
                     pageable.getPageNumber(), pageable.getPageSize());
        Page<Book> books = bookRepository.findAll(pageable);
        logger.debug("Service: Retrieved {} books out of {} total", 
                     books.getNumberOfElements(), books.getTotalElements());
        return books;
    }

    /**
     * Get a single book by ID
     */
    public Optional<Book> getBookById(Integer id) {
        logger.debug("Service: Fetching book with id: {}", id);
        Optional<Book> book = bookRepository.findById(id);
        if (book.isPresent()) {
            logger.debug("Service: Found book: {}", book.get().getTitle());
        } else {
            logger.debug("Service: Book not found with id: {}", id);
        }
        return book;
    }

    /**
     * Save a new book to the database
     */
    public Book createBook(Book book) {
        logger.info("Service: Creating new book - Title: {}, Author: {}", book.getTitle(), book.getAuthor());
        book.setAvailable(true);
        Book savedBook = bookRepository.save(book);
        logger.info("Service: Book created successfully with ID: {}", savedBook.getId());
        return savedBook;
    }

    /**
     * Save multiple books to the database
     */
    public List<Book> createMultipleBooks(List<Book> books) {
        logger.info("Service: Creating {} books in bulk", books.size());
        books.forEach(book -> book.setAvailable(true));
        List<Book> savedBooks = bookRepository.saveAll(books);
        logger.info("Service: {} books created successfully", savedBooks.size());
        return savedBooks;
    }

    /**
     * Update an existing book in the database
     */
    public Book updateBook(Integer id, Book updatedBook) {
        logger.info("Service: Updating book with id: {}", id);
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Service: Book not found for update with id: {}", id);
                    return new IllegalArgumentException("Book with id " + id + " not found");
                });

        book.setTitle(updatedBook.getTitle());
        book.setAuthor(updatedBook.getAuthor());
        book.setContent(updatedBook.getContent());
        book.setPublicationYear(updatedBook.getPublicationYear());
        book.setIsbn(updatedBook.getIsbn());
        book.setAvailable(updatedBook.isAvailable());
        
        // Update authors relationship
        if (updatedBook.getAuthors() != null && !updatedBook.getAuthors().isEmpty()) {
            Set<Author> newAuthors = new HashSet<>(updatedBook.getAuthors());
            book.setAuthors(newAuthors);
        }

        Book savedBook = bookRepository.save(book);
        logger.info("Service: Book updated successfully with id: {}", id);
        return savedBook;
    }

    /**
     * Delete a book from the database
     */
    public void deleteBook(Integer id) {
        logger.info("Service: Deleting book with id: {}", id);
        if (!bookRepository.existsById(id)) {
            logger.warn("Service: Book not found for deletion with id: {}", id);
            throw new IllegalArgumentException("Book with id " + id + " not found");
        }
        bookRepository.deleteById(id);
        logger.info("Service: Book deleted successfully with id: {}", id);
    }

    /**
     * Check if a book exists
     */
    public boolean bookExists(Integer id) {
        return bookRepository.existsById(id);
    }
}
