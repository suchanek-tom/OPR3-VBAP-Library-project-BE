package com.example.library.service;

import com.example.library.exception.AuthorNotFoundException;
import com.example.library.model.Author;
import com.example.library.repository.AuthorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class AuthorService {
    private final AuthorRepository authorRepository;

    public AuthorService(AuthorRepository authorRepository) {
        this.authorRepository = authorRepository;
    }

    public List<Author> getAllAuthors() {
        return authorRepository.findAll();
    }

    public Optional<Author> getAuthorById(Integer id) {
        return authorRepository.findById(id);
    }

    public List<Author> searchAuthorsByName(String name) {
        return authorRepository.findByName(name);
    }

    public List<Author> getAuthorsByNationality(String nationality) {
        return authorRepository.findByNationality(nationality);
    }

    @Transactional
    public Author createAuthor(Author author) {
        author.setId(null); // Ensure we're creating a new author
        return authorRepository.save(author);
    }

    @Transactional
    public List<Author> createAuthors(List<Author> authors) {
        authors.forEach(author -> author.setId(null));
        return authorRepository.saveAll(authors);
    }

    @Transactional
    public Author updateAuthor(Integer id, Author authorDetails) {
        Author author = authorRepository.findById(id)
                .orElseThrow(() -> new AuthorNotFoundException(id));

        author.setFirstName(authorDetails.getFirstName());
        author.setLastName(authorDetails.getLastName());
        author.setBiography(authorDetails.getBiography());
        author.setNationality(authorDetails.getNationality());

        return authorRepository.save(author);
    }

    @Transactional
    public void deleteAuthor(Integer id) {
        Author author = authorRepository.findById(id)
                .orElseThrow(() -> new AuthorNotFoundException(id));
        authorRepository.delete(author);
    }
}
