package com.onbok.book_hub.payment.dto;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentRefundRequestDto {
    private String paymentKey;
    private String cancelReason;
    private Integer refundAmount;  // null이면 전액 환불

    @Builder
    private PaymentRefundRequestDto(String paymentKey, String cancelReason, Integer refundAmount) {
        this.paymentKey = paymentKey;
        this.cancelReason = cancelReason;
        this.refundAmount = refundAmount;
    }
}
