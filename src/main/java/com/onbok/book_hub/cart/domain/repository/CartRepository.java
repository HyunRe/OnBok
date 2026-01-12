package com.onbok.book_hub.cart.domain.repository;

import com.onbok.book_hub.cart.domain.model.Cart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CartRepository extends JpaRepository<Cart, Long> {
    // 사용자의 장바구니 목록을 조회
    List<Cart> findByUserId(Long userId);
}
