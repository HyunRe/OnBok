package com.onbok.book_hub.book;

import com.onbok.book_hub.book.domain.model.book.Book;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Book 재고 관리 단위 테스트")
class BookStockTest {

    @Test
    @DisplayName("재고 감소 - 정상 케이스")
    void decreaseStock_success() {
        // given
        Book book = Book.builder()
                .title("테스트 도서")
                .price(15000)
                .stock(100)
                .build();

        // when
        book.decreaseStock(30);

        // then
        assertThat(book.getStock()).isEqualTo(70);
    }

    @Test
    @DisplayName("재고 감소 - 재고가 정확히 0이 되는 경우")
    void decreaseStock_toZero() {
        // given
        Book book = Book.builder()
                .title("테스트 도서")
                .price(15000)
                .stock(50)
                .build();

        // when
        book.decreaseStock(50);

        // then
        assertThat(book.getStock()).isEqualTo(0);
    }

    @Test
    @DisplayName("재고 감소 실패 - 재고 부족")
    void decreaseStock_insufficientStock() {
        // given
        Book book = Book.builder()
                .title("테스트 도서")
                .price(15000)
                .stock(10)
                .build();

        // when & then
        assertThatThrownBy(() -> book.decreaseStock(15))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("재고가 부족합니다");
    }

    @Test
    @DisplayName("재고 증가 - 정상 케이스 (주문 취소/환불 시)")
    void increaseStock_success() {
        // given
        Book book = Book.builder()
                .title("테스트 도서")
                .price(15000)
                .stock(50)
                .build();

        // when
        book.increaseStock(20);

        // then
        assertThat(book.getStock()).isEqualTo(70);
    }

    @Test
    @DisplayName("재고가 0인 상태에서 증가")
    void increaseStock_fromZero() {
        // given
        Book book = Book.builder()
                .title("테스트 도서")
                .price(15000)
                .stock(0)
                .build();

        // when
        book.increaseStock(100);

        // then
        assertThat(book.getStock()).isEqualTo(100);
    }
}
