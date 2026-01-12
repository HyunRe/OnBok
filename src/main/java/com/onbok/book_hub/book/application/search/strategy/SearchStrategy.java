package com.onbok.book_hub.book.application.search.strategy;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Strategy Pattern - 검색 전략 인터페이스
 * @param <T> 검색 결과 엔티티 타입
 */
public interface SearchStrategy<T> {
    /**
     * 검색 실행
     * @param query 검색어
     * @param pageable 페이지 정보
     * @return 검색 결과 페이지
     */
    Page<T> search(String query, Pageable pageable);
}
