package com.example.library.controller;

import com.example.library.model.Loan;
import com.example.library.model.Book;
import com.example.library.repository.LoanRepository;
import com.example.library.repository.BookRepository;
import org.springframework.web.bind.annotation.*;

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
}
