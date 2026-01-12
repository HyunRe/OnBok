package com.onbok.book_hub.order.dto;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderCreateRequestDto {
    private Long paymentId;
    private Long deliveryAddressId;
}
