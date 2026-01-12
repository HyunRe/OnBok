package com.onbok.book_hub.review.dto.request;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReviewCreateRequestDto {
    private Long bookId;
    private Integer rating;  // 1-5
    private String content;
}
