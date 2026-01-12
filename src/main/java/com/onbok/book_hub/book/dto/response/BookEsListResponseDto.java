package com.onbok.book_hub.book.dto.response;

import com.onbok.book_hub.book.dto.BookEsDto;
import com.onbok.book_hub.common.pagination.PaginationInfo;

import java.util.List;

public record BookEsListResponseDto(List<BookEsDto> bookEsList, PaginationInfo paginationInfo) {
}
