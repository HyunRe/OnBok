package com.onbok.book_hub.recommendation.dto;

import com.onbok.book_hub.book.domain.model.book.Book;
import com.onbok.book_hub.recommendation.model.RecommendationType;

import java.util.List;

public record PersonalizedRecommendationResponseDto(RecommendationType type, List<Book> orderBased, List<Book> reviewBased, List<Book> collaborative) {
}
