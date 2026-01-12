package com.onbok.book_hub.book;

import com.onbok.book_hub.book.domain.model.book.Book;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@DisplayName("Book 도메인 테스트")
class BookTest {
    private Book book;

    @BeforeEach
    void setUp() {
        book = Book.builder()
                .title("테스트 도서")
                .author("테스트 저자")
                .company("테스트 출판사")
                .price(15000)
                .stock(10)
                .build();
    }

    @Test
    @DisplayName("재고 감소 - 정상 케이스")
    void decreaseStock_success() {
        // given
        int quantity = 5;

        // when
        book.decreaseStock(quantity);

        // then
        assertThat(book.getStock()).isEqualTo(5);
    }

    @Test
    @DisplayName("재고 감소 - 재고 부족 시 예외 발생")
    void decreaseStock_insufficientStock() {
        // given
        int quantity = 15; // 재고(10)보다 많은 수량

        // when & then
        assertThatThrownBy(() -> book.decreaseStock(quantity))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("재고가 부족합니다");
    }

    @Test
    @DisplayName("재고 감소 - 정확히 재고만큼 감소")
    void decreaseStock_exactAmount() {
        // given
        int quantity = 10;

        // when
        book.decreaseStock(quantity);

        // then
        assertThat(book.getStock()).isEqualTo(0);
    }

    @Test
    @DisplayName("재고 증가 - 정상 케이스")
    void increaseStock_success() {
        // given
        int quantity = 5;

        // when
        book.increaseStock(quantity);

        // then
        assertThat(book.getStock()).isEqualTo(15);
    }

    @Test
    @DisplayName("재고 증가 - 여러 번 증가")
    void increaseStock_multiple() {
        // when
        book.increaseStock(3);
        book.increaseStock(7);

        // then
        assertThat(book.getStock()).isEqualTo(20);
    }

    @Test
    @DisplayName("재고 감소 후 증가")
    void decreaseAndIncreaseStock() {
        // when
        book.decreaseStock(5);
        book.increaseStock(3);

        // then
        assertThat(book.getStock()).isEqualTo(8);
    }
}
