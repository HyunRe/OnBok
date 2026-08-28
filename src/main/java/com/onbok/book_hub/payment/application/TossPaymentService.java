package com.onbok.book_hub.payment.application;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class TossPaymentService {
    private final RestTemplate restTemplate;
    private final TossPaymentRepository tossPaymentRepository;
    private final OrderCommandService orderCommandService;
    private final ObjectMapper objectMapper;

    @Value("${toss.payment.secret.key}")
    private String SECRET_KEY;

    @Value("${toss.payment.api.url}")
    private String API_BASE_URL;

    public TossPayment findById(Long id) {
        return tossPaymentRepository.findById(id).orElseThrow(() -> new ExpectedException(ErrorCode.TOSS_PAYMENT_NOT_FOUND));
    }

    // 최종 결제 승인을 완료 API
    public String approvePayment(PaymentApproveRequestDto request) {
        String confirmUrl = API_BASE_URL + "/v1/payments/confirm";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBasicAuth(SECRET_KEY, "");

        Map<String, Object> body = new HashMap<>();
        body.put("paymentKey", request.getPaymentKey());
        body.put("orderId", request.getOrderId());
        body.put("amount", request.getAmount());

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(confirmUrl, entity, String.class);
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

    /**
     * 결제를 승인하고 Toss 응답을 TossPayment 엔티티로 변환한다. (저장하지는 않는다)
     * Toss 응답 규격을 아는 책임은 payment 계층에 둔다.
     */
    public TossPayment approveAndBuildPayment(PaymentApproveRequestDto request) {
        return toTossPayment(approvePayment(request));
    }

    private TossPayment toTossPayment(String jsonResult) {
        try {
            Map<String, Object> result = objectMapper.readValue(jsonResult, new TypeReference<Map<String, Object>>() {});
            String approvedAt = (String) result.get("approvedAt");

            return TossPayment.builder()
                    .paymentKey((String) result.get("paymentKey"))
                    .name((String) result.get("orderName"))
                    .status((String) result.get("status"))
                    // 가상계좌 등 승인 시각이 없는 결제 수단을 고려해 null을 허용한다
                    .approvalTime(approvedAt != null ? OffsetDateTime.parse(approvedAt).toLocalDateTime() : null)
                    .paymentType(result.get("card") != null ? "card" : "other")
                    .totalPayment((Integer) result.get("totalAmount"))
                    .version((String) result.get("version"))
                    .build();
        } catch (Exception e) {
            log.error("결제 승인 응답 파싱 실패: {}", jsonResult, e);
            throw new ExpectedException(ErrorCode.PAYMENT_CONFIRM_ERROR);
        }
    }

    public TossPayment insertTossPayment(TossPayment tossPayment) {
        return tossPaymentRepository.save(tossPayment);
    }

    /**
     * Toss 취소/환불 API 성공 후 결제 엔티티의 상태를 CANCELED로 반영한다.
     */
    @Transactional
    public void markCanceled(Long paymentId) {
        TossPayment payment = findById(paymentId);
        payment.cancel();
        tossPaymentRepository.save(payment);
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
    public void cancelPayment(PaymentCancelRequestDto request) {
        String cancelUrl = API_BASE_URL + "/v1/payments/" + request.getPaymentKey() + "/cancel";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBasicAuth(SECRET_KEY, "");

        Map<String, Object> body = new HashMap<>();
        body.put("cancelReason", request.getCancelReason());

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(cancelUrl, entity, String.class);
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
    public void refundPayment(PaymentRefundRequestDto request) {
        String cancelUrl = API_BASE_URL + "/v1/payments/" + request.getPaymentKey() + "/cancel";

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
