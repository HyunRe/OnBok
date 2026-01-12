package com.onbok.book_hub.cart.application;

import com.onbok.book_hub.cart.domain.model.Cart;
import com.onbok.book_hub.cart.dto.response.CartResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CartCalculationService {
    private static final int DELIVERY_COST = 3000;

    /**
     * 장바구니 총 금액 계산
     */
    public Map<String, Object> calculateCartSummary(List<Cart> cartList) {
        int totalPrice = 0;
        List<CartResponseDto> cartResponseList = new ArrayList<>();

        for (Cart cart : cartList) {
            CartResponseDto cartResponse = CartResponseDto.from(cart);
            totalPrice += cartResponse.subTotal();
            cartResponseList.add(cartResponse);
        }

        int totalPriceIncludingDeliveryCost = totalPrice + DELIVERY_COST;

        Map<String, Object> result = new HashMap<>();
        result.put("cartDtoList", cartResponseList);
        result.put("totalPrice", totalPrice);
        result.put("deliveryCost", DELIVERY_COST);
        result.put("totalPriceIncludingDeliveryCost", totalPriceIncludingDeliveryCost);

        return result;
    }

    /**
     * 장바구니 업데이트 후 계산 결과 반환
     */
    public Map<String, Object> calculateUpdateResult(List<Cart> cartList, int subTotal) {
        int totalPrice = 0;
        for (Cart cart : cartList) {
            totalPrice += cart.getBook().getPrice() * cart.getQuantity();
        }

        int totalPriceIncludingDeliveryCost = totalPrice + DELIVERY_COST;

        return Map.of(
                "success", true,
                "subTotal", subTotal,
                "totalPrice", totalPrice,
                "deliveryCost", DELIVERY_COST,
                "totalPriceIncludingDeliveryCost", totalPriceIncludingDeliveryCost
        );
    }
}
