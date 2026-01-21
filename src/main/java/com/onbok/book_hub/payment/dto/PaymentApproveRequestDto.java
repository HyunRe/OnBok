package com.onbok.book_hub.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentApproveRequestDto {
    private String paymentKey;
    private String orderId;
    private Integer amount;
    private Long deliveryId;
}
