package com.onbok.book_hub.book.application.service.book;

import com.onbok.book_hub.book.domain.model.book.Book;
import com.onbok.book_hub.book.domain.repository.book.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 도서 생성/수정/삭제 관련 비즈니스 로직 (Command)
 */
@Service
@RequiredArgsConstructor
public class BookCommandService {
    private final BookRepository bookRepository;

    public void insertBook(Book book) {
        bookRepository.save(book);
    }

    public void updateBook(Book book) {
        bookRepository.save(book);
    }

    public void deleteBook(Long id) {
        bookRepository.deleteById(id);
    }
}
