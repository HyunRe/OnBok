package com.onbok.book_hub.order.domain.repository;

import com.onbok.book_hub.order.domain.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    // 개별 사용자
    List<Order> findByUserId(Long userId);
    List<Order> findByUserIdOrderByIdDesc(Long userId);

    // 관리자 - 기간 설정
    List<Order> findByOrderDateTimeBetween(LocalDateTime start, LocalDateTime end);
}
