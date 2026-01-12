package com.onbok.book_hub.book.domain.repository.book;

import com.onbok.book_hub.book.domain.model.book.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookRepository extends JpaRepository<Book, Long> {
    // 모든 도서 목록 조회 (페이징 처리)
    Page<Book> findAll(Pageable pageable);

    // 도서 제목에 특정 단어가 포함된 도서 검색
    Page<Book> findByTitleContaining(String title, Pageable pageable);

    // 저자 이름에 특정 단어가 포함된 도서 검색
    Page<Book> findByAuthorContaining(String author, Pageable pageable);
    // 출판사 이름에 특정 단어가 포함된 도서 검색
    Page<Book> findByCompanyContaining(String company, Pageable pageable);
    // 도서 요약 내용에 특정 단어가 포함된 도서 검색
    Page<Book> findBySummaryContaining(String summary, Pageable pageable);

    // 특정 가격 범위 내의 도서 검색 (최소가 ~ 최대가)
    Page<Book> findByPriceBetween(int minPrice, int maxPrice, Pageable pageable);

    // 추천 시스템용 메서드
    // 특정 저자의 모든 도서 목록 조회
    List<Book> findByAuthor(String author);
    // 특정 출판사의 모든 도서 목록 조회
    List<Book> findByCompany(String company);
}