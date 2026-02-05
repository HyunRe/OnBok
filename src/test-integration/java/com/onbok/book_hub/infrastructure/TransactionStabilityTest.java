package com.onbok.book_hub.infrastructure;

import com.onbok.book_hub.book.domain.model.book.Book;
import com.onbok.book_hub.book.domain.repository.book.BookRepository;
import com.onbok.book_hub.cart.domain.model.Cart;
import com.onbok.book_hub.delivery.domain.model.DeliveryAddress;
import com.onbok.book_hub.delivery.domain.repository.DeliveryAddressRepository;
import com.onbok.book_hub.order.application.OrderCommandService;
import com.onbok.book_hub.order.domain.model.Order;
import com.onbok.book_hub.order.domain.model.OrderStatus;
import com.onbok.book_hub.order.domain.repository.OrderRepository;
import com.onbok.book_hub.payment.domain.model.TossPayment;
import com.onbok.book_hub.user.domain.model.User;
import com.onbok.book_hub.user.domain.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
@DisplayName("트랜잭션 안정성 통합 테스트")
class TransactionStabilityTest {

    @Autowired
    private OrderCommandService orderCommandService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private DeliveryAddressRepository deliveryAddressRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Test
    @DisplayName("주문 취소 시 재고 복구 및 상태 변경 원자성 보장")
    void cancelOrder_atomicity() {
        // given
        User user = createUser("test@test.com");
        Book book = createBook("테스트 도서", 15000, 50);
        DeliveryAddress address = createDeliveryAddress(user, "집");

        Cart cart = Cart.builder().user(user).book(book).quantity(10).build();

        TossPayment tossPayment = TossPayment.builder()
                .paymentKey("test_key")
                .totalPayment(150000)
                .build();

        Order order = orderCommandService.createOrder(user.getId(), List.of(cart), tossPayment, address.getId());
        Long orderId = order.getId();

        int stockAfterOrder = bookRepository.findById(book.getId()).orElseThrow().getStock();
        assertThat(stockAfterOrder).isEqualTo(40); // 50 - 10 = 40

        // when - 주문 취소
        orderCommandService.cancelOrder(orderId);

        // then
        Order cancelledOrder = orderRepository.findById(orderId).orElseThrow();
        assertThat(cancelledOrder.getStatus()).isEqualTo(OrderStatus.CANCELLED); // 상태 변경됨

        Book restoredBook = bookRepository.findById(book.getId()).orElseThrow();
        assertThat(restoredBook.getStock()).isEqualTo(50); // 재고 복구됨 (40 + 10)
    }

    @Test
    @DisplayName("배송지 포함 주문 생성 원자성 보장")
    void createOrder_withDeliveryAddress_atomicity() {
        // given
        User user = createUser("test@test.com");
        Book book = createBook("테스트 도서", 15000, 100);
        DeliveryAddress address = createDeliveryAddress(user, "집");

        Cart cart = Cart.builder().user(user).book(book).quantity(2).build();

        TossPayment tossPayment = TossPayment.builder()
                .paymentKey("test_key")
                .totalPayment(30000)
                .build();

        // when
        Order order = orderCommandService.createOrder(user.getId(), List.of(cart), tossPayment, address.getId());

        // then - 주문과 배송지가 함께 저장되었는지 확인
        assertThat(order.getDeliveryAddress()).isNotNull();
        assertThat(order.getDeliveryAddress().getId()).isEqualTo(address.getId());
        assertThat(order.getOrderItems()).hasSize(1);
        assertThat(order.getTotalAmount()).isEqualTo(30000);
    }

    @Test
    @DisplayName("주문 환불 시 재고 복구 원자성 보장")
    void refundOrder_atomicity() {
        // given
        User user = createUser("test@test.com");
        Book book = createBook("테스트 도서", 15000, 50);
        DeliveryAddress address = createDeliveryAddress(user, "집");

        Cart cart = Cart.builder().user(user).book(book).quantity(5).build();

        TossPayment tossPayment = TossPayment.builder()
                .paymentKey("test_key")
                .totalPayment(75000)
                .build();

        Order order = orderCommandService.createOrder(user.getId(), List.of(cart), tossPayment, address.getId());
        Long orderId = order.getId();

        // 주문 완료 프로세스 진행
        orderCommandService.completePayment(orderId);
        orderCommandService.startPreparing(orderId);
        orderCommandService.shipOrder(orderId);
        orderCommandService.deliverOrder(orderId);

        int stockAfterOrder = bookRepository.findById(book.getId()).orElseThrow().getStock();
        assertThat(stockAfterOrder).isEqualTo(45); // 50 - 5 = 45

        // when - 환불
        orderCommandService.refundOrder(orderId);

        // then
        Order refundedOrder = orderRepository.findById(orderId).orElseThrow();
        assertThat(refundedOrder.getStatus()).isEqualTo(OrderStatus.REFUNDED); // 상태 변경됨

        Book restoredBook = bookRepository.findById(book.getId()).orElseThrow();
        assertThat(restoredBook.getStock()).isEqualTo(50); // 재고 복구됨 (45 + 5)
    }

    @Test
    @DisplayName("존재하지 않는 배송지로 주문 생성 시 실패")
    void createOrder_withInvalidDeliveryAddress_fails() {
        // given
        User user = createUser("test@test.com");
        Book book = createBook("테스트 도서", 15000, 100);

        Cart cart = Cart.builder().user(user).book(book).quantity(2).build();

        TossPayment tossPayment = TossPayment.builder()
                .paymentKey("test_key")
                .totalPayment(30000)
                .build();

        Long invalidDeliveryId = 9999L;
        Long beforeCount = orderRepository.count();


        // when & then
        assertThatThrownBy(() ->
                orderCommandService.createOrder(user.getId(), List.of(cart), tossPayment, invalidDeliveryId)
        ).isInstanceOf(Exception.class);

        // then
        assertThat(orderRepository.count()).isEqualTo(beforeCount);
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
