package com.onbok.book_hub.common.admin;

import com.onbok.book_hub.order.application.OrderQueryService;
import com.onbok.book_hub.order.domain.model.Order;
import com.onbok.book_hub.user.application.UserQueryService;
import com.onbok.book_hub.user.domain.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/view/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminViewController {
    private final OrderQueryService orderQueryService;
    private final UserQueryService userQueryService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        // 총괄 주문 목록
        List<Order> allOrders = orderQueryService.getAllOrders();

        // 주문 통계
        Map<String, Long> orderStats = orderQueryService.getOrderStatisticsByStatus();

        // 사용자 목록
        List<User> allUsers = userQueryService.getUsers();

        model.addAttribute("menu", "admin");
        model.addAttribute("allOrders", allOrders);
        model.addAttribute("orderStats", orderStats);
        model.addAttribute("allUsers", allUsers);

        return "admin/dashboard";
    }
}
