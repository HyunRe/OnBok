package com.onbok.book_hub.common.pagination;

import java.util.List;

public record PaginationInfo(int startPage, int endPage, List<Integer> pageList, int totalPages) {
}
