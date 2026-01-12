package com.onbok.book_hub.payment.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PaymentApproveRequestDto {
    private String paymentKey;
    private Long orderId;
    private Long amount;
    private Long deliveryId;
}
