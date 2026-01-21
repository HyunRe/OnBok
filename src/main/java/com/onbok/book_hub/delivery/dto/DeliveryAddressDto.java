package com.onbok.book_hub.delivery.dto;

import com.onbok.book_hub.delivery.domain.model.DeliveryAddress;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryAddressDto {
    private Long id;
    private String alias;
    private String recipientName;
    private String zipCode;
    private String basicAddress;
    private String detailAddress;
    private String tel;
    private String memo;

    public static DeliveryAddressDto from(DeliveryAddress deliveryAddress) {
        return DeliveryAddressDto.builder()
                .id(deliveryAddress.getId())
                .alias(deliveryAddress.getAlias())
                .recipientName(deliveryAddress.getRecipientName())
                .zipCode(deliveryAddress.getZipCode())
                .basicAddress(deliveryAddress.getBasicAddress())
                .detailAddress(deliveryAddress.getDetailAddress())
                .tel(deliveryAddress.getTel())
                .memo(deliveryAddress.getMemo())
                .build();
    }
}
