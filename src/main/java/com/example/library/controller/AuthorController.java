package com.example.library.controller;

import com.example.library.model.Author;
import com.example.library.service.AuthorService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@RestController
@RequestMapping("/api/authors")
@CrossOrigin(origins = "*")
public class AuthorController {
    private static final Logger logger = LoggerFactory.getLogger(AuthorController.class);
    private final AuthorService authorService;

    public AuthorController(AuthorService authorService) {
        this.authorService = authorService;
    }

    // Get all authors
    @GetMapping
    public ResponseEntity<List<Author>> getAll(@RequestParam(required = false) String name,
            @RequestParam(required = false) String nationality) {
        logger.info("GET request: Fetching authors with filters - name: {}, nationality: {}", name, nationality);
        try {
            List<Author> authors;

            if (name != null && !name.isEmpty()) {
                authors = authorService.searchAuthorsByName(name);
            } else if (nationality != null && !nationality.isEmpty()) {
                authors = authorService.getAuthorsByNationality(nationality);
            } else {
                authors = authorService.getAllAuthors();
            }

            logger.info("Successfully retrieved {} authors", authors.size());
            return ResponseEntity.ok(authors);
        } catch (Exception e) {
            logger.error("Error retrieving authors", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to retrieve authors");
        }
    }

    // Get author by ID
    @GetMapping("/{id}")
    public ResponseEntity<Author> getOne(@PathVariable Integer id) {
        logger.info("GET request: Fetching author with id: {}", id);
        try {
            if (id == null || id <= 0) {
                logger.warn("Invalid author ID: {}", id);
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid author ID");
            }

            Author author = authorService.getAuthorById(id)
                    .orElseThrow(() -> {
                        logger.warn("Author not found with id: {}", id);
                        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Author not found");
                    });
            logger.info("Successfully retrieved author: {}", author.getFullName());
            return ResponseEntity.ok(author);
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error retrieving author with id: {}", id, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to retrieve author");
        }
    }

    // Create a new author
    @PostMapping
    public ResponseEntity<Author> create(@Valid @RequestBody Author author) {
        logger.info("POST request: Creating new author: {} {}",
                author != null ? author.getFirstName() : "null",
                author != null ? author.getLastName() : "null");
        try {
            if (author == null) {
                logger.warn("Author data is null");
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Author data is required");
            }

            Author savedAuthor = authorService.createAuthor(author);
            logger.info("Successfully created author with ID: {}", savedAuthor.getId());
            return new ResponseEntity<>(savedAuthor, HttpStatus.CREATED);
        } catch (ResponseStatusException e) {
            throw e;
        } catch (IllegalArgumentException e) {
            logger.error("Invalid author data provided", e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid author data: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Error creating author", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to create author");
        }
    }

    // Create multiple authors
    @PostMapping("/bulk")
    public ResponseEntity<List<Author>> createMany(@Valid @RequestBody List<Author> authors) {
        logger.info("POST request: Creating {} authors in bulk", authors != null ? authors.size() : 0);
        try {
            if (authors == null || authors.isEmpty()) {
                logger.warn("Author list is null or empty");
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Author list cannot be empty");
            }

            List<Author> savedAuthors = authorService.createAuthors(authors);
            logger.info("Successfully created {} authors", savedAuthors.size());
            return new ResponseEntity<>(savedAuthors, HttpStatus.CREATED);
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error creating authors in bulk", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to create authors");
        }
    }

    // Update an author
    @PutMapping("/{id}")
    public ResponseEntity<Author> update(@PathVariable Integer id, @Valid @RequestBody Author author) {
        logger.info("PUT request: Updating author with id: {}", id);
        try {
            if (id == null || id <= 0) {
                logger.warn("Invalid author ID: {}", id);
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid author ID");
            }

            if (author == null) {
                logger.warn("Author data is null");
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Author data is required");
            }

            Author updatedAuthor = authorService.updateAuthor(id, author);
            logger.info("Successfully updated author with ID: {}", id);
            return ResponseEntity.ok(updatedAuthor);
        } catch (ResponseStatusException e) {
            throw e;
        } catch (RuntimeException e) {
            if (e.getMessage() != null && e.getMessage().contains("not found")) {
                logger.warn("Author not found with id: {}", id);
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
            }
            logger.error("Error updating author with id: {}", id, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to update author");
        } catch (Exception e) {
            logger.error("Error updating author with id: {}", id, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to update author");
        }
    }

    // Delete an author
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        logger.info("DELETE request: Deleting author with id: {}", id);
        try {
            if (id == null || id <= 0) {
                logger.warn("Invalid author ID: {}", id);
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid author ID");
            }

            authorService.deleteAuthor(id);
            logger.info("Successfully deleted author with ID: {}", id);
            return ResponseEntity.noContent().build();
        } catch (ResponseStatusException e) {
            throw e;
        } catch (RuntimeException e) {
            if (e.getMessage() != null && e.getMessage().contains("not found")) {
                logger.warn("Author not found with id: {}", id);
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
            }
            logger.error("Error deleting author with id: {}", id, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to delete author");
        } catch (Exception e) {
            logger.error("Error deleting author with id: {}", id, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to delete author");
        }
    }
}
