package com.onbok.book_hub.book.domain.model.search;

import com.onbok.book_hub.common.domain.BaseTime;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 인기 검색어를 저장하는 엔티티
 */
@Entity
@Table(name = "search_keywords")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SearchKeyword extends BaseTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String keyword;         // 검색어

    private Long searchCount;       // 검색 횟수

    @Builder
    public SearchKeyword(String keyword, Long searchCount) {
        this.keyword = keyword;
        this.searchCount = searchCount != null ? searchCount : 1L;
    }

    /**
     * 검색 횟수 증가
     */
    public void increaseSearchCount() {
        this.searchCount++;
    }
}
