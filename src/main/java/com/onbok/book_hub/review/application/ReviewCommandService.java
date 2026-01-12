package com.onbok.book_hub.review.application;

import com.onbok.book_hub.book.domain.model.book.Book;
import com.onbok.book_hub.book.domain.repository.book.BookRepository;
import com.onbok.book_hub.common.exception.ErrorCode;
import com.onbok.book_hub.common.exception.ExpectedException;
import com.onbok.book_hub.review.domain.model.Review;
import com.onbok.book_hub.review.domain.repository.ReviewRepository;
import com.onbok.book_hub.user.domain.model.User;
import com.onbok.book_hub.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Review Command Service - CQS Pattern
 * 리뷰 상태 변경 작업 담당 (생성, 수정, 삭제)
 */
@Service
@RequiredArgsConstructor
public class ReviewCommandService {
    private final ReviewRepository reviewRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final ReviewQueryService reviewQueryService;

    // 본인 계정 확인
    private boolean isOwner(User user, Long reviewOwnerId) {
        return !user.getId().equals(reviewOwnerId);
    }

    @Transactional
    public Review createReview(Long bookId, Long userId, int rating, String content) {
        Book book = bookRepository.findById(bookId).orElseThrow(() -> new ExpectedException(ErrorCode.BOOK_NOT_FOUND));
        User user = userRepository.findById(userId).orElseThrow(() -> new ExpectedException(ErrorCode.USER_NOT_FOUND));

        // 기존 리뷰가 있는지 확인
        Review existingReview = reviewRepository.findByBookIdAndUserId(bookId, userId);
        if (existingReview != null) {
            throw new ExpectedException(ErrorCode.REVIEW_ALREADY_EXISTS);
        }

        Review review = Review.builder()
                .book(book)
                .user(user)
                .rating(rating)
                .content(content)
                .build();

        return reviewRepository.save(review);
    }

    @Transactional
    public Review updateReview(User user, Long id, int rating, String content) {
        Review review = reviewQueryService.findById(id);
        // 내 리뷰 인지 확인 필요
        if (isOwner(user, review.getUser().getId())) {
            throw new ExpectedException(ErrorCode.INSUFFICIENT_PERMISSION);
        }

        review.update(rating, content);

        return reviewRepository.save(review);
    }

    @Transactional
    public void deleteReview(User user, Long id) {
        Review review = reviewQueryService.findById(id);
        // 내 리뷰 인지 확인 필요
        if (isOwner(user, review.getUser().getId())) {
            throw new ExpectedException(ErrorCode.INSUFFICIENT_PERMISSION);
        }

        reviewRepository.deleteById(id);
    }
}
