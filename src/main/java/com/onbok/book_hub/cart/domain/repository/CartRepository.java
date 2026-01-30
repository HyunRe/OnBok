package com.onbok.book_hub.cart.domain.repository;

import com.onbok.book_hub.cart.domain.model.Cart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {
    // 사용자의 장바구니 목록을 조회
    List<Cart> findByUserId(Long userId);

    // 사용자와 도서로 장바구니 조회
    Optional<Cart> findByUserIdAndBookId(Long userId, Long bookId);

    // 사용자별 장바구니 전체 삭제
    void deleteByUserId(Long userId);

    // 도서별 장바구니 조회
    List<Cart> findByBookId(Long bookId);

    // 장바구니 개수 조회
    long countByUserId(Long userId);
}
