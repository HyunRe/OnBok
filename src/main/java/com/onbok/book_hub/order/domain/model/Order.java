package com.onbok.book_hub.order.domain.model;

import com.onbok.book_hub.common.domain.BaseTime;
import com.onbok.book_hub.delivery.domain.model.DeliveryAddress;
import com.onbok.book_hub.payment.domain.model.TossPayment;
import com.onbok.book_hub.user.domain.model.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order extends BaseTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItem> orderItems = new ArrayList<>();     // 빈 리스트로 초기화

    private LocalDateTime orderDateTime;
    private int totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status = OrderStatus.PENDING;  // 기본값: 주문 대기

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "payment_id", referencedColumnName = "id")
    private TossPayment tossPayment;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "delivery_address_id", referencedColumnName = "id")
    private DeliveryAddress deliveryAddress;

    @Builder
    private Order(User user, LocalDateTime orderDateTime, int totalAmount, OrderStatus status, TossPayment tossPayment, DeliveryAddress deliveryAddress, List<OrderItem> orderItems) {
        this.user = user;
        this.orderDateTime = orderDateTime != null ? orderDateTime : LocalDateTime.now();
        this.totalAmount = totalAmount;
        this.status = status != null ? status : OrderStatus.PENDING;
        this.tossPayment = tossPayment;
        this.deliveryAddress = deliveryAddress;
        // 외부에서 받은 리스트를 세팅하면서 양방향 관계를 여기서 다 맺어줌
        if (orderItems != null) {
            this.orderItems = orderItems;
            for (OrderItem item : orderItems) {
                item.setOrder(this); // OrderItem들에게 주인이 누구인지 자동 설정
            }
        } else {
            this.orderItems = new ArrayList<>();
        }
    }

    // 연관관계 메소드 추가
    public void addOrderItem(OrderItem orderItem) {
        if (this.orderItems == null)
            this.orderItems = new ArrayList<>();
        this.orderItems.add(orderItem);
        orderItem.setOrder(this);
    }

    // 주문 상태 전환 메서드
    public void changeStatus(OrderStatus newStatus) {
        if (!this.status.canTransitionTo(newStatus)) {
            throw new IllegalStateException(
                    String.format("주문 상태를 %s에서 %s로 변경할 수 없습니다.",
                            this.status.getDescription(), newStatus.getDescription())
            );
        }
        this.status = newStatus;
    }

    // 편의 메서드
    public void completePayment() {
        changeStatus(OrderStatus.PAYMENT_COMPLETED);
    }

    public void startPreparing() {
        changeStatus(OrderStatus.PREPARING);
    }

    public void ship() {
        changeStatus(OrderStatus.SHIPPED);
    }

    public void deliver() {
        changeStatus(OrderStatus.DELIVERED);
    }

    public void cancel() {
        changeStatus(OrderStatus.CANCELLED);
    }

    public void refund() {
        changeStatus(OrderStatus.REFUNDED);
    }
}
