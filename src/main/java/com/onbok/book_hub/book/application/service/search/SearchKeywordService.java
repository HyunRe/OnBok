package com.onbok.book_hub.book.application.service.search;

import com.onbok.book_hub.book.domain.model.search.SearchKeyword;
import com.onbok.book_hub.book.domain.repository.search.SearchKeywordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 검색어 관련 서비스
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SearchKeywordService {
    private final SearchKeywordRepository searchKeywordRepository;

    /**
     * 검색어 저장 및 검색 횟수 증가
     * 이미 존재하는 검색어라면 검색 횟수만 증가
     */
    @Transactional
    public void recordSearch(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return; // 빈 검색어는 저장하지 않음
        }

        String trimmedKeyword = keyword.trim();

        searchKeywordRepository.findByKeyword(trimmedKeyword)
                .ifPresentOrElse(
                        SearchKeyword::increaseSearchCount,
                        () -> {
                            SearchKeyword newKeyword = SearchKeyword.builder()
                                    .keyword(trimmedKeyword)
                                    .searchCount(1L)
                                    .build();
                            searchKeywordRepository.save(newKeyword);
                        }
                );
    }

    /**
     * 인기 검색어 Top 10 조회
     */
    public List<SearchKeyword> getPopularKeywords() {
        return searchKeywordRepository.findTop10ByOrderBySearchCountDesc();
    }
}
