package com.onbok.book_hub.cart.dto.request;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 장바구니 수량 업데이트 요청
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CartAddRequestDto {
    private Long id;
    private int quantity;  // 0일 경우 삭제
}
