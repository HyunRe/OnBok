package com.onbok.book_hub.book.application.service.book;

import com.onbok.book_hub.book.domain.model.book.Book;
import org.springframework.stereotype.Service;

/**
 * 도서 뷰 관련 비즈니스 로직
 */
@Service
public class BookViewService {
    /**
     * 검색어로 요약 하이라이팅 처리
     * @param book 도서
     * @param query 검색어
     * @return 하이라이팅 처리된 도서
     */
    public Book highlightSummary(Book book, String query) {
        if (query == null || query.isEmpty()) {
            return book;
        }

        String highlightedSummary = book.getSummary()
                .replaceAll(query, "<span style='background-color: skyblue;'>" + query + "</span>");
        book.updateSummary(highlightedSummary);
        return book;
    }
}
