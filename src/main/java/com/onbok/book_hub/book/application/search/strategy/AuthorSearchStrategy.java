package com.onbok.book_hub.book.application.search.strategy;

import com.onbok.book_hub.book.domain.model.book.Book;
import com.onbok.book_hub.book.domain.repository.book.BookRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public class AuthorSearchStrategy extends BookSearchStrategy {
    public AuthorSearchStrategy(BookRepository bookRepository) {
        super(bookRepository);
    }

    @Override
    public Page<Book> search(String query, Pageable pageable) {
        return bookRepository.findByAuthorContaining(query, pageable);
    }
}
