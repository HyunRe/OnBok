package com.onbok.book_hub.cart.dto.response;

import com.onbok.book_hub.cart.domain.model.Cart;

public record CartResponseDto(
        Long id,
        Long userId,
        Long bookId,
        String title,
        String imageUrl,
        int price,
        int quantity,
        int subTotal
) {
    // 정적 팩토리 메서드
    public static CartResponseDto from(Cart cart) {
        return new CartResponseDto(
                cart.getId(),
                cart.getUser().getId(),
                cart.getBook().getId(),
                cart.getBook().getTitle(),
                cart.getBook().getImageUrl(),
                cart.getBook().getPrice(),
                cart.getQuantity(),
                cart.getBook().getPrice() * cart.getQuantity()
        );
    }
}
