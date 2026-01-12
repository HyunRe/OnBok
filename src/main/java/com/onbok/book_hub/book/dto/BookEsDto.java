package com.onbok.book_hub.book.dto;

import com.onbok.book_hub.book.domain.model.bookEs.BookEs;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BookEsDto {
    private BookEs bookEs;
    private float matchScore;

    public BookEsDto(BookEs bookEs, float matchScore) {
        this.bookEs = bookEs;
        this.matchScore = matchScore;
    }
}
