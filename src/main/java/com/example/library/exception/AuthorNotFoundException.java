package com.example.library.exception;

public class AuthorNotFoundException extends RuntimeException {
    public AuthorNotFoundException(Integer id) {
        super("Author not found with id: " + id);
    }

    public AuthorNotFoundException(String message) {
        super(message);
    }
}
