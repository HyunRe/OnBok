package com.onbok.book_hub.delivery.domain.repository;

import com.onbok.book_hub.delivery.domain.model.DeliveryAddress;
import com.onbok.book_hub.user.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DeliveryAddressRepository extends JpaRepository<DeliveryAddress, Long> {
    /**
     * 특정 사용자의 모든 배송지 조회
     */
    List<DeliveryAddress> findByUser(User user);

    /**
     * 특정 사용자의 배송지 개수 조회
     */
    long countByUser(User user);
}
