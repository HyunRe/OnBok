package com.onbok.book_hub.order;

import com.onbok.book_hub.order.domain.model.OrderStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@DisplayName("OrderStatus 상태 전환 테스트")
class OrderStatusTest {

    @Test
    @DisplayName("PENDING -> PAYMENT_COMPLETED 전환 가능")
    void pending_to_paymentCompleted() {
        // given
        OrderStatus current = OrderStatus.PENDING;

        // when & then
        assertThat(current.canTransitionTo(OrderStatus.PAYMENT_COMPLETED)).isTrue();
    }

    @Test
    @DisplayName("PENDING -> CANCELLED 전환 가능")
    void pending_to_cancelled() {
        // given
        OrderStatus current = OrderStatus.PENDING;

        // when & then
        assertThat(current.canTransitionTo(OrderStatus.CANCELLED)).isTrue();
    }

    @Test
    @DisplayName("PENDING -> SHIPPED 직접 전환 불가")
    void pending_to_shipped_invalid() {
        // given
        OrderStatus current = OrderStatus.PENDING;

        // when & then
        assertThat(current.canTransitionTo(OrderStatus.SHIPPED)).isFalse();
    }

    @Test
    @DisplayName("PAYMENT_COMPLETED -> PREPARING 전환 가능")
    void paymentCompleted_to_preparing() {
        // given
        OrderStatus current = OrderStatus.PAYMENT_COMPLETED;

        // when & then
        assertThat(current.canTransitionTo(OrderStatus.PREPARING)).isTrue();
    }

    @Test
    @DisplayName("PAYMENT_COMPLETED -> CANCELLED 전환 가능")
    void paymentCompleted_to_cancelled() {
        // given
        OrderStatus current = OrderStatus.PAYMENT_COMPLETED;

        // when & then
        assertThat(current.canTransitionTo(OrderStatus.CANCELLED)).isTrue();
    }

    @Test
    @DisplayName("PREPARING -> SHIPPED 전환 가능")
    void preparing_to_shipped() {
        // given
        OrderStatus current = OrderStatus.PREPARING;

        // when & then
        assertThat(current.canTransitionTo(OrderStatus.SHIPPED)).isTrue();
    }

    @Test
    @DisplayName("SHIPPED -> DELIVERED 전환 가능")
    void shipped_to_delivered() {
        // given
        OrderStatus current = OrderStatus.SHIPPED;

        // when & then
        assertThat(current.canTransitionTo(OrderStatus.DELIVERED)).isTrue();
    }

    @Test
    @DisplayName("DELIVERED -> REFUNDED 전환 가능")
    void delivered_to_refunded() {
        // given
        OrderStatus current = OrderStatus.DELIVERED;

        // when & then
        assertThat(current.canTransitionTo(OrderStatus.REFUNDED)).isTrue();
    }

    @Test
    @DisplayName("CANCELLED 상태에서는 더 이상 전환 불가")
    void cancelled_noTransition() {
        // given
        OrderStatus current = OrderStatus.CANCELLED;

        // when & then
        assertThat(current.canTransitionTo(OrderStatus.PENDING)).isFalse();
        assertThat(current.canTransitionTo(OrderStatus.PAYMENT_COMPLETED)).isFalse();
        assertThat(current.canTransitionTo(OrderStatus.PREPARING)).isFalse();
    }

    @Test
    @DisplayName("REFUNDED 상태에서는 더 이상 전환 불가")
    void refunded_noTransition() {
        // given
        OrderStatus current = OrderStatus.REFUNDED;

        // when & then
        assertThat(current.canTransitionTo(OrderStatus.DELIVERED)).isFalse();
        assertThat(current.canTransitionTo(OrderStatus.CANCELLED)).isFalse();
    }

    @Test
    @DisplayName("정상적인 주문 플로우 전체 검증")
    void normalOrderFlow() {
        // PENDING -> PAYMENT_COMPLETED -> PREPARING -> SHIPPED -> DELIVERED
        OrderStatus status = OrderStatus.PENDING;

        assertThat(status.canTransitionTo(OrderStatus.PAYMENT_COMPLETED)).isTrue();
        status = OrderStatus.PAYMENT_COMPLETED;

        assertThat(status.canTransitionTo(OrderStatus.PREPARING)).isTrue();
        status = OrderStatus.PREPARING;

        assertThat(status.canTransitionTo(OrderStatus.SHIPPED)).isTrue();
        status = OrderStatus.SHIPPED;

        assertThat(status.canTransitionTo(OrderStatus.DELIVERED)).isTrue();
    }
}
