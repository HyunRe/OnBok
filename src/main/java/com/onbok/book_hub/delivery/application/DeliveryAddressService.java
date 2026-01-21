package com.onbok.book_hub.delivery.application;

import com.onbok.book_hub.common.exception.ErrorCode;
import com.onbok.book_hub.common.exception.ExpectedException;
import com.onbok.book_hub.delivery.domain.model.DeliveryAddress;
import com.onbok.book_hub.delivery.domain.repository.DeliveryAddressRepository;
import com.onbok.book_hub.delivery.dto.DeliveryAddressDto;
import com.onbok.book_hub.user.domain.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class DeliveryAddressService {
    private final DeliveryAddressRepository deliveryAddressRepository;

    /**
     * 사용자의 모든 배송지 조회
     */
    @Transactional(readOnly = true)
    public List<DeliveryAddressDto> getUserDeliveryAddresses(User user) {
        return deliveryAddressRepository.findByUser(user).stream()
                .map(DeliveryAddressDto::from)
                .collect(Collectors.toList());
    }

    /**
     * 배송지 ID로 조회
     */
    @Transactional(readOnly = true)
    public DeliveryAddress findById(Long id) {
        return deliveryAddressRepository.findById(id)
                .orElseThrow(() -> new ExpectedException(ErrorCode.DELIVERY_ADDRESS_NOT_FOUND));
    }

    /**
     * 배송지 DTO로 조회
     */
    @Transactional(readOnly = true)
    public DeliveryAddressDto getDeliveryAddressDto(Long id) {
        DeliveryAddress deliveryAddress = findById(id);
        return DeliveryAddressDto.from(deliveryAddress);
    }

    /**
     * 배송지 등록
     */
    public DeliveryAddress createDeliveryAddress(User user, DeliveryAddressDto dto) {
        log.info("배송지 등록 - userId: {}, alias: {}", user.getId(), dto.getAlias());

        DeliveryAddress deliveryAddress = DeliveryAddress.builder()
                .user(user)
                .alias(dto.getAlias())
                .recipientName(dto.getRecipientName())
                .zipCode(dto.getZipCode())
                .basicAddress(dto.getBasicAddress())
                .detailAddress(dto.getDetailAddress())
                .tel(dto.getTel())
                .memo(dto.getMemo())
                .build();

        return deliveryAddressRepository.save(deliveryAddress);
    }

    /**
     * 배송지 수정
     */
    public void updateDeliveryAddress(User user, Long id, DeliveryAddressDto dto) {
        log.info("배송지 수정 - userId: {}, addressId: {}", user.getId(), id);

        DeliveryAddress deliveryAddress = findById(id);

        // 본인의 배송지인지 확인
        if (!deliveryAddress.getUser().getId().equals(user.getId())) {
            throw new ExpectedException(ErrorCode.UNAUTHORIZED);
        }

        deliveryAddress.update(
                dto.getAlias(),
                dto.getRecipientName(),
                dto.getZipCode(),
                dto.getBasicAddress(),
                dto.getDetailAddress(),
                dto.getTel(),
                dto.getMemo()
        );
    }

    /**
     * 배송지 삭제
     */
    public void deleteDeliveryAddress(User user, Long id) {
        log.info("배송지 삭제 - userId: {}, addressId: {}", user.getId(), id);

        DeliveryAddress deliveryAddress = findById(id);

        // 본인의 배송지인지 확인
        if (!deliveryAddress.getUser().getId().equals(user.getId())) {
            throw new ExpectedException(ErrorCode.UNAUTHORIZED);
        }

        deliveryAddressRepository.delete(deliveryAddress);
    }

    /**
     * 특정 사용자의 모든 배송지 조회 (엔티티)
     */
    @Transactional(readOnly = true)
    public List<DeliveryAddress> findByUser(User user) {
        return deliveryAddressRepository.findByUser(user);
    }

    /**
     * 특정 사용자의 배송지 개수 조회
     */
    @Transactional(readOnly = true)
    public long countByUser(User user) {
        return deliveryAddressRepository.countByUser(user);
    }
}
