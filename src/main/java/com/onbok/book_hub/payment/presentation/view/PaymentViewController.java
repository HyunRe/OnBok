package com.onbok.book_hub.payment.presentation.view;

import com.onbok.book_hub.common.annotation.CurrentUser;
import com.onbok.book_hub.common.exception.ErrorCode;
import com.onbok.book_hub.common.exception.ExpectedException;
import com.onbok.book_hub.order.domain.model.Order;
import com.onbok.book_hub.payment.application.facade.PaymentOrderFacade;
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


@Slf4j
@Controller
@RequestMapping("/view/payments")
@RequiredArgsConstructor
public class PaymentViewController {
    private final PaymentOrderFacade paymentOrderFacade;

    @GetMapping("/success")
    public String success(@RequestParam String paymentKey,
                         @RequestParam String orderId,
                         @RequestParam int amount,
                         @RequestParam Long deliveryId,
                         @CurrentUser User user,
                         RedirectAttributes redirectAttributes) {
        log.info("===== 결제 승인 요청 - paymentKey: {}, orderId: {}, amount: {}, deliveryId: {}",
                paymentKey, orderId, amount, deliveryId);

        PaymentApproveRequestDto approveRequest = PaymentApproveRequestDto.builder()
                .paymentKey(paymentKey)
                .orderId(orderId)
                .amount(amount)
                .build();

        try {
            Order order = paymentOrderFacade.completePaymentAndCreateOrder(user.getId(), approveRequest, deliveryId);
            log.info("주문 생성 완료 - orderId: {}", order.getId());

            redirectAttributes.addFlashAttribute("msg", "결제 및 주문이 완료되었습니다.");
            return "redirect:/view/orders/detail/" + order.getId();

        } catch (ExpectedException e) {
            if (e.getErrorCode() == ErrorCode.CART_EMPTY) {
                log.warn("장바구니가 비어있음 - userId: {}", user.getId());
                redirectAttributes.addFlashAttribute("msg", e.getErrorCode().getMessage());
                return "redirect:/view/carts";
            }
            return redirectToFailure(redirectAttributes, e.getErrorCode().getMessage(), e);

        } catch (Exception e) {
            return redirectToFailure(redirectAttributes, e.getMessage(), e);
        }
    }

    private String redirectToFailure(RedirectAttributes redirectAttributes, String reason, Exception e) {
        log.error("결제 승인 또는 주문 생성 중 오류 발생", e);
        redirectAttributes.addFlashAttribute("errorMessage", "결제 처리 중 오류가 발생했습니다: " + reason);
        return "redirect:/view/payments/failure";
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
