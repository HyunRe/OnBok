package com.onbok.book_hub.order.domain.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public enum OrderStatus {
    PENDING("주문 대기"),           // 주문 생성, 결제 대기
    PAYMENT_COMPLETED("결제 완료"),  // 결제 완료
    PREPARING("상품 준비중"),        // 상품 준비
    SHIPPED("배송중"),              // 배송 시작
    DELIVERED("배송 완료"),         // 배송 완료
    CANCELLED("주문 취소"),         // 주문 취소
    REFUNDED("환불 완료");          // 환불 완료

    private String description;

    OrderStatus(String description) {
        this.description = description;
    }

    // 상태 전환 가능 여부 확인
    public boolean canTransitionTo(OrderStatus newStatus) {
        return switch (this) {
            case PENDING -> newStatus == PAYMENT_COMPLETED || newStatus == CANCELLED;
            case PAYMENT_COMPLETED -> newStatus == PREPARING || newStatus == CANCELLED;
            case PREPARING -> newStatus == SHIPPED || newStatus == CANCELLED;
            case SHIPPED -> newStatus == DELIVERED;
            case DELIVERED -> newStatus == REFUNDED;
            case CANCELLED, REFUNDED -> false; // 취소나 환불 상태에서는 더 이상 전환 불가
            default -> false;
        };
    }
}
