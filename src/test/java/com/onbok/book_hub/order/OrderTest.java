package com.onbok.book_hub.order;

import com.onbok.book_hub.order.domain.model.Order;
import com.onbok.book_hub.order.domain.model.OrderItem;
import com.onbok.book_hub.order.domain.model.OrderStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@DisplayName("Order 도메인 테스트")
class OrderTest {
    private Order order;

    @BeforeEach
    void setUp() {
        order = Order.builder()
                .orderDateTime(LocalDateTime.now())
                .totalAmount(50000)
                .status(OrderStatus.PENDING)
                .build();
    }

    @Test
    @DisplayName("결제 완료 - 정상 케이스")
    void completePayment_success() {
        // when
        order.completePayment();

        // then
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PAYMENT_COMPLETED);
    }

    @Test
    @DisplayName("상품 준비 시작 - 정상 케이스")
    void startPreparing_success() {
        // given
        order.completePayment();

        // when
        order.startPreparing();

        // then
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PREPARING);
    }

    @Test
    @DisplayName("배송 시작 - 정상 케이스")
    void ship_success() {
        // given
        order.completePayment();
        order.startPreparing();

        // when
        order.ship();

        // then
        assertThat(order.getStatus()).isEqualTo(OrderStatus.SHIPPED);
    }

    @Test
    @DisplayName("배송 완료 - 정상 케이스")
    void deliver_success() {
        // given
        order.completePayment();
        order.startPreparing();
        order.ship();

        // when
        order.deliver();

        // then
        assertThat(order.getStatus()).isEqualTo(OrderStatus.DELIVERED);
    }

    @Test
    @DisplayName("주문 취소 - PENDING 상태에서 가능")
    void cancel_fromPending() {
        // when
        order.cancel();

        // then
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
    }

    @Test
    @DisplayName("환불 - DELIVERED 상태에서 가능")
    void refund_fromDelivered() {
        // given
        order.completePayment();
        order.startPreparing();
        order.ship();
        order.deliver();

        // when
        order.refund();

        // then
        assertThat(order.getStatus()).isEqualTo(OrderStatus.REFUNDED);
    }

    @Test
    @DisplayName("잘못된 상태 전환 - PENDING에서 바로 SHIPPED로 불가")
    void invalidTransition_pendingToShipped() {
        // when & then
        assertThatThrownBy(() -> order.ship())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("주문 상태를")
                .hasMessageContaining("변경할 수 없습니다");
    }

    @Test
    @DisplayName("잘못된 상태 전환 - CANCELLED 상태에서 결제 완료 불가")
    void invalidTransition_cancelledToPaymentCompleted() {
        // given
        order.cancel();

        // when & then
        assertThatThrownBy(() -> order.completePayment())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("주문 상태를")
                .hasMessageContaining("변경할 수 없습니다");
    }

    @Test
    @DisplayName("정상적인 주문 플로우 전체 테스트")
    void normalOrderFlow() {
        // given
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);

        // when & then
        order.completePayment();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PAYMENT_COMPLETED);

        order.startPreparing();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PREPARING);

        order.ship();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.SHIPPED);

        order.deliver();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.DELIVERED);
    }

    @Test
    @DisplayName("OrderItem 추가 테스트")
    void addOrderItem() {
        // given
        OrderItem item1 = OrderItem.builder()
                .quantity(2)
                .subPrice(20000)
                .build();
        OrderItem item2 = OrderItem.builder()
                .quantity(1)
                .subPrice(15000)
                .build();

        // when
        order.addOrderItem(item1);
        order.addOrderItem(item2);

        // then
        assertThat(order.getOrderItems()).hasSize(2);
        assertThat(order.getOrderItems()).contains(item1, item2);
        assertThat(item1.getOrder()).isEqualTo(order);
        assertThat(item2.getOrder()).isEqualTo(order);
    }
}
