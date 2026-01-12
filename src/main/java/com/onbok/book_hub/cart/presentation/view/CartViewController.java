package com.onbok.book_hub.cart.presentation.view;

import com.onbok.book_hub.cart.application.CartCalculationService;
import com.onbok.book_hub.cart.application.CartService;
import com.onbok.book_hub.cart.domain.model.Cart;
import com.onbok.book_hub.common.annotation.CurrentUser;
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
    @Value("${toss.payment.client.key}")
    private String TOSS_CLIENT_KEY;

    @GetMapping
    public String cart(@CurrentUser User user, Model model) {
        List<Cart> cartList = cartService.getCartItemsByUser(user.getId());
        Map<String, Object> summary = cartCalculationService.calculateCartSummary(cartList);

        model.addAttribute("cartList", cartList);
        model.addAllAttributes(summary);
        model.addAttribute("TOSS_CLIENT_KEY", TOSS_CLIENT_KEY);
        return "cart/cart";
    }
}
