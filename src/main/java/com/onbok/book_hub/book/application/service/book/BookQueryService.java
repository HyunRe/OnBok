package com.onbok.book_hub.book.application.service.book;

import com.onbok.book_hub.book.application.search.factory.BookSearchStrategyFactory;
import com.onbok.book_hub.book.application.search.strategy.SearchStrategy;
import com.onbok.book_hub.book.domain.model.book.Book;
import com.onbok.book_hub.book.domain.repository.book.BookRepository;
import com.onbok.book_hub.book.dto.response.BookListResponseDto;
import com.onbok.book_hub.common.pagination.PaginationInfo;
import com.onbok.book_hub.common.pagination.PaginationUtil;
import com.onbok.book_hub.common.exception.ErrorCode;
import com.onbok.book_hub.common.exception.ExpectedException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * 도서 조회 전용 Service (Query)
 */
@Service
@RequiredArgsConstructor
public class BookQueryService {
    private final BookRepository bookRepository;

    private final BookSearchStrategyFactory bookSearchStrategyFactory;

    /**
     * 페이지네이션된 도서 목록 조회 (페이지 정보 포함)
     */
    public BookListResponseDto getBookListWithPagination(int page, String field, String query) {
        Pageable pageable = PaginationUtil.createDefaultPageable(page);
        SearchStrategy<Book> strategy = bookSearchStrategyFactory.getStrategy(field);
        Page<Book> pagedResult = strategy.search(query, pageable);
        int totalPages = pagedResult.getTotalPages();

        PaginationInfo paginationInfo = PaginationUtil.calculatePagination(page, totalPages);

        return new BookListResponseDto(pagedResult.getContent(), paginationInfo);
    }

    /**
     * 도서 ID로 도서 조회
     */
    public Book findById(Long id) {
        return bookRepository.findById(id).orElseThrow(() -> new ExpectedException(ErrorCode.BOOK_NOT_FOUND));
    }
}
