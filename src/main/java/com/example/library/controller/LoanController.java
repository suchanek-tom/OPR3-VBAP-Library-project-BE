package com.example.library.controller;

import com.example.library.model.Loan;
import com.example.library.model.Book;
import com.example.library.repository.LoanRepository;
import com.example.library.repository.BookRepository;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/loans")
@CrossOrigin(origins = "*")
public class LoanController {
    private final LoanRepository loanRepo;
    private final BookRepository bookRepo;

    public LoanController(LoanRepository loanRepo, BookRepository bookRepo) {
        this.loanRepo = loanRepo;
        this.bookRepo = bookRepo;
    }

    @GetMapping
    public List<Loan> all() { return loanRepo.findAll(); }

    @PostMapping("/borrow")
    public Loan borrow(@RequestBody Loan loan) {
        Book book = bookRepo.findById(loan.getBook().getId()).orElseThrow();
        if (!book.isAvailable()) throw new RuntimeException("Book not available");
        book.setAvailable(false);
        bookRepo.save(book);

        loan.setLoanDate(LocalDate.now());
        loan.setStatus("ACTIVE");
        return loanRepo.save(loan);
    }

    @PostMapping("/return/{id}")
    public Loan returnLoan(@PathVariable Integer id) {
        Loan loan = loanRepo.findById(id).orElseThrow();
        loan.setReturnDate(LocalDate.now());
        loan.setStatus("RETURNED");
        Book book = loan.getBook();
        book.setAvailable(true);
        bookRepo.save(book);
        return loanRepo.save(loan);
    }

    // GET loan by ID
    @GetMapping("/{id}")
    public ResponseEntity<Loan> getOne(@PathVariable Integer id) {
        Loan loan = loanRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Loan not found"));
        return ResponseEntity.ok(loan);
    }

    // PUT update loan (updates dates/status). Adjusts book availability when status changes.
    @PutMapping("/{id}")
    public ResponseEntity<Loan> update(@PathVariable Integer id, @RequestBody Loan updated) {
        Loan loan = loanRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Loan not found"));

        String previousStatus = loan.getStatus();
        loan.setLoanDate(updated.getLoanDate());
        loan.setReturnDate(updated.getReturnDate());
        loan.setStatus(updated.getStatus());

        if (previousStatus != null && !previousStatus.equals(updated.getStatus())) {
            Book book = loan.getBook();
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
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
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
    }
}
