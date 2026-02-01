package com.onbok.book_hub.domain;

import com.onbok.book_hub.book.domain.model.book.Book;
import com.onbok.book_hub.cart.domain.model.Cart;
import com.onbok.book_hub.user.domain.model.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@DisplayName("Cart 도메인 단위 테스트")
class CartTest {

    @Test
    @DisplayName("장바구니 생성 - 정상 케이스")
    void createCart_success() {
        // given
        User user = User.builder()
                .email("test@test.com")
                .build();
        Book book = Book.builder()
                .title("테스트 도서")
                .price(15000)
                .stock(100)
                .build();

        // when
        Cart cart = Cart.builder()
                .user(user)
                .book(book)
                .quantity(2)
                .build();

        // then
        assertThat(cart.getUser()).isEqualTo(user);
        assertThat(cart.getBook()).isEqualTo(book);
        assertThat(cart.getQuantity()).isEqualTo(2);
    }

    @Test
    @DisplayName("장바구니 수량 변경 - 정상 케이스")
    void updateQuantity_success() {
        // given
        Cart cart = Cart.builder()
                .quantity(2)
                .build();

        // when
        cart.updateQuantity(5);

        // then
        assertThat(cart.getQuantity()).isEqualTo(5);
    }

    @Test
    @DisplayName("장바구니 수량을 0으로 변경 (삭제 의도)")
    void updateQuantity_toZero() {
        // given
        Cart cart = Cart.builder()
                .quantity(2)
                .build();

        // when
        cart.updateQuantity(0);

        // then
        assertThat(cart.getQuantity()).isEqualTo(0);
    }
}
