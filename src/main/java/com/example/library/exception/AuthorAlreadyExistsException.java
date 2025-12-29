package com.example.library.exception;

public class AuthorAlreadyExistsException extends RuntimeException {
    public AuthorAlreadyExistsException(String message) {
        super(message);
    }

    public AuthorAlreadyExistsException(String firstName, String lastName) {
        super("Author already exists: " + firstName + " " + lastName);
    }
}
