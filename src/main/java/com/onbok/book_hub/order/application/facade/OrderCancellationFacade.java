package com.onbok.book_hub.order.application.facade;

import com.onbok.book_hub.common.exception.ErrorCode;
import com.onbok.book_hub.common.exception.ExpectedException;
import com.onbok.book_hub.order.application.OrderCommandService;
import com.onbok.book_hub.order.application.OrderQueryService;
import com.onbok.book_hub.order.domain.model.Order;
import com.onbok.book_hub.payment.application.TossPaymentService;
import com.onbok.book_hub.payment.domain.model.TossPayment;
import com.onbok.book_hub.payment.dto.PaymentCancelRequestDto;
import com.onbok.book_hub.payment.dto.PaymentRefundRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * 주문 취소 / 환불 흐름을 담당하는 Facade.
 * "소유권 검증 -> Toss 결제 취소 -> 주문·결제 상태 반영" 순서를 한 곳에서 보장한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderCancellationFacade {
    private static final String CANCEL_REASON = "고객 주문 취소";
    private static final String REFUND_REASON = "고객 환불 요청";

    private final OrderQueryService orderQueryService;
    private final OrderCommandService orderCommandService;
    private final TossPaymentService tossPaymentService;
    private final TransactionTemplate transactionTemplate;

    public void cancel(Long userId, Long orderId) {
        TossPayment payment = loadOwnedOrder(userId, orderId).getTossPayment();

        tossPaymentService.cancelPayment(PaymentCancelRequestDto.builder()
                .paymentKey(payment.getPaymentKey())
                .cancelReason(CANCEL_REASON)
                .build());

        applyCancellation(orderId, payment, () -> orderCommandService.cancelOrder(orderId));
    }

    public void refund(Long userId, Long orderId) {
        TossPayment payment = loadOwnedOrder(userId, orderId).getTossPayment();

        // refundAmount를 비워 두면 전액 환불
        tossPaymentService.refundPayment(PaymentRefundRequestDto.builder()
                .paymentKey(payment.getPaymentKey())
                .cancelReason(REFUND_REASON)
                .build());

        applyCancellation(orderId, payment, () -> orderCommandService.refundOrder(orderId));
    }

    private Order loadOwnedOrder(Long userId, Long orderId) {
        Order order = orderQueryService.findById(orderId);
        if (!order.getUser().getId().equals(userId)) {
            throw new ExpectedException(ErrorCode.UNAUTHORIZED_ACCESS);
        }
        return order;
    }

    /**
     * Toss 취소가 끝난 뒤의 DB 반영. 주문 상태와 결제 상태를 한 트랜잭션으로 묶는다.
     * 이 단계가 실패하면 결제만 취소되고 주문은 살아있는 불일치가 남으므로 별도로 로그를 남긴다.
     * (이미 취소된 결제는 되돌릴 수 없어 자동 보상이 불가능하다)
     */
    private void applyCancellation(Long orderId, TossPayment payment, Runnable orderStateChange) {
        try {
            transactionTemplate.executeWithoutResult(status -> {
                orderStateChange.run();
                tossPaymentService.markCanceled(payment.getId());
            });
        } catch (RuntimeException e) {
            log.error("결제는 취소되었으나 주문 상태 반영에 실패했습니다 - 수동 정산이 필요합니다. orderId: {}, paymentKey: {}",
                    orderId, payment.getPaymentKey(), e);
            throw e;
        }
    }
}
