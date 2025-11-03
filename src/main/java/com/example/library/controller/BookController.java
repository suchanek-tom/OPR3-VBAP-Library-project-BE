package com.example.library.controller;

import com.example.library.model.Book;
import com.example.library.repository.BookRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/books")
@CrossOrigin(origins = "*")
public class BookController {
    private final BookRepository repo;

    public BookController(BookRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public List<Book> getAll() {
        return repo.findAll();
    }

    @GetMapping("/{id}")
    public Book getOne(@PathVariable Integer id) {
        return repo.findById(id).orElseThrow(() -> new RuntimeException("Book not found"));
    }

    @PostMapping
    public Book create(@RequestBody Book book) {
        return repo.save(book);
    }

    @PutMapping("/{id}")
    public Book update(@PathVariable Integer id, @RequestBody Book updated) {
        Book b = repo.findById(id).orElseThrow();
        b.setTitle(updated.getTitle());
        b.setAuthor(updated.getAuthor());
        b.setContent(updated.getContent());
        b.setYear(updated.getYear());
        b.setIsbn(updated.getIsbn());
        b.setAvailable(updated.isAvailable());
        return repo.save(b);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) {
        repo.deleteById(id);
    }
}
