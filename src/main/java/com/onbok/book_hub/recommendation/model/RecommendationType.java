package com.onbok.book_hub.recommendation.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RecommendationType {
    ORDER_BASED("order-based", "주문 내역 기반 추천"),
    REVIEW_BASED("review-based", "리뷰 기반 추천"),
    COLLABORATIVE("collaborative-filtering", "협업 필터링 추천"),
    POPULAR("popular", "인기 도서 추천"),
    HIGHLY_RATED("highly-rated", "평점 높은 도서 추천"),
    PERSONALIZED_MIXED("personalized-mixed", "개인화 종합 추천");

    private final String code;
    private final String description;
}
