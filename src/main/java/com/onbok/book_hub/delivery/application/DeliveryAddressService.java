package com.onbok.book_hub.delivery.application;


import com.onbok.book_hub.common.exception.ErrorCode;
import com.onbok.book_hub.common.exception.ExpectedException;
import com.onbok.book_hub.delivery.domain.model.DeliveryAddress;
import com.onbok.book_hub.delivery.domain.repository.DeliveryAddressRepository;
import com.onbok.book_hub.user.domain.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DeliveryAddressService {
    private final DeliveryAddressRepository deliveryAddressRepository;

    public DeliveryAddress findById(Long id) {
        return deliveryAddressRepository.findById(id).orElseThrow(() -> new ExpectedException(ErrorCode.DELIVERY_ADRESS_NOT_FOUND));
    }

    @Transactional
    public DeliveryAddress insertDeliveryAddress(User user, DeliveryAddress deliveryAddress) {
        DeliveryAddress newAddress = DeliveryAddress.builder()
                .user(user)
                .recipientName(deliveryAddress.getRecipientName())
                .zipCode(deliveryAddress.getZipCode())
                .basicAddress(deliveryAddress.getBasicAddress())
                .detailAddress(deliveryAddress.getDetailAddress())
                .tel(deliveryAddress.getTel())
                .memo(deliveryAddress.getMemo())
                .build();
        return deliveryAddressRepository.save(newAddress);
    }

    /**
     * 특정 사용자의 모든 배송지 조회
     */
    public List<DeliveryAddress> findByUser(User user) {
        return deliveryAddressRepository.findByUser(user);
    }

    /**
     * 특정 사용자의 배송지 개수 조회
     */
    public long countByUser(User user) {
        return deliveryAddressRepository.countByUser(user);
    }
}
