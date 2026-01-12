package com.onbok.book_hub.book.domain.repository.search;

import com.onbok.book_hub.book.domain.model.search.SearchKeyword;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SearchKeywordRepository extends JpaRepository<SearchKeyword, Long> {
    /**
     * 검색어로 조회
     */
    Optional<SearchKeyword> findByKeyword(String keyword);

    /**
     * 인기 검색어 top N 조회 (검색 횟수 기준 내림차순)
     */
    List<SearchKeyword> findTop10ByOrderBySearchCountDesc();
}
