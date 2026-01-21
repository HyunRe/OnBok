package com.onbok.book_hub.cart.presentation.view;

import com.onbok.book_hub.cart.application.CartCalculationService;
import com.onbok.book_hub.cart.application.CartService;
import com.onbok.book_hub.cart.domain.model.Cart;
import com.onbok.book_hub.common.annotation.CurrentUser;
import com.onbok.book_hub.delivery.application.DeliveryAddressService;
import com.onbok.book_hub.delivery.dto.DeliveryAddressDto;
import com.onbok.book_hub.user.domain.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/view/carts")
@RequiredArgsConstructor
public class CartViewController {
    private final CartService cartService;
    private final CartCalculationService cartCalculationService;
    private final DeliveryAddressService deliveryAddressService;

    @Value("${toss.payment.client.key}")
    private String TOSS_CLIENT_KEY;

    @GetMapping
    public String cart(@CurrentUser User user, Model model) {
        List<Cart> cartList = cartService.getCartItemsByUser(user.getId());
        Map<String, Object> summary = cartCalculationService.calculateCartSummary(cartList);

        // Cart 엔티티를 CartResponseDto로 변환
        List<com.onbok.book_hub.cart.dto.response.CartResponseDto> cartDtoList = cartList.stream()
                .map(com.onbok.book_hub.cart.dto.response.CartResponseDto::from)
                .toList();

        // 배송지 목록 조회
        List<DeliveryAddressDto> deliveryAddresses = deliveryAddressService.getUserDeliveryAddresses(user);

        model.addAttribute("cartDtoList", cartDtoList);
        model.addAllAttributes(summary);
        model.addAttribute("TOSS_CLIENT_KEY", TOSS_CLIENT_KEY);
        model.addAttribute("user", user); // 사용자 정보 추가
        model.addAttribute("deliveryAddresses", deliveryAddresses); // 배송지 목록 추가
        return "cart/cart";
    }
}
