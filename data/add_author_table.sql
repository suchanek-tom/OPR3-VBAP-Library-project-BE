-- Create Author table
CREATE TABLE IF NOT EXISTS author (
    id INT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    biography TEXT,
    nationality VARCHAR(100)
);

-- Create book_author join table for many-to-many relationship
CREATE TABLE IF NOT EXISTS book_author (
    book_id INT NOT NULL,
    author_id INT NOT NULL,
    PRIMARY KEY (book_id, author_id),
    FOREIGN KEY (book_id) REFERENCES book(id) ON DELETE CASCADE,
    FOREIGN KEY (author_id) REFERENCES author(id) ON DELETE CASCADE
);

-- Add indexes for better query performance
CREATE INDEX idx_author_first_name ON author(first_name);
CREATE INDEX idx_author_last_name ON author(last_name);
CREATE INDEX idx_author_nationality ON author(nationality);
CREATE INDEX idx_book_author_book_id ON book_author(book_id);
CREATE INDEX idx_book_author_author_id ON book_author(author_id);
