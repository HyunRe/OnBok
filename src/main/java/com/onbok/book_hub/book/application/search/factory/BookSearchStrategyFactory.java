package com.onbok.book_hub.book.application.search.factory;

import com.onbok.book_hub.book.application.search.strategy.*;
import com.onbok.book_hub.book.domain.model.book.Book;
import com.onbok.book_hub.book.domain.repository.book.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Factory Pattern - 검색 전략 팩토리
 */
@Component
@RequiredArgsConstructor
public class BookSearchStrategyFactory {
    private final BookRepository bookRepository;

    public SearchStrategy<Book> getStrategy(String field) {
        return switch (field.toLowerCase()) {
            case "author" -> new AuthorSearchStrategy(bookRepository);
            case "company" -> new CompanySearchStrategy(bookRepository);
            case "summary" -> new SummarySearchStrategy(bookRepository);
            default -> new TitleSearchStrategy(bookRepository);
        };
    }
}
