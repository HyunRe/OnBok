package com.onbok.book_hub.payment.presentation.view;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.onbok.book_hub.cart.application.CartService;
import com.onbok.book_hub.cart.domain.model.Cart;
import com.onbok.book_hub.common.annotation.CurrentUser;
import com.onbok.book_hub.order.application.OrderCommandService;
import com.onbok.book_hub.order.domain.model.Order;
import com.onbok.book_hub.payment.application.TossPaymentService;
import com.onbok.book_hub.payment.domain.model.TossPayment;
import com.onbok.book_hub.payment.dto.PaymentApproveRequestDto;
import com.onbok.book_hub.user.domain.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping("/view/payments")
@RequiredArgsConstructor
public class PaymentViewController {
    private final TossPaymentService tossPaymentService;
    private final OrderCommandService orderCommandService;
    private final CartService cartService;

    @GetMapping("/success")
    public String success(@RequestParam String paymentKey,
                         @RequestParam String orderId,
                         @RequestParam int amount,
                         @RequestParam Long deliveryId,
                         @CurrentUser User user,
                         RedirectAttributes redirectAttributes) {
        log.info("===== 결제 승인 요청 - paymentKey: {}, orderId: {}, amount: {}, deliveryId: {}",
                paymentKey, orderId, amount, deliveryId);

        try {
            // 1. 결제 승인
            PaymentApproveRequestDto approveRequest = PaymentApproveRequestDto.builder()
                    .paymentKey(paymentKey)
                    .orderId(orderId)
                    .amount(amount)
                    .build();

            String jsonResult = tossPaymentService.approvePayment(approveRequest);
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> result = objectMapper.readValue(jsonResult, new TypeReference<Map<String, Object>>() {});
            log.info("결제 승인 성공: {}", result);

            // 2. TossPayment 엔티티 생성 및 저장
            TossPayment tossPayment = TossPayment.builder()
                    .paymentKey((String) result.get("paymentKey"))
                    .name((String) result.get("orderName"))
                    .status((String) result.get("status"))
                    .approvalTime(OffsetDateTime.parse((String) result.get("approvedAt")).toLocalDateTime())
                    .paymentType(result.get("card") != null ? "card" : "other")
                    .totalPayment((Integer) result.get("totalAmount"))
                    .version((String) result.get("version"))
                    .build();
            TossPayment savedPayment = tossPaymentService.insertTossPayment(tossPayment);

            // 3. 장바구니 조회
            List<Cart> cartList = cartService.getCartItemsByUser(user.getId());

            if (!cartList.isEmpty()) {
                // 4. 주문 생성 (deliveryAddressId 사용)
                Order order = orderCommandService.createOrder(
                        user.getId(),
                        cartList,
                        savedPayment,
                        deliveryId
                );
                log.info("주문 생성 완료 - orderId: {}", order.getId());

                // 5. 장바구니 비우기
                cartService.clearCart(user.getId());
                log.info("장바구니 비우기 완료");

                redirectAttributes.addFlashAttribute("msg", "결제 및 주문이 완료되었습니다.");
                return "redirect:/view/orders/detail/" + order.getId();
            } else {
                log.warn("장바구니가 비어있음");
                redirectAttributes.addFlashAttribute("msg", "장바구니가 비어있습니다.");
                return "redirect:/view/carts";
            }

        } catch (Exception e) {
            log.error("결제 승인 또는 주문 생성 중 오류 발생", e);
            redirectAttributes.addFlashAttribute("errorMessage", "결제 처리 중 오류가 발생했습니다: " + e.getMessage());
            return "redirect:/payment/failure";
        }
    }

    @GetMapping("/failure")
    public String failure(@RequestParam(required = false) String code,
                         @RequestParam(required = false) String message,
                         @RequestParam(required = false) String errorMessage,
                         Model model) {
        String finalMessage = errorMessage != null ? errorMessage :
                             (message != null ? message : "결제 처리 중 오류가 발생했습니다.");

        log.error("===== 결제 실패 - code: {}, message: {}", code, finalMessage);
        model.addAttribute("errorMessage", finalMessage);
        return "payment/failure";
    }
}
