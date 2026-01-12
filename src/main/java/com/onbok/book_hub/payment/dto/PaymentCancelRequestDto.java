package com.onbok.book_hub.payment.dto;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentCancelRequestDto {
    private String paymentKey;
    private String cancelReason;
}
