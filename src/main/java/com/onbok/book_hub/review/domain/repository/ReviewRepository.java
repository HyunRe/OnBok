package com.onbok.book_hub.review.domain.repository;

import com.onbok.book_hub.review.domain.model.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    // 특정 도서의 리뷰 조회 (최신순)
    Page<Review> findByBookIdOrderByCreatedAtDesc(Long bookId, Pageable pageable);

    // 특정 사용자의 리뷰 조회
    List<Review> findByUserIdOrderByCreatedAtDesc(Long userId);

    // 특정 도서의 평균 평점 계산
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.book.bid = :bid")
    Double getAverageRatingByBookId(Long bookId);

    // 특정 도서의 리뷰 개수
    long countByBookId(Long bookId);

    // 특정 사용자가 특정 도서에 작성한 리뷰 조회
    Review findByBookIdAndUserId(Long bookId, Long userId);
}
