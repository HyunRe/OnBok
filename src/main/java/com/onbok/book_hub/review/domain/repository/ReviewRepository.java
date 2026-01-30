package com.onbok.book_hub.review.domain.repository;

import com.onbok.book_hub.review.domain.model.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    // 특정 도서의 리뷰 조회 (최신순)
    Page<Review> findByBookIdOrderByCreatedAtDesc(Long bookId, Pageable pageable);

    // 특정 사용자의 리뷰 조회
    List<Review> findByUserIdOrderByCreatedAtDesc(Long userId);

    // 특정 도서의 평균 평점 계산
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.book.id = :bookId")
    Double getAverageRatingByBookId(Long bookId);

    // 특정 도서의 리뷰 개수
    long countByBookId(Long bookId);

    // 특정 사용자가 특정 도서에 작성한 리뷰 조회
    Review findByBookIdAndUserId(Long bookId, Long userId);

    // 사용자별 리뷰 조회
    List<Review> findByUserId(Long userId);

    // 도서별 리뷰 조회
    List<Review> findByBookId(Long bookId);

    // 사용자와 도서로 리뷰 조회
    Optional<Review> findByUserIdAndBookId(Long userId, Long bookId);

    // 평점별 리뷰 조회
    List<Review> findByRating(int rating);

    // 리뷰 평점 범위 조회
    List<Review> findByRatingGreaterThanEqual(int rating);
}
