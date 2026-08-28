package com.onbok.book_hub.payment.application.facade;

import com.onbok.book_hub.cart.application.CartService;
import com.onbok.book_hub.cart.domain.model.Cart;
import com.onbok.book_hub.common.exception.ErrorCode;
import com.onbok.book_hub.common.exception.ExpectedException;
import com.onbok.book_hub.order.application.OrderCommandService;
import com.onbok.book_hub.order.domain.model.Order;
import com.onbok.book_hub.payment.application.TossPaymentService;
import com.onbok.book_hub.payment.domain.model.TossPayment;
import com.onbok.book_hub.payment.dto.PaymentApproveRequestDto;
import com.onbok.book_hub.payment.dto.PaymentCancelRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;

/**
 * 결제 승인 ~ 주문 생성 흐름을 담당하는 Facade.
 * payment / order / cart 세 도메인을 조합하는 책임을 Controller에서 걷어내고,
 * 외부 API 호출과 DB 트랜잭션의 경계를 명시적으로 분리한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentOrderFacade {
    private static final String COMPENSATION_REASON = "주문 생성 실패로 인한 자동 취소";

    private final TossPaymentService tossPaymentService;
    private final OrderCommandService orderCommandService;
    private final CartService cartService;
    private final TransactionTemplate transactionTemplate;

    /**
     * 결제를 승인하고 주문을 생성한다.
     * 클래스 레벨에 @Transactional을 두지 않는 이유는, Toss API 호출(네트워크 I/O)이
     * 트랜잭션 안에 들어가면 그 시간 동안 DB 커넥션을 점유하기 때문이다.
     * 또한 DB 롤백은 이미 승인된 결제를 되돌리지 못하므로 보상 취소를 직접 수행한다.
     */
    public Order completePaymentAndCreateOrder(Long userId, PaymentApproveRequestDto approveRequest, Long deliveryAddressId) {
        // 1. 사전 검증 - 결제를 승인하기 전에 확인해야 보상 취소가 필요 없다
        List<Cart> cartList = cartService.getCartItemsByUser(userId);
        if (cartList.isEmpty()) {
            throw new ExpectedException(ErrorCode.CART_EMPTY);
        }

        // 2. 외부 결제 승인 - 트랜잭션 밖
        TossPayment approvedPayment = tossPaymentService.approveAndBuildPayment(approveRequest);

        // 3. DB 반영 - 결제 저장 / 주문 생성 / 장바구니 비우기를 하나의 트랜잭션으로 묶는다
        try {
            return transactionTemplate.execute(status -> {
                TossPayment savedPayment = tossPaymentService.insertTossPayment(approvedPayment);
                Order order = orderCommandService.createOrder(userId, cartList, savedPayment, deliveryAddressId);
                cartService.clearCart(userId);
                return order;
            });
        } catch (RuntimeException e) {
            compensate(approveRequest.getPaymentKey(), e);
            throw e;
        }
    }

    /**
     * 보상 트랜잭션 - 결제는 승인됐는데 주문 생성이 실패한 경우 결제를 되돌린다.
     */
    private void compensate(String paymentKey, RuntimeException cause) {
        log.error("주문 생성 실패 - 결제 보상 취소를 시도합니다. paymentKey: {}", paymentKey, cause);
        try {
            tossPaymentService.cancelPayment(PaymentCancelRequestDto.builder()
                    .paymentKey(paymentKey)
                    .cancelReason(COMPENSATION_REASON)
                    .build());
            log.info("결제 보상 취소 완료 - paymentKey: {}", paymentKey);
        } catch (Exception e) {
            // 보상까지 실패하면 결제만 승인된 상태로 남으므로 수동 정산이 필요하다
            log.error("결제 보상 취소 실패 - 수동 확인이 필요합니다. paymentKey: {}", paymentKey, e);
        }
    }
}
