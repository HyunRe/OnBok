package com.onbok.book_hub.payment.presentation.view;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.onbok.book_hub.payment.application.TossPaymentService;
import com.onbok.book_hub.payment.domain.model.TossPayment;
import com.onbok.book_hub.payment.dto.PaymentApproveRequestDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.OffsetDateTime;
import java.util.Map;

@Controller
@RequestMapping("/view/payments")
@RequiredArgsConstructor
public class PaymentViewController {
    private final TossPaymentService tossPaymentService;

    @GetMapping("/success")
    public String success(@Valid @RequestBody PaymentApproveRequestDto paymentApproveRequestDto) {
        // 결제 승인 요청 생성
        System.out.println("===== 결제 승인 요청 데이터: " + paymentApproveRequestDto);
        try {
            String jsonResult = tossPaymentService.approvePayment(paymentApproveRequestDto);
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> result = objectMapper.readValue(jsonResult, new TypeReference<Map<String, Object>>() { });
            System.out.println("결제 승인 성공: " + result);
            TossPayment tossPayment = TossPayment.builder()
                    .id((Long) result.get("orderId"))
                    .paymentKey((String) result.get("paymentKey"))
                    .name((String) result.get("orderName"))
                    .status((String) result.get("status"))
                    .approvalTime(OffsetDateTime.parse((String) result.get("approvedAt")).toLocalDateTime())
                    .paymentType(result.get("card") != null ? "card" : "other")
                    .totalPayment((int) result.get("totalAmount"))
                    .version((String) result.get("version"))
                    .build();
            tossPaymentService.insertTossPayment(tossPayment);
            return "redirect:/order/createOrder?pid=" + result.get("orderId") + "&did=" + paymentApproveRequestDto.getDeliveryId();
        } catch (Exception e) {
            System.out.println("결제 승인중 오류 발생: " + e.getMessage());
            return "redirect:/mall/cart";
        }
    }
}
