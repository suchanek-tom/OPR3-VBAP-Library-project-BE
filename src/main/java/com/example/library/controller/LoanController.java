package com.example.library.controller;

import com.example.library.model.Loan;
import com.example.library.model.Book;
import com.example.library.model.User;
import com.example.library.dto.BorrowLoanRequest;
import com.example.library.repository.LoanRepository;
import com.example.library.repository.BookRepository;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.access.prepost.PreAuthorize;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/loans")
@CrossOrigin(origins = "*")
public class LoanController {
    private static final Logger logger = LoggerFactory.getLogger(LoanController.class);
    private final LoanRepository loanRepo;
    private final BookRepository bookRepo;

    public LoanController(LoanRepository loanRepo, BookRepository bookRepo) {
        this.loanRepo = loanRepo;
        this.bookRepo = bookRepo;
    }

    @GetMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<List<Loan>> all() {
        logger.info("GET request: Admin fetching all loans");
        try {
            List<Loan> loans = loanRepo.findAll();
            logger.info("Successfully retrieved {} loans", loans.size());
            return ResponseEntity.ok(loans);
        } catch (Exception e) {
            logger.error("Error retrieving all loans", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to retrieve loans");
        }
    }

    /**
     * Borrow a book - creates a new loan and marks book as unavailable
     */
    @PostMapping("/borrow")
    public ResponseEntity<Loan> borrow(@Valid @RequestBody BorrowLoanRequest borrowRequest) {
        logger.info("POST request: Borrowing book - User: {}, Book: {}",
                borrowRequest != null ? borrowRequest.getUserId() : null,
                borrowRequest != null ? borrowRequest.getBookId() : null);
        try {
            if (borrowRequest == null) {
                logger.warn("Borrow request is null");
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Borrow request is required");
            }

            if (borrowRequest.getUserId() == null || borrowRequest.getUserId() <= 0) {
                logger.warn("Invalid user ID in borrow request: {}", borrowRequest.getUserId());
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Valid user ID is required");
            }

            if (borrowRequest.getBookId() == null || borrowRequest.getBookId() <= 0) {
                logger.warn("Invalid book ID in borrow request: {}", borrowRequest.getBookId());
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Valid book ID is required");
            }

            Book book = bookRepo.findById(borrowRequest.getBookId())
                    .orElseThrow(() -> {
                        logger.warn("Book not found for borrowing: {}", borrowRequest.getBookId());
                        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found");
                    });

            if (!book.isAvailable()) {
                logger.warn("Book not available for borrowing: {} (ID: {})", book.getTitle(), book.getId());
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Book is not available");
            }

            book.setAvailable(false);
            bookRepo.save(book);

            Loan loan = new Loan();
            User user = new User();
            user.setId(borrowRequest.getUserId());

            loan.setUser(user);
            loan.setBook(book);
            loan.setLoanDate(LocalDate.now());
            loan.setStatus("ACTIVE");
            loan.setReturnDate(null);

            Loan savedLoan = loanRepo.save(loan);
            logger.info("Book borrowed successfully - Loan ID: {}, User: {}, Book: {}",
                    savedLoan.getId(), borrowRequest.getUserId(), book.getTitle());
            return new ResponseEntity<>(savedLoan, HttpStatus.CREATED);
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error borrowing book", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to borrow book");
        }
    }

    /**
     * Return a loan - marks loan as returned and makes book available
     */
    @PostMapping("/return/{id}")
    public ResponseEntity<Loan> returnLoan(@PathVariable Integer id) {
        logger.info("POST request: Returning loan with id: {}", id);
        try {
            if (id == null || id <= 0) {
                logger.warn("Invalid loan ID for return: {}", id);
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid loan ID");
            }

            Loan loan = loanRepo.findById(id)
                    .orElseThrow(() -> {
                        logger.warn("Loan not found for return with id: {}", id);
                        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Loan not found");
                    });
            if ("RETURNED".equalsIgnoreCase(loan.getStatus())) {
                logger.warn("Loan already returned - Loan ID: {}", id);
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Loan is already returned");
            }

            loan.setReturnDate(LocalDate.now());
            loan.setStatus("RETURNED");

            Book book = loan.getBook();
            if (book != null) {
                book.setAvailable(true);
                bookRepo.save(book);
            }

            Loan savedLoan = loanRepo.save(loan);
            logger.info("Loan returned successfully - Loan ID: {}, Book: {}", id,
                    book != null ? book.getTitle() : "unknown");
            return ResponseEntity.ok(savedLoan);
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error returning loan with id: {}", id, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to return loan");
        }
    }

    // GET loan by ID
    @GetMapping("/{id}")
    public ResponseEntity<Loan> getOne(@PathVariable Integer id) {
        logger.info("GET request: Fetching loan with id: {}", id);
        try {
            if (id == null || id <= 0) {
                logger.warn("Invalid loan ID: {}", id);
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid loan ID");
            }

            Loan loan = loanRepo.findById(id)
                    .orElseThrow(() -> {
                        logger.warn("Loan not found with id: {}", id);
                        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Loan not found");
                    });
            logger.info("Successfully retrieved loan with id: {}", id);
            return ResponseEntity.ok(loan);
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error retrieving loan with id: {}", id, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to retrieve loan");
        }
    }

    // PUT update loan (updates dates/status). Adjusts book availability when status
    // changes.
    @PutMapping("/{id}")
    public ResponseEntity<Loan> update(@PathVariable Integer id, @Valid @RequestBody Loan updated) {
        try {
            if (id == null || id <= 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid loan ID");
            }
            if (updated == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Loan data is required");
            }

            Loan loan = loanRepo.findById(id)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Loan not found"));

            String previousStatus = loan.getStatus();

            if (updated.getLoanDate() != null) {
                loan.setLoanDate(updated.getLoanDate());
            }

            if (updated.getReturnDate() != null) {
                loan.setReturnDate(updated.getReturnDate());
            }

            if (updated.getStatus() != null && !updated.getStatus().isBlank()) {
                loan.setStatus(updated.getStatus());
            }

            if (previousStatus != null && !previousStatus.equals(updated.getStatus())) {
                Book book = loan.getBook();
                if (book == null) {
                    throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Associated book not found");
                }
                if ("RETURNED".equalsIgnoreCase(updated.getStatus())) {
                    book.setAvailable(true);
                    bookRepo.save(book);
                } else if ("ACTIVE".equalsIgnoreCase(updated.getStatus())) {
                    if (book.isAvailable()) {
                        book.setAvailable(false);
                        bookRepo.save(book);
                    }
                }
            }

            Loan saved = loanRepo.save(loan);
            return ResponseEntity.ok(saved);
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to update loan");
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        try {
            if (id == null || id <= 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid loan ID");
            }

            Loan loan = loanRepo.findById(id)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Loan not found"));

            if (loan.getStatus() != null && loan.getStatus().equalsIgnoreCase("ACTIVE")) {
                Book book = loan.getBook();
                if (book != null) {
                    book.setAvailable(true);
                    bookRepo.save(book);
                }
            }

            loanRepo.deleteById(id);
            return ResponseEntity.noContent().build();
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to delete loan");
        }
    }
}
