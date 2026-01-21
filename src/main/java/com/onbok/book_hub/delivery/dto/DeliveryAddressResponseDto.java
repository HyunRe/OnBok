package com.onbok.book_hub.delivery.dto;

import com.onbok.book_hub.delivery.domain.model.DeliveryAddress;

public record DeliveryAddressResponseDto(
        Long id,
        String recipientName,
        String zipCode,
        String basicAddress,
        String detailAddress,
        String tel,
        String memo
) {
    public static DeliveryAddressResponseDto from(DeliveryAddress deliveryAddress) {
        return new DeliveryAddressResponseDto(
                deliveryAddress.getId(),
                deliveryAddress.getRecipientName(),
                deliveryAddress.getZipCode(),
                deliveryAddress.getBasicAddress(),
                deliveryAddress.getDetailAddress(),
                deliveryAddress.getTel(),
                deliveryAddress.getMemo()
        );
    }
}
