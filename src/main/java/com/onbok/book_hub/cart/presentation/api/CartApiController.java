package com.onbok.book_hub.cart.presentation.api;

import com.onbok.book_hub.cart.application.CartCalculationService;
import com.onbok.book_hub.cart.application.CartService;
import com.onbok.book_hub.cart.domain.model.Cart;
import com.onbok.book_hub.cart.dto.request.CartAddRequestDto;
import com.onbok.book_hub.common.annotation.CurrentUser;
import com.onbok.book_hub.common.response.OnBokResponse;
import com.onbok.book_hub.user.domain.model.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@Tag(name = "Cart", description = "장바구니 API")
@RestController
@RequestMapping("/api/carts")
@RequiredArgsConstructor
public class CartApiController {
    private final CartService cartService;
    private final CartCalculationService cartCalculationService;

    @Operation(summary = "장바구니 수량 변경", description = "장바구니 아이템의 수량을 수정합니다")
    @PostMapping("/update")
    public OnBokResponse<Map<String, Object>> updateCart(@CurrentUser User user,
                                                         @Valid @RequestBody CartAddRequestDto cartAddRequestDto) {
        Long userId = user.getId();
        Long id = cartAddRequestDto.getId();
        int quantity = cartAddRequestDto.getQuantity();

        int subTotal = 0;
        if (quantity == 0) {
            cartService.removeFromCart(id);
        } else {
            Cart cart = cartService.findById(id);
            cart.updateQuantity(quantity);
            cartService.updateCart(cart);
            subTotal = cart.getBook().getPrice() * quantity;
        }

        List<Cart> cartList = cartService.getCartItemsByUser(userId);
        Map<String, Object> result = cartCalculationService.calculateUpdateResult(cartList, subTotal);

        return OnBokResponse.success(result);
    }
}
