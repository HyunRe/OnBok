package com.onbok.book_hub.payment.presentation.api;

import com.onbok.book_hub.common.response.OnBokResponse;
import com.onbok.book_hub.payment.application.TossPaymentService;
import com.onbok.book_hub.payment.domain.model.TossPayment;
import com.onbok.book_hub.payment.dto.TossWebhookRequestDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Tag(name = "Payment", description = "결제 API")
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentApiController {
    private final TossPaymentService tossPaymentService;

    @Operation(summary = "결제 실패 처리", description = "결제 과정에서 발생한 실패 코드와 메시지를 수신하여 출력합니다.")
    @GetMapping("/failure")
    public String failure(@RequestParam String code, @RequestParam String message) {
        log.error("===== 결제 실패 코드: " + code + ", 메시지: " + message);
        return "결제 실패: " + message;
    }

    @Operation(summary = "Toss Payments Webhook", description = "Toss에서 결제 상태 변경 시 호출되는 Webhook 엔드포인트")
    @PostMapping("/webhook")
    public OnBokResponse<String> handleWebhook(@RequestBody TossWebhookRequestDto request) {
        try {
            log.info("Webhook 수신 - EventType: {}", request.getEventType());
            tossPaymentService.handleWebhook(request);
            return OnBokResponse.success("Webhook 처리 완료");
        } catch (Exception e) {
            log.error("Webhook 처리 실패", e);
            return OnBokResponse.error("Webhook 처리 실패: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(summary = "결제 상세 조회", description = "특정 결제의 상세 정보를 조회합니다")
    @GetMapping("/{paymentId}")
    public OnBokResponse<TossPayment> getPaymentDetail(@PathVariable Long paymentId) {
        TossPayment payment = tossPaymentService.findById(paymentId);
        return OnBokResponse.success(payment);
    }
}
