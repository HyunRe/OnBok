package com.onbok.book_hub.payment.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Toss Payments Webhook 요청 DTO
 * Toss에서 결제 상태 변경 시 전송하는 데이터
 */
@Getter
@NoArgsConstructor
public class TossWebhookRequestDto {
    private String eventType;           // 이벤트 타입 (예: "PAYMENT_STATUS_CHANGED")
    private LocalDateTime createdAt;    // 이벤트 발생 시각
    private PaymentData data;           // 결제 정보

    @Getter
    @NoArgsConstructor
    public static class PaymentData {
        private String paymentKey;          // 결제 고유 키
        private String orderId;             // 주문 ID
        private String status;              // 결제 상태 (READY, IN_PROGRESS, WAITING_FOR_DEPOSIT, DONE, CANCELED, PARTIAL_CANCELED, ABORTED, EXPIRED)
        private LocalDateTime approvedAt;   // 결제 승인 시각
        private String orderName;           // 주문명
        private String method;              // 결제 수단 (카드, 가상계좌, 계좌이체 등)
        private Integer totalAmount;        // 총 결제 금액
        private Integer balanceAmount;      // 취소 가능 잔액
        private String version;             // API 버전
    }
}
