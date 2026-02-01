package com.onbok.book_hub.repository;

import com.onbok.book_hub.book.domain.model.book.Book;
import com.onbok.book_hub.book.domain.repository.book.BookRepository;
import com.onbok.book_hub.delivery.domain.model.DeliveryAddress;
import com.onbok.book_hub.delivery.domain.repository.DeliveryAddressRepository;
import com.onbok.book_hub.order.domain.model.Order;
import com.onbok.book_hub.order.domain.model.OrderItem;
import com.onbok.book_hub.order.domain.model.OrderStatus;
import com.onbok.book_hub.order.domain.repository.OrderRepository;
import com.onbok.book_hub.payment.domain.model.TossPayment;
import com.onbok.book_hub.user.domain.model.User;
import com.onbok.book_hub.user.domain.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@EntityScan(basePackages = "com.onbok.book_hub")
@EnableJpaRepositories(basePackages = "com.onbok.book_hub")
@DisplayName("Order Repository 계층 테스트 - @DataJpaTest")
class OrderRepositoryTest {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private DeliveryAddressRepository deliveryAddressRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("주문 저장 - 정상 케이스")
    void saveOrder_success() {
        // given
        User user = createUser("test@test.com");
        Book book = createBook("자바의 정석", 30000, 100);
        DeliveryAddress address = createDeliveryAddress(user, "집");

        TossPayment payment = TossPayment.builder()
                .paymentKey("test_key")
                .totalPayment(60000)
                .build();

        Order order = Order.builder()
                .user(user)
                .deliveryAddress(address)
                .totalAmount(60000)
                .status(OrderStatus.PENDING)
                .tossPayment(payment)
                .build();

        OrderItem orderItem = OrderItem.builder()
                .order(order)
                .book(book)
                .quantity(2)
                .build();

        order.getOrderItems().add(orderItem);

        // when
        Order savedOrder = orderRepository.save(order);
        entityManager.flush();
        entityManager.clear();

        // then
        assertThat(savedOrder.getId()).isNotNull();
        assertThat(savedOrder.getUser().getEmail()).isEqualTo("test@test.com");
        assertThat(savedOrder.getTotalAmount()).isEqualTo(60000);
        assertThat(savedOrder.getStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(savedOrder.getOrderItems()).hasSize(1);
    }

    @Test
    @DisplayName("사용자별 주문 목록 조회 - findByUserId")
    void findByUserId_success() {
        // given
        User user = createUser("user@test.com");
        Book book1 = createBook("책1", 10000, 50);
        Book book2 = createBook("책2", 20000, 30);
        DeliveryAddress address = createDeliveryAddress(user, "집");

        Order order1 = createOrder(user, address, 10000, OrderStatus.PENDING);
        Order order2 = createOrder(user, address, 20000, OrderStatus.PAYMENT_COMPLETED);

        entityManager.flush();
        entityManager.clear();

        // when
        List<Order> orders = orderRepository.findByUserIdOrderByIdDesc(user.getId());

        // then
        assertThat(orders).hasSize(2);
        assertThat(orders.get(0).getId()).isEqualTo(order2.getId()); // 최신순 정렬
        assertThat(orders.get(1).getId()).isEqualTo(order1.getId());
    }

    @Test
    @DisplayName("주문 상태별 조회 - findByStatus")
    void findByStatus_success() {
        // given
        User user = createUser("user@test.com");
        DeliveryAddress address = createDeliveryAddress(user, "집");

        Order pendingOrder1 = createOrder(user, address, 10000, OrderStatus.PENDING);
        Order pendingOrder2 = createOrder(user, address, 20000, OrderStatus.PENDING);
        Order completedOrder = createOrder(user, address, 30000, OrderStatus.PAYMENT_COMPLETED);

        entityManager.flush();
        entityManager.clear();

        // when
        List<Order> pendingOrders = orderRepository.findByStatus(OrderStatus.PENDING);
        List<Order> completedOrders = orderRepository.findByStatus(OrderStatus.PAYMENT_COMPLETED);

        // then
        assertThat(pendingOrders).hasSize(2);
        assertThat(completedOrders).hasSize(1);
    }

    @Test
    @DisplayName("주문 ID로 조회 - findById")
    void findById_success() {
        // given
        User user = createUser("user@test.com");
        DeliveryAddress address = createDeliveryAddress(user, "집");
        Order order = createOrder(user, address, 50000, OrderStatus.PENDING);

        entityManager.flush();
        entityManager.clear();

        // when
        Order foundOrder = orderRepository.findById(order.getId()).orElse(null);

        // then
        assertThat(foundOrder).isNotNull();
        assertThat(foundOrder.getId()).isEqualTo(order.getId());
        assertThat(foundOrder.getTotalAmount()).isEqualTo(50000);
        assertThat(foundOrder.getUser().getId()).isEqualTo(user.getId());
    }

    @Test
    @DisplayName("사용자와 상태별 주문 조회 - findByUserIdAndStatus")
    void findByUserIdAndStatus_success() {
        // given
        User user1 = createUser("user1@test.com");
        User user2 = createUser("user2@test.com");
        DeliveryAddress address1 = createDeliveryAddress(user1, "집");
        DeliveryAddress address2 = createDeliveryAddress(user2, "회사");

        Order user1PendingOrder = createOrder(user1, address1, 10000, OrderStatus.PENDING);
        Order user1CompletedOrder = createOrder(user1, address1, 20000, OrderStatus.PAYMENT_COMPLETED);
        Order user2PendingOrder = createOrder(user2, address2, 30000, OrderStatus.PENDING);

        entityManager.flush();
        entityManager.clear();

        // when
        List<Order> user1PendingOrders = orderRepository.findByUserIdAndStatus(user1.getId(), OrderStatus.PENDING);
        List<Order> user1CompletedOrders = orderRepository.findByUserIdAndStatus(user1.getId(), OrderStatus.PAYMENT_COMPLETED);

        // then
        assertThat(user1PendingOrders).hasSize(1);
        assertThat(user1PendingOrders.get(0).getId()).isEqualTo(user1PendingOrder.getId());

        assertThat(user1CompletedOrders).hasSize(1);
        assertThat(user1CompletedOrders.get(0).getId()).isEqualTo(user1CompletedOrder.getId());
    }

    @Test
    @DisplayName("배송지 포함 주문 저장 - DeliveryAddress 연관관계")
    void saveOrder_withDeliveryAddress() {
        // given
        User user = createUser("user@test.com");
        DeliveryAddress address = createDeliveryAddress(user, "회사");

        Order order = createOrder(user, address, 100000, OrderStatus.PENDING);

        entityManager.flush();
        entityManager.clear();

        // when
        Order foundOrder = orderRepository.findById(order.getId()).orElseThrow();

        // then
        assertThat(foundOrder.getDeliveryAddress()).isNotNull();
        assertThat(foundOrder.getDeliveryAddress().getAlias()).isEqualTo("회사");
        assertThat(foundOrder.getDeliveryAddress().getRecipientName()).isEqualTo("홍길동");
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

    private Order createOrder(User user, DeliveryAddress address, int totalAmount, OrderStatus status) {
        TossPayment payment = TossPayment.builder()
                .paymentKey("test_payment_key_" + System.currentTimeMillis())
                .totalPayment(totalAmount)
                .build();

        Order order = Order.builder()
                .user(user)
                .deliveryAddress(address)
                .totalAmount(totalAmount)
                .status(status)
                .tossPayment(payment)
                .build();

        return orderRepository.save(order);
    }
}
