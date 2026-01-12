package com.onbok.book_hub.review.domain.model;

import com.onbok.book_hub.book.domain.model.book.Book;
import com.onbok.book_hub.common.domain.BaseTime;
import com.onbok.book_hub.common.exception.ErrorCode;
import com.onbok.book_hub.common.exception.ExpectedException;
import com.onbok.book_hub.user.domain.model.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "reviews")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Review extends BaseTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false, referencedColumnName = "id")
    private Book book;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, referencedColumnName = "id")
    private User user;

    @Column(nullable = false)
    private int rating;  // 평점 (1-5)

    @Column(length = 2000)
    private String content;  // 리뷰 내용

    @Builder
    public Review(Book book, User user, int rating, String content) {
        this.book = book;
        this.user = user;
        this.rating = rating;
        this.content = content;
    }

    // 리뷰 내용과 평점을 업데이트
    public void update(int rating, String content) {
        validateRating(rating); // 별도의 검증 로직 호출
        this.rating = rating;
        this.content = content;
    }

    // 평점 유효성 검증
    private void validateRating(int rating) {
        if (rating < 1 || rating > 5) {
            throw new ExpectedException(ErrorCode.INVALID_RATING_RANGE);
        }
    }
}
