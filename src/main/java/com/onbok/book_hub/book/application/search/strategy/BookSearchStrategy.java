package com.onbok.book_hub.book.application.search.strategy;

import com.onbok.book_hub.book.domain.model.book.Book;
import com.onbok.book_hub.book.domain.repository.book.BookRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class BookSearchStrategy implements SearchStrategy<Book> {
    protected final BookRepository bookRepository;
}
