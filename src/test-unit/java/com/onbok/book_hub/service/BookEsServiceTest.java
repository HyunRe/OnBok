package com.onbok.book_hub.service;

import com.onbok.book_hub.book.application.service.bookEs.BookEsService;
import com.onbok.book_hub.book.domain.model.bookEs.BookEs;
import com.onbok.book_hub.book.domain.repository.bookEs.BookEsRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
@DisplayName("BookEsService 단위 테스트")
class BookEsServiceTest {

    @Mock
    private BookEsRepository bookEsRepository;

    @InjectMocks
    private BookEsService bookEsService;

    @Test
    @DisplayName("도서 ES 저장")
    void insertBookEs_success() {
        // given
        BookEs book = BookEs.builder()
                .bookId("1")
                .title("스프링 부트")
                .author("저자A")
                .company("출판사A")
                .price(30000)
                .summary("요약")
                .build();

        when(bookEsRepository.save(any(BookEs.class))).thenReturn(book);

        // when
        BookEs result = bookEsService.insertBookEs(book);

        // then
        assertThat(result).isNotNull();
        verify(bookEsRepository).save(book);
    }

    @Test
    @DisplayName("도서 ID로 조회")
    void findById_success() {
        // given
        BookEs book = BookEs.builder()
                .bookId("1")
                .title("자바의 정석")
                .build();

        when(bookEsRepository.findById("1"))
                .thenReturn(Optional.of(book));

        // when
        BookEs result = bookEsService.findById("1");

        // then
        assertThat(result.getTitle()).isEqualTo("자바의 정석");
    }

    @Test
    @DisplayName("도서 삭제")
    void deleteBookEs_success() {
        // when
        bookEsService.deleteBookEs("1");

        // then
        verify(bookEsRepository).deleteById("1");
    }
}

