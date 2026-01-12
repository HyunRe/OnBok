package com.onbok.book_hub.order.presentation.view;

import com.onbok.book_hub.book.dto.BookStateDto;
import com.onbok.book_hub.cart.application.CartService;
import com.onbok.book_hub.cart.domain.model.Cart;
import com.onbok.book_hub.common.annotation.CurrentUser;
import com.onbok.book_hub.common.aspect.CheckPermission;
import com.onbok.book_hub.common.aspect.LogExecutionTime;
import com.onbok.book_hub.delivery.application.DeliveryAddressService;
import com.onbok.book_hub.delivery.domain.model.DeliveryAddress;
import com.onbok.book_hub.order.application.OrderCommandService;
import com.onbok.book_hub.order.application.OrderQueryService;
import com.onbok.book_hub.order.application.OrderStatisticsService;
import com.onbok.book_hub.order.domain.model.Order;
import com.onbok.book_hub.order.dto.OrderCreateRequestDto;
import com.onbok.book_hub.payment.application.TossPaymentService;
import com.onbok.book_hub.payment.domain.model.TossPayment;
import com.onbok.book_hub.user.domain.model.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/view/orders")
@RequiredArgsConstructor
public class OrderViewController {
    private final CartService cartService;
    private final DeliveryAddressService deliveryAddressService;
    private final OrderCommandService orderCommandService;
    private final OrderQueryService orderQueryService;
    private final OrderStatisticsService orderStatisticsService;
    private final TossPaymentService tossPaymentService;

    @GetMapping("/createOrder")
    public String createOrder(@CurrentUser User user,
                              @Valid @RequestBody OrderCreateRequestDto createRequestDto) {
        TossPayment tossPayment = tossPaymentService.findById(createRequestDto.getPaymentId());
        DeliveryAddress deliveryAddress = deliveryAddressService.findById(createRequestDto.getDeliveryAddressId());
        List<Cart> cartList = cartService.getCartItemsByUser(user.getId());
        if (!cartList.isEmpty()) {
            orderCommandService.createOrder(user.getId(), cartList, tossPayment, deliveryAddress);
        }
        return "redirect:/order/list";
    }

    @GetMapping("/list")
    public String list(@CurrentUser User user, Model model) {
        List<Order> orderList = orderQueryService.getOrdersByUser(user.getId());
        List<String> orderTitleList = orderQueryService.getOrderTitleList(orderList);

        model.addAttribute("menu", "order");
        model.addAttribute("orderList", orderList);
        model.addAttribute("orderTitleList", orderTitleList);
        return "order/list";
    }

    @GetMapping("/listAll")
    @CheckPermission("ROLE_ADMIN")
    public String listAll(Model model) {
        // 2024년 12월
        LocalDateTime startTime = LocalDateTime.of(2024, 12, 1, 0, 0);
        LocalDateTime endTime = LocalDateTime.of(2024, 12, 31, 23, 59, 59, 999999999);

        OrderQueryService.OrderStatistics statistics = orderQueryService.calculateOrderStatistics(startTime, endTime);
        List<String> orderTitleList = orderQueryService.getOrderTitleList(statistics.getOrderList());

        model.addAttribute("menu", "order");
        model.addAttribute("orderList", statistics.getOrderList());
        model.addAttribute("orderTitleList", orderTitleList);
        model.addAttribute("totalRevenue", statistics.getTotalRevenue());
        model.addAttribute("totalBooks", statistics.getTotalBooks());
        return "order/listAll";
    }

    @GetMapping("/bookState")
    @LogExecutionTime
    public String bookStat(Model model) {
        // 2026년 1월
        LocalDateTime startTime = LocalDateTime.of(2026, 1, 1, 0, 0);
        LocalDateTime endTime = LocalDateTime.of(2026, 1, 31, 23, 59, 59, 999999999);

        List<BookStateDto> bookStatList = orderStatisticsService.calculateBookStatistics(startTime, endTime);

        model.addAttribute("menu", "order");
        model.addAttribute("bookStatList", bookStatList);
        return "order/bookStat";
    }

    @GetMapping("/charts")
    @CheckPermission("ROLE_ADMIN")
    public String charts(Model model) {
        model.addAttribute("menu", "chart");
        return "order/charts";
    }
}
