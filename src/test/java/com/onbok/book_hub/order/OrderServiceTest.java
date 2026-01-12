package com.onbok.book_hub.order;

import com.onbok.book_hub.book.domain.model.book.Book;
import com.onbok.book_hub.book.domain.repository.book.BookRepository;
import com.onbok.book_hub.cart.domain.model.Cart;
import com.onbok.book_hub.cart.domain.repository.CartRepository;
import com.onbok.book_hub.delivery.domain.model.DeliveryAddress;
import com.onbok.book_hub.order.application.OrderCommandService;
import com.onbok.book_hub.order.application.OrderQueryService;
import com.onbok.book_hub.order.domain.model.Order;
import com.onbok.book_hub.order.domain.model.OrderStatus;
import com.onbok.book_hub.order.domain.repository.OrderRepository;
import com.onbok.book_hub.payment.domain.model.TossPayment;
import com.onbok.book_hub.user.domain.model.User;
import com.onbok.book_hub.user.domain.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(properties = {
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.data.elasticsearch.ElasticsearchDataAutoConfiguration,org.springframework.boot.autoconfigure.elasticsearch.ElasticsearchRestClientAutoConfiguration"
})
@Transactional
@DisplayName("OrderService 통합 테스트")
class OrderServiceTest {
    @Autowired private OrderCommandService orderCommandService;
    @Autowired private OrderQueryService orderQueryService;
    @Autowired private OrderRepository orderRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private BookRepository bookRepository;
    @Autowired private CartRepository cartRepository;

    private User testUser;
    private Book testBook;
    private Order testOrder;

    @BeforeEach
    void setUp() {
        // 테스트 사용자 생성
        testUser = User.builder()
                .uname("테스트 사용자")
                .email("test@test.com")
                .role("ROLE_USER")
                .regDate(LocalDate.now())
                .build();
        testUser = userRepository.save(testUser);

        // 테스트 도서 생성
        testBook = Book.builder()
                .title("테스트 도서")
                .author("테스트 저자")
                .company("테스트 출판사")
                .price(15000)
                .stock(100)
                .build();
        testBook = bookRepository.save(testBook);

        // 테스트 주문 생성
        testOrder = Order.builder()
                .user(testUser)
                .totalAmount(30000)
                .status(OrderStatus.PENDING)
                .build();
        testOrder = orderRepository.save(testOrder);
    }

    @AfterEach
    void tearDown() {
        if (testOrder != null) orderRepository.deleteById(testOrder.getId());
        if (testBook != null) bookRepository.deleteById(testBook.getId());
        if (testUser != null) userRepository.deleteById(testUser.getId());
    }

    @Test
    @DisplayName("주문 생성 - 정상 케이스")
    void createOrder_success() {
        // given
        Cart cart = Cart.builder()
                .user(testUser)
                .book(testBook)
                .quantity(2)
                .build();
        cartRepository.save(cart);

        List<Cart> cartList = new ArrayList<>();
        cartList.add(cart);

        TossPayment payment = TossPayment.builder()
                .paymentKey("test_payment_key")
                .id(1L)
                .totalPayment(30000)
                .build();

        DeliveryAddress address = DeliveryAddress.builder()
                .recipientName("홍길동")
                .zipCode("12345")
                .basicAddress("서울시 강남구")
                .detailAddress("101호")
                .tel("010-1234-5678")
                .build();

        // when
        Order createdOrder = orderCommandService.createOrder(testUser.getId(), cartList, payment, address);

        // then
        assertThat(createdOrder).isNotNull();
        assertThat(createdOrder.getUser().getId()).isEqualTo(testUser.getId());
        assertThat(createdOrder.getOrderItems()).hasSize(1);
        assertThat(createdOrder.getTotalAmount()).isEqualTo(30000);
        assertThat(createdOrder.getStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(createdOrder.getTossPayment()).isNotNull();
        assertThat(createdOrder.getDeliveryAddress()).isNotNull();
    }

    @Test
    @DisplayName("결제 완료 상태 전환")
    void completePayment() {
        // when
        orderCommandService.completePayment(testOrder.getId());

        // then
        Order updatedOrder = orderRepository.findById(testOrder.getId()).orElseThrow();
        assertThat(updatedOrder.getStatus()).isEqualTo(OrderStatus.PAYMENT_COMPLETED);
    }

    @Test
    @DisplayName("상품 준비 상태 전환")
    void startPreparing() {
        // given
        orderCommandService.completePayment(testOrder.getId());

        // when
        orderCommandService.startPreparing(testOrder.getId());

        // then
        Order updatedOrder = orderRepository.findById(testOrder.getId()).orElseThrow();
        assertThat(updatedOrder.getStatus()).isEqualTo(OrderStatus.PREPARING);
    }

    @Test
    @DisplayName("배송 시작 상태 전환")
    void shipOrder() {
        // given
        orderCommandService.completePayment(testOrder.getId());
        orderCommandService.startPreparing(testOrder.getId());

        // when
        orderCommandService.shipOrder(testOrder.getId());

        // then
        Order updatedOrder = orderRepository.findById(testOrder.getId()).orElseThrow();
        assertThat(updatedOrder.getStatus()).isEqualTo(OrderStatus.SHIPPED);
    }

    @Test
    @DisplayName("배송 완료 상태 전환")
    void deliverOrder() {
        // given
        orderCommandService.completePayment(testOrder.getId());
        orderCommandService.startPreparing(testOrder.getId());
        orderCommandService.shipOrder(testOrder.getId());

        // when
        orderCommandService.deliverOrder(testOrder.getId());

        // then
        Order updatedOrder = orderRepository.findById(testOrder.getId()).orElseThrow();
        assertThat(updatedOrder.getStatus()).isEqualTo(OrderStatus.DELIVERED);
    }

    @Test
    @DisplayName("주문 취소 - PENDING 상태에서")
    void cancelOrder_fromPending() {
        // when
        orderCommandService.cancelOrder(testOrder.getId());

        // then
        Order updatedOrder = orderRepository.findById(testOrder.getId()).orElseThrow();
        assertThat(updatedOrder.getStatus()).isEqualTo(OrderStatus.CANCELLED);
    }

    @Test
    @DisplayName("환불 - DELIVERED 상태에서")
    void refundOrder() {
        // given
        orderCommandService.completePayment(testOrder.getId());
        orderCommandService.startPreparing(testOrder.getId());
        orderCommandService.shipOrder(testOrder.getId());
        orderCommandService.deliverOrder(testOrder.getId());

        // when
        orderCommandService.refundOrder(testOrder.getId());

        // then
        Order updatedOrder = orderRepository.findById(testOrder.getId()).orElseThrow();
        assertThat(updatedOrder.getStatus()).isEqualTo(OrderStatus.REFUNDED);
    }

    @Test
    @DisplayName("잘못된 상태 전환 시도 시 예외 발생")
    void invalidStatusTransition() {
        // when & then
        assertThatThrownBy(() -> orderCommandService.shipOrder(testOrder.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("주문 상태를")
                .hasMessageContaining("변경할 수 없습니다");
    }

    @Test
    @DisplayName("사용자별 주문 조회")
    void getOrdersByUser() {
        // when
        List<Order> orders = orderQueryService.getOrdersByUser(testUser.getId());

        // then
        assertThat(orders).isNotEmpty();
        assertThat(orders).anyMatch(order -> order.getId() == testOrder.getId());
    }

    @Test
    @DisplayName("주문 조회 by ID")
    void findById() {
        // when
        Order foundOrder = orderRepository.findById(testOrder.getId()).orElseThrow();

        // then
        assertThat(foundOrder).isNotNull();
        assertThat(foundOrder.getId()).isEqualTo(testOrder.getId());
        assertThat(foundOrder.getStatus()).isEqualTo(OrderStatus.PENDING);
    }
}
