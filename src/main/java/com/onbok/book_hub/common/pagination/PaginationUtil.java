package com.onbok.book_hub.common.pagination;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class PaginationUtil {
    public static final int DEFAULT_ROW_SIZE = 8;  // 한 페이지에 보여줄 게시글 수
    public static final int DEFAULT_PAGE_SIZE = 10; // 하단에 표시할 페이지 번호 개수

    /**
     * 페이지 번호와 페이지 크기로 Pageable 객체 생성
     * @param page 현재 페이지 (1부터 시작)
     * @param size 페이지당 항목 수
     * @return Pageable 객체
     */
    public static Pageable createPageable(int page, int size) {
        return PageRequest.of(page - 1, size);
    }

    /**
     * 기본 페이지 크기로 Pageable 객체 생성
     * @param page 현재 페이지 (1부터 시작)
     * @return Pageable 객체
     */
    public static Pageable createDefaultPageable(int page) {
        return PageRequest.of(page - 1, DEFAULT_ROW_SIZE);
    }

    /**
     * 페이지네이션 정보 계산
     * @param currentPage 현재 페이지
     * @param totalPages 전체 페이지 수
     * @param pageSize 한 번에 보여줄 페이지 버튼 수
     * @return PaginationInfo 객체
     */
    public static PaginationInfo calculatePagination(int currentPage, int totalPages, int pageSize) {
        int startPage = (int) Math.ceil((currentPage - 0.5) / pageSize - 1) * pageSize + 1;
        int endPage = Math.min(startPage + pageSize - 1, totalPages);

        List<Integer> pageList = new ArrayList<>();
        for (int i = startPage; i <= endPage; i++) {
            pageList.add(i);
        }

        return new PaginationInfo(startPage, endPage, pageList, totalPages);
    }

    /**
     * 기본 페이지 크기로 페이지네이션 정보 계산
     */
    public static PaginationInfo calculatePagination(int currentPage, int totalPages) {
        return calculatePagination(currentPage, totalPages, DEFAULT_PAGE_SIZE);
    }
}
