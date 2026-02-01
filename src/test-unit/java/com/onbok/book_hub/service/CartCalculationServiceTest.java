package com.onbok.book_hub.service;

import com.onbok.book_hub.book.domain.model.book.Book;
import com.onbok.book_hub.cart.application.CartCalculationService;
import com.onbok.book_hub.cart.domain.model.Cart;
import com.onbok.book_hub.user.domain.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@DisplayName("CartCalculationService 단위 테스트 - 총 금액 계산")
class CartCalculationServiceTest {

    private CartCalculationService cartCalculationService;
    private static final int DELIVERY_COST = 3000;

    @BeforeEach
    void setUp() {
        cartCalculationService = new CartCalculationService();
    }

    @Test
    @DisplayName("장바구니 총 금액 계산 - 단일 상품")
    void calculateCartSummary_singleItem() {
        // given
        User user = User.builder().email("test@test.com").build();
        Book book = Book.builder()
                .title("자바의 정석")
                .price(30000)
                .stock(100)
                .build();
        Cart cart = Cart.builder()
                .user(user)
                .book(book)
                .quantity(2)
                .build();

        // when
        Map<String, Object> result = cartCalculationService.calculateCartSummary(List.of(cart));

        // then
        assertThat(result.get("totalPrice")).isEqualTo(60000); // 30000 * 2
        assertThat(result.get("deliveryCost")).isEqualTo(DELIVERY_COST);
        assertThat(result.get("totalPriceIncludingDeliveryCost")).isEqualTo(63000); // 60000 + 3000
    }

    @Test
    @DisplayName("장바구니 총 금액 계산 - 여러 상품")
    void calculateCartSummary_multipleItems() {
        // given
        User user = User.builder().email("test@test.com").build();
        Book book1 = Book.builder().title("자바의 정석").price(30000).stock(100).build();
        Book book2 = Book.builder().title("스프링 인 액션").price(40000).stock(50).build();
        Book book3 = Book.builder().title("클린 코드").price(35000).stock(80).build();

        Cart cart1 = Cart.builder().user(user).book(book1).quantity(2).build();
        Cart cart2 = Cart.builder().user(user).book(book2).quantity(1).build();
        Cart cart3 = Cart.builder().user(user).book(book3).quantity(3).build();

        // when
        Map<String, Object> result = cartCalculationService.calculateCartSummary(List.of(cart1, cart2, cart3));

        // then
        int expectedTotal = (30000 * 2) + (40000 * 1) + (35000 * 3); // 60000 + 40000 + 105000 = 205000
        assertThat(result.get("totalPrice")).isEqualTo(expectedTotal);
        assertThat(result.get("totalPriceIncludingDeliveryCost")).isEqualTo(expectedTotal + DELIVERY_COST);
    }

    @Test
    @DisplayName("빈 장바구니 총 금액 계산 - 배송비만 존재")
    void calculateCartSummary_emptyCart() {
        // when
        Map<String, Object> result = cartCalculationService.calculateCartSummary(List.of());

        // then
        assertThat(result.get("totalPrice")).isEqualTo(0);
        assertThat(result.get("deliveryCost")).isEqualTo(DELIVERY_COST);
        assertThat(result.get("totalPriceIncludingDeliveryCost")).isEqualTo(DELIVERY_COST);
    }

    @Test
    @DisplayName("장바구니 업데이트 후 금액 재계산")
    void calculateUpdateResult_success() {
        // given
        User user = User.builder().email("test@test.com").build();
        Book book1 = Book.builder().title("자바의 정석").price(30000).stock(100).build();
        Book book2 = Book.builder().title("스프링 인 액션").price(40000).stock(50).build();

        Cart cart1 = Cart.builder().user(user).book(book1).quantity(3).build();
        Cart cart2 = Cart.builder().user(user).book(book2).quantity(1).build();

        int subTotal = 90000; // 30000 * 3 (변경된 cart1의 부분 금액)

        // when
        Map<String, Object> result = cartCalculationService.calculateUpdateResult(List.of(cart1, cart2), subTotal);

        // then
        int expectedTotal = (30000 * 3) + (40000 * 1); // 130000
        assertThat(result.get("subTotal")).isEqualTo(subTotal);
        assertThat(result.get("totalPrice")).isEqualTo(expectedTotal);
        assertThat(result.get("totalPriceIncludingDeliveryCost")).isEqualTo(expectedTotal + DELIVERY_COST);
    }
}
