package com.onbok.book_hub.review.dto.response;

import com.onbok.book_hub.review.domain.model.Review;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ReviewResponseDto {
    private Long id;
    private Long bookId;
    private String bookTitle;
    private Long userId;
    private String userName;
    private int rating;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Entity -> Response 변환 팩토리 메서드
    public static ReviewResponseDto from(Review review) {
        return ReviewResponseDto.builder()
                .id(review.getId())
                .bookId(review.getBook().getId())
                .bookTitle(review.getBook().getTitle())
                .userId(review.getUser().getId())
                .userName(review.getUser().getUname())
                .rating(review.getRating())
                .content(review.getContent())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
    }
}
