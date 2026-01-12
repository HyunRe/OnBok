package com.onbok.book_hub.book.dto.response;

import com.onbok.book_hub.book.domain.model.book.Book;
import com.onbok.book_hub.common.pagination.PaginationInfo;
import lombok.Getter;

import java.util.List;

@Getter
public class BookListResponseDto {
    private final List<Book> books;
    private final PaginationInfo paginationInfo;

    public BookListResponseDto(List<Book> books, PaginationInfo paginationInfo) {
        this.books = books;
        this.paginationInfo = paginationInfo;
    }
}
