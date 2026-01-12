package com.onbok.book_hub.review.application;

import com.onbok.book_hub.common.exception.ErrorCode;
import com.onbok.book_hub.common.exception.ExpectedException;
import com.onbok.book_hub.review.domain.model.Review;
import com.onbok.book_hub.review.domain.repository.ReviewRepository;
import com.onbok.book_hub.review.dto.response.ReviewResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Review Query Service - CQS Pattern
 * 리뷰 조회 작업 담당 (읽기 전용)
 */
@Service
@RequiredArgsConstructor
public class ReviewQueryService {
    private final ReviewRepository reviewRepository;
    private static final int REVIEWS_PER_PAGE = 10;

    public Page<ReviewResponseDto> getReviewsByBook(Long bookId, int page) {
        Pageable pageable = PageRequest.of(page - 1, REVIEWS_PER_PAGE);
        Page<Review> reviews = reviewRepository.findByBookIdOrderByCreatedAtDesc(bookId, pageable);

        return reviews.map(ReviewResponseDto::from);
    }

    public List<ReviewResponseDto> getReviewsByUser(Long userId) {
        List<Review> reviews = reviewRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return reviews.stream()
                .map(ReviewResponseDto::from)
                .collect(Collectors.toList());
    }

    public Double getAverageRating(Long bookId) {
        Double avg = reviewRepository.getAverageRatingByBookId(bookId);
        return avg != null ? Math.round(avg * 10) / 10.0 : 0.0;
    }

    public long getReviewCount(Long bookId) {
        return reviewRepository.countByBookId(bookId);
    }

    public Review findById(Long id) {
        return reviewRepository.findById(id).orElseThrow(() -> new ExpectedException(ErrorCode.REVIEW_NOT_FOUND));
    }
}
