package com.onbok.book_hub.order;

import com.onbok.book_hub.book.domain.model.book.Book;
import com.onbok.book_hub.book.domain.repository.book.BookRepository;
import com.onbok.book_hub.cart.domain.model.Cart;
import com.onbok.book_hub.delivery.domain.model.DeliveryAddress;
import com.onbok.book_hub.delivery.domain.repository.DeliveryAddressRepository;
import com.onbok.book_hub.order.application.OrderCommandService;
import com.onbok.book_hub.order.domain.model.Order;
import com.onbok.book_hub.order.domain.model.OrderStatus;
import com.onbok.book_hub.payment.domain.model.TossPayment;
import com.onbok.book_hub.user.domain.model.User;
import com.onbok.book_hub.user.domain.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@DisplayName("Order 통합 테스트 - Cart 기반 주문 생성")
class OrderIntegrationTest {

    @Autowired
    private OrderCommandService orderCommandService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private DeliveryAddressRepository deliveryAddressRepository;

    @Test
    @DisplayName("Cart 기반 주문 생성 - 단일 상품")
    void createOrder_singleItem() {
        // given
        User user = createUser("test@test.com");
        Book book = createBook("자바의 정석", 30000, 100);
        DeliveryAddress address = createDeliveryAddress(user, "집");

        Cart cart = Cart.builder()
                .user(user)
                .book(book)
                .quantity(2)
                .build();

        TossPayment tossPayment = TossPayment.builder()
                .paymentKey("test_payment_key")
                .totalPayment(60000)
                .build();

        // when
        Order order = orderCommandService.createOrder(user.getId(), List.of(cart), tossPayment, address.getId());

        // then
        assertThat(order).isNotNull();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(order.getTotalAmount()).isEqualTo(60000);
        assertThat(order.getOrderItems()).hasSize(1);
        assertThat(order.getOrderItems().get(0).getQuantity()).isEqualTo(2);
        assertThat(order.getDeliveryAddress()).isEqualTo(address);
    }

    @Test
    @DisplayName("Cart 기반 주문 생성 - 여러 상품")
    void createOrder_multipleItems() {
        // given
        User user = createUser("test@test.com");
        Book book1 = createBook("자바의 정석", 30000, 100);
        Book book2 = createBook("스프링 인 액션", 40000, 50);
        Book book3 = createBook("클린 코드", 35000, 80);
        DeliveryAddress address = createDeliveryAddress(user, "회사");

        Cart cart1 = Cart.builder().user(user).book(book1).quantity(2).build();
        Cart cart2 = Cart.builder().user(user).book(book2).quantity(1).build();
        Cart cart3 = Cart.builder().user(user).book(book3).quantity(3).build();

        int totalAmount = (30000 * 2) + (40000 * 1) + (35000 * 3); // 205000

        TossPayment tossPayment = TossPayment.builder()
                .paymentKey("test_payment_key")
                .totalPayment(totalAmount)
                .build();

        // when
        Order order = orderCommandService.createOrder(user.getId(), List.of(cart1, cart2, cart3), tossPayment, address.getId());

        // then
        assertThat(order.getOrderItems()).hasSize(3);
        assertThat(order.getTotalAmount()).isEqualTo(totalAmount);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);

        // 각 상품의 수량 검증
        assertThat(order.getOrderItems())
                .extracting("quantity")
                .containsExactlyInAnyOrder(2, 1, 3);
    }

    @Test
    @DisplayName("주문 생성 후 재고 감소 확인")
    void createOrder_stockDecreased() {
        // given
        User user = createUser("test@test.com");
        Book book = createBook("테스트 도서", 15000, 50);
        DeliveryAddress address = createDeliveryAddress(user, "집");

        Cart cart = Cart.builder()
                .user(user)
                .book(book)
                .quantity(10)
                .build();

        TossPayment tossPayment = TossPayment.builder()
                .paymentKey("test_payment_key")
                .totalPayment(150000)
                .build();

        int originalStock = book.getStock();

        // when
        orderCommandService.createOrder(user.getId(), List.of(cart), tossPayment, address.getId());

        // then
        Book updatedBook = bookRepository.findById(book.getId()).orElseThrow();
        assertThat(updatedBook.getStock()).isEqualTo(originalStock - 10);
    }

    @Test
    @DisplayName("배송지 없는 주문 불가")
    void createOrder_withoutDeliveryAddress() {
        // given
        User user = createUser("test@test.com");
        Book book = createBook("테스트 도서", 15000, 100);

        Cart cart = Cart.builder()
                .user(user)
                .book(book)
                .quantity(2)
                .build();

        TossPayment tossPayment = TossPayment.builder()
                .paymentKey("test_payment_key")
                .totalPayment(30000)
                .build();

        // when & then
        org.junit.jupiter.api.Assertions.assertThrows(Exception.class, () -> {
            orderCommandService.createOrder(user.getId(), List.of(cart), tossPayment, 999L); // 존재하지 않는 배송지 ID
        });
    }

    // === Helper Methods ===

    private User createUser(String email) {
        User user = User.builder()
                .email(email)
                .uname("테스트 유저")
                .build();
        return userRepository.save(user);
    }

    private Book createBook(String title, int price, int stock) {
        Book book = Book.builder()
                .title(title)
                .author("테스트 저자")
                .company("테스트 출판사")
                .price(price)
                .stock(stock)
                .category("IT")
                .build();
        return bookRepository.save(book);
    }

    private DeliveryAddress createDeliveryAddress(User user, String alias) {
        DeliveryAddress address = DeliveryAddress.builder()
                .user(user)
                .alias(alias)
                .recipientName("홍길동")
                .zipCode("06234")
                .basicAddress("서울시 강남구 테헤란로")
                .detailAddress("123호")
                .tel("010-1234-5678")
                .build();
        return deliveryAddressRepository.save(address);
    }
}
