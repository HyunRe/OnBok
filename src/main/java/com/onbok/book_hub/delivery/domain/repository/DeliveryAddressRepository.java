package com.onbok.book_hub.delivery.domain.repository;

import com.onbok.book_hub.delivery.domain.model.DeliveryAddress;
import com.onbok.book_hub.user.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DeliveryAddressRepository extends JpaRepository<DeliveryAddress, Long> {
    /**
     * 특정 사용자의 모든 배송지 조회
     */
    List<DeliveryAddress> findByUser(User user);

    /**
     * 특정 사용자의 배송지 개수 조회
     */
    long countByUser(User user);

    // 사용자별 배송지 목록 조회
    List<DeliveryAddress> findByUserId(Long userId);

    // 사용자와 별칭으로 배송지 조회
    Optional<DeliveryAddress> findByUserIdAndAlias(Long userId, String alias);

    // 기본 배송지 조회

}
