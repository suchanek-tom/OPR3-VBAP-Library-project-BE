package com.example.library.service;

import com.example.library.model.Book;
import com.example.library.repository.BookRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class BookService {

    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    /**
     * Get all books from the database
     */
    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    /**
     * Get a single book by ID
     */
    public Optional<Book> getBookById(Integer id) {
        return bookRepository.findById(id);
    }

    /**
     * Save a new book to the database
     */
    public Book createBook(Book book) {
        book.setAvailable(true);
        return bookRepository.save(book);
    }

    /**
     * Save multiple books to the database
     */
    public List<Book> createMultipleBooks(List<Book> books) {
        books.forEach(book -> book.setAvailable(true));
        return bookRepository.saveAll(books);
    }

    /**
     * Update an existing book in the database
     */
    public Book updateBook(Integer id, Book updatedBook) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Book with id " + id + " not found"));

        book.setTitle(updatedBook.getTitle());
        book.setAuthor(updatedBook.getAuthor());
        book.setContent(updatedBook.getContent());
        book.setPublicationYear(updatedBook.getPublicationYear());
        book.setIsbn(updatedBook.getIsbn());
        book.setAvailable(updatedBook.isAvailable());

        return bookRepository.save(book);
    }

    /**
     * Delete a book from the database
     */
    public void deleteBook(Integer id) {
        if (!bookRepository.existsById(id)) {
            throw new IllegalArgumentException("Book with id " + id + " not found");
        }
        bookRepository.deleteById(id);
    }

    /**
     * Check if a book exists
     */
    public boolean bookExists(Integer id) {
        return bookRepository.existsById(id);
    }
}
