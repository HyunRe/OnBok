package com.onbok.book_hub.infrastructure;

import com.onbok.book_hub.book.domain.model.book.Book;
import com.onbok.book_hub.book.domain.repository.book.BookRepository;
import com.onbok.book_hub.cart.domain.model.Cart;
import com.onbok.book_hub.delivery.domain.model.DeliveryAddress;
import com.onbok.book_hub.delivery.domain.repository.DeliveryAddressRepository;
import com.onbok.book_hub.order.application.OrderCommandService;
import com.onbok.book_hub.payment.domain.model.TossPayment;
import com.onbok.book_hub.user.domain.model.User;
import com.onbok.book_hub.user.domain.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("트랜잭션 롤백 검증 테스트")
public class TransactionRollbackTest {
    @Autowired
    private OrderCommandService orderCommandService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private DeliveryAddressRepository deliveryAddressRepository;

    @Test
    @DisplayName("주문 생성 중 재고 부족 시 전체 롤백")
    void createOrder_insufficientStock_rollback() {
        // given
        User user = userRepository.save(User.builder().email("test@test.com").uname("테스트 유저").build());
        Book book1 = bookRepository.save(Book.builder().title("충분한 재고 책").author("테스트 저자").company("테스트 출판사").price(15000).stock(100).category("IT").build()); // 재고 100개
        Book book2 = bookRepository.save(Book.builder().title("부족한 재고 책").author("테스트 저자").company("테스트 출판사").price(20000).stock(5).category("IT").build());   // 재고 5개
        DeliveryAddress address = deliveryAddressRepository.save(
                DeliveryAddress.builder()
                    .user(user)
                    .alias("집")
                    .recipientName("홍길동")
                    .zipCode("06234")
                    .basicAddress("서울시 강남구 테헤란로")
                    .detailAddress("123호")
                    .tel("010-1234-5678")
                    .build()
        );

        Cart cart1 = Cart.builder().user(user).book(book1).quantity(10).build();
        Cart cart2 = Cart.builder().user(user).book(book2).quantity(10).build(); // 재고보다 많이 주문

        TossPayment tossPayment = TossPayment.builder()
                .paymentKey("test_key")
                .totalPayment(350000)
                .build();

        int book1OriginalStock = book1.getStock();
        int book2OriginalStock = book2.getStock();

        // when & then
        assertThatThrownBy(() ->
                orderCommandService.createOrder(user.getId(), List.of(cart1, cart2), tossPayment, address.getId())
        ).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("재고가 부족합니다");

        // 재고가 롤백되어 원래대로 돌아왔는지 확인
        Book updatedBook1 = bookRepository.findById(book1.getId()).orElseThrow();
        Book updatedBook2 = bookRepository.findById(book2.getId()).orElseThrow();
        assertThat(updatedBook1.getStock()).isEqualTo(book1OriginalStock); // 롤백됨
        assertThat(updatedBook2.getStock()).isEqualTo(book2OriginalStock); // 변경되지 않음
    }
}
