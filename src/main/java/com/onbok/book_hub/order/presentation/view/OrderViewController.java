package com.onbok.book_hub.order.presentation.view;

import com.onbok.book_hub.book.dto.BookStateDto;
import com.onbok.book_hub.cart.application.CartService;
import com.onbok.book_hub.cart.domain.model.Cart;
import com.onbok.book_hub.common.annotation.CurrentUser;
import com.onbok.book_hub.common.aspect.CheckPermission;
import com.onbok.book_hub.common.aspect.LogExecutionTime;
import com.onbok.book_hub.common.exception.ErrorCode;
import com.onbok.book_hub.common.exception.ExpectedException;
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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
        List<Cart> cartList = cartService.getCartItemsByUser(user.getId());
        if (!cartList.isEmpty()) {
            // 배송지 ID를 직접 전달 (배송지 관리에서 미리 등록된 배송지 사용)
            orderCommandService.createOrder(user.getId(), cartList, tossPayment, createRequestDto.getDeliveryAddressId());
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

    @GetMapping("/detail/{id}")
    public String detail(@CurrentUser User user, @PathVariable Long id, Model model) {
        Order order = orderQueryService.findById(id);

        // 본인의 주문인지 확인
        if (!order.getUser().getId().equals(user.getId())) {
            throw new ExpectedException(ErrorCode.UNAUTHORIZED_ACCESS);
        }

        model.addAttribute("menu", "order");
        model.addAttribute("order", order);
        return "order/detail";
    }

    @PostMapping("/cancel/{id}")
    public String cancelOrder(@CurrentUser User user, @PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Order order = orderQueryService.findById(id);

            // 본인의 주문인지 확인
            if (!order.getUser().getId().equals(user.getId())) {
                throw new ExpectedException(ErrorCode.UNAUTHORIZED_ACCESS);
            }

            orderCommandService.cancelOrder(id);
            redirectAttributes.addFlashAttribute("msg", "주문이 취소되었습니다.");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("msg", "주문 취소 실패: " + e.getMessage());
        } catch (ExpectedException e) {
            redirectAttributes.addFlashAttribute("msg", "주문 취소 실패: " + e.getErrorCode().getMessage());
        }
        return "redirect:/view/orders/detail/" + id;
    }

    @PostMapping("/refund/{id}")
    public String refundOrder(@CurrentUser User user, @PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Order order = orderQueryService.findById(id);

            // 본인의 주문인지 확인
            if (!order.getUser().getId().equals(user.getId())) {
                throw new ExpectedException(ErrorCode.UNAUTHORIZED_ACCESS);
            }

            orderCommandService.refundOrder(id);
            redirectAttributes.addFlashAttribute("msg", "환불이 완료되었습니다.");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("msg", "환불 실패: " + e.getMessage());
        } catch (ExpectedException e) {
            redirectAttributes.addFlashAttribute("msg", "환불 실패: " + e.getErrorCode().getMessage());
        }
        return "redirect:/view/orders/detail/" + id;
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
