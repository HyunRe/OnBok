package com.onbok.book_hub.book.application.service.book;

import com.onbok.book_hub.book.domain.model.book.Book;
import com.onbok.book_hub.book.domain.repository.book.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 도서 재고 관리 전용 Service
 */
@Service
@RequiredArgsConstructor
public class BookStockService {
    private final BookQueryService bookQueryService;

    /**
     * 재고 감소
     */
    public void decreaseStock(Long id, int quantity) {
        Book book = bookQueryService.findById(id);
        book.decreaseStock(quantity);
    }

    /**
     * 재고 증가
     */
    public void increaseStock(Long id, int quantity) {
        Book book = bookQueryService.findById(id);
        book.increaseStock(quantity);
    }

    /**
     * 재고 조회
     */
    public int getStock(Long id) {
        Book book = bookQueryService.findById(id);
        return book.getStock();
    }
}
