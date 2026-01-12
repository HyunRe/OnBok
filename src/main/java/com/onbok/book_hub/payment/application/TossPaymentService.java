package com.onbok.book_hub.payment.application;

import com.onbok.book_hub.common.exception.ErrorCode;
import com.onbok.book_hub.common.exception.ExpectedException;
import com.onbok.book_hub.order.application.OrderCommandService;
import com.onbok.book_hub.payment.domain.model.TossPayment;
import com.onbok.book_hub.payment.domain.repository.TossPaymentRepository;
import com.onbok.book_hub.payment.dto.PaymentApproveRequestDto;
import com.onbok.book_hub.payment.dto.PaymentCancelRequestDto;
import com.onbok.book_hub.payment.dto.PaymentRefundRequestDto;
import com.onbok.book_hub.payment.dto.TossWebhookRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class TossPaymentService {
    private final RestTemplate restTemplate;
    private final TossPaymentRepository tossPaymentRepository;
    private final OrderCommandService orderCommandService;

    @Value("${toss.payment.secret.key}")
    private String SECRET_KEY;

    private final String API_URL = "https://api.tosspayments.com/v1/payments/confirm";

    public TossPayment findById(Long id) {
        return tossPaymentRepository.findById(id).orElseThrow(() -> new ExpectedException(ErrorCode.TOSS_PAYMENT_NOT_FOUND));
    }

    // 최종 결제 승인을 완료 API
    public String approvePayment(PaymentApproveRequestDto request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBasicAuth(SECRET_KEY, "");

        Map<String, Object> body = new HashMap<>();
        body.put("paymentKey", request.getPaymentKey());
        body.put("orderId", request.getOrderId());
        body.put("amount", request.getAmount());

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(API_URL, entity, String.class);
            System.out.println("결제 승인 응답: " + response.getBody());
            return response.getBody();
        } catch (HttpClientErrorException e) {
            System.err.println("HTTP 오류 발생: " + e.getStatusCode());
            System.err.println("응답 본문: " + e.getResponseBodyAsString());
            throw new ExpectedException(ErrorCode.PAYMENT_CONFIRM_FAILED);
        } catch (Exception e) {
            // 기타 예외 처리
            System.err.println("알 수 없는 오류 발생: " + e.getMessage());
            throw new ExpectedException(ErrorCode.PAYMENT_CONFIRM_ERROR);
        }
    }

    public void insertTossPayment(TossPayment tossPayment) {
        tossPaymentRepository.save(tossPayment);
    }

    /**
     * Toss Webhook 처리 - 결제 상태 변경 이벤트
     * Toss에서 결제 상태가 변경되면 자동으로 호출됩니다
     */
    @Transactional
    public void handleWebhook(TossWebhookRequestDto webhook) {
        log.info("Webhook 수신 - EventType: {}, PaymentKey: {}",
                webhook.getEventType(), webhook.getData().getPaymentKey());

        TossWebhookRequestDto.PaymentData data = webhook.getData();

        // paymentKey로 결제 정보 조회
        TossPayment payment = tossPaymentRepository.findById(Long.parseLong(data.getOrderId()))
                .orElseThrow(() -> new ExpectedException(ErrorCode.TOSS_PAYMENT_NOT_FOUND));

        String previousStatus = payment.getStatus();
        String newStatus = data.getStatus();

        // 결제 상태 업데이트
        payment.updateStatus(newStatus, data.getApprovedAt());
        tossPaymentRepository.save(payment);

        log.info("결제 상태 업데이트 완료 - OrderId: {}, {} -> {}",
                data.getOrderId(), previousStatus, newStatus);

        // 주문 상태도 함께 업데이트
        updateOrderStatus(Long.parseLong(data.getOrderId()), newStatus);
    }

    /**
     * 결제 상태에 따라 주문 상태 업데이트
     */
    private void updateOrderStatus(Long orderId, String paymentStatus) {
        try {
            switch (paymentStatus) {
                case "DONE":
                    // 결제 완료 -> 주문 상태를 PAYMENT_COMPLETED로 변경
                    orderCommandService.completePayment(orderId);
                    log.info("주문 결제 완료 처리 - OrderId: {}", orderId);
                    break;
                case "CANCELED":
                case "PARTIAL_CANCELED":
                    // 결제 취소 -> 주문 취소
                    orderCommandService.cancelOrder(orderId);
                    log.info("주문 취소 처리 - OrderId: {}", orderId);
                    break;
                case "EXPIRED":
                case "ABORTED":
                    // 결제 만료/실패 -> 주문 취소
                    orderCommandService.cancelOrder(orderId);
                    log.info("주문 실패 처리 - OrderId: {}, Status: {}", orderId, paymentStatus);
                    break;
                default:
                    log.info("주문 상태 유지 - OrderId: {}, PaymentStatus: {}", orderId, paymentStatus);
            }
        } catch (Exception e) {
            log.error("주문 상태 업데이트 실패 - OrderId: {}, Error: {}", orderId, e.getMessage());
            // Webhook 처리는 실패해도 예외를 던지지 않음 (재시도는 Toss에서 처리)
        }
    }

    // 결제 취소 API
    public String cancelPayment(PaymentCancelRequestDto request) {
        String cancelUrl = "https://api.tosspayments.com/v1/payments/" + request.getPaymentKey() + "/cancel";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBasicAuth(SECRET_KEY, "");

        Map<String, Object> body = new HashMap<>();
        body.put("cancelReason", request.getCancelReason());

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(cancelUrl, entity, String.class);
            return response.getBody();
        } catch (HttpClientErrorException e) {
            System.err.println("HTTP 오류 발생: " + e.getStatusCode());
            System.err.println("응답 본문: " + e.getResponseBodyAsString());
            throw new ExpectedException(ErrorCode.PAYMENT_CANCEL_FAILED);
        } catch (Exception e) {
            System.err.println("알 수 없는 오류 발생: " + e.getMessage());
            throw new ExpectedException(ErrorCode.PAYMENT_CANCEL_ERROR);
        }
    }

    // 부분 환불 또는 전액 환불 API
    public String refundPayment(PaymentRefundRequestDto request) {
        String cancelUrl = "https://api.tosspayments.com/v1/payments/" + request.getPaymentKey() + "/cancel";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBasicAuth(SECRET_KEY, "");

        Map<String, Object> body = new HashMap<>();
        body.put("cancelReason", request.getCancelReason());
        if (request.getRefundAmount() != null) {
            body.put("cancelAmount", request.getRefundAmount());  // 부분 환불
        }

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(cancelUrl, entity, String.class);
            return response.getBody();
        } catch (HttpClientErrorException e) {
            System.err.println("HTTP 오류 발생: " + e.getStatusCode());
            System.err.println("응답 본문: " + e.getResponseBodyAsString());
            throw new ExpectedException(ErrorCode.REFUND_FAILED);
        } catch (Exception e) {
            System.err.println("알 수 없는 오류 발생: " + e.getMessage());
            throw new ExpectedException(ErrorCode.REFUND_ERROR);
        }
    }
}
