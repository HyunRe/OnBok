package com.onbok.book_hub.facade;

import com.onbok.book_hub.common.exception.ErrorCode;
import com.onbok.book_hub.common.exception.ExpectedException;
import com.onbok.book_hub.order.application.OrderCommandService;
import com.onbok.book_hub.order.application.OrderQueryService;
import com.onbok.book_hub.order.application.facade.OrderCancellationFacade;
import com.onbok.book_hub.order.domain.model.Order;
import com.onbok.book_hub.order.domain.model.OrderStatus;
import com.onbok.book_hub.payment.application.TossPaymentService;
import com.onbok.book_hub.payment.domain.model.TossPayment;
import com.onbok.book_hub.payment.dto.PaymentCancelRequestDto;
import com.onbok.book_hub.payment.dto.PaymentRefundRequestDto;
import com.onbok.book_hub.user.domain.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
@DisplayName("OrderCancellationFacade 주문 취소/환불 단위 테스트")
class OrderCancellationFacadeTest {
    @Mock
    private OrderCommandService orderCommandService;
    @Mock
    private OrderQueryService orderQueryService;
    @Mock
    private TossPaymentService tossPaymentService;

    private OrderCancellationFacade orderCancellationFacade;

    private User owner;
    private User otherUser;
    private TossPayment tossPayment;
    private Order order;

    private static final Long ORDER_ID = 1L;
    private static final Long OWNER_ID = 100L;
    private static final Long OTHER_USER_ID = 200L;
    private static final Long PAYMENT_ID = 10L;
    private static final String PAYMENT_KEY = "test_payment_key_123";

    @BeforeEach
    void setUp() {
        owner = User.builder().email("owner@test.com").build();
        ReflectionTestUtils.setField(owner, "id", OWNER_ID);
        otherUser = User.builder().email("other@test.com").build();
        ReflectionTestUtils.setField(otherUser, "id", OTHER_USER_ID);

        tossPayment = TossPayment.builder().id(PAYMENT_ID).paymentKey(PAYMENT_KEY).build();
        order = Order.builder()
                .user(owner)
                .totalAmount(30000)
                .status(OrderStatus.PAYMENT_COMPLETED)
                .tossPayment(tossPayment)
                .build();

        // 실제 TransactionTemplate + mock 트랜잭션 매니저 - 콜백이 그대로 실행되도록 한다
        orderCancellationFacade = new OrderCancellationFacade(
                orderQueryService,
                orderCommandService,
                tossPaymentService,
                new TransactionTemplate(mock(PlatformTransactionManager.class))
        );

        lenient().when(orderQueryService.findById(ORDER_ID)).thenReturn(order);
    }

    @Nested
    @DisplayName("cancel")
    class Cancel {

        @Test
        @DisplayName("성공 - Toss 결제 취소 API 호출 후 주문 취소 및 결제 상태 반영")
        void cancel_success() {
            // when
            orderCancellationFacade.cancel(OWNER_ID, ORDER_ID);

            // then
            ArgumentCaptor<PaymentCancelRequestDto> captor = ArgumentCaptor.forClass(PaymentCancelRequestDto.class);
            verify(tossPaymentService).cancelPayment(captor.capture());
            assertThat(captor.getValue().getPaymentKey()).isEqualTo(PAYMENT_KEY);
            assertThat(captor.getValue().getCancelReason()).isEqualTo("고객 주문 취소");

            verify(orderCommandService).cancelOrder(ORDER_ID);
            verify(tossPaymentService).markCanceled(PAYMENT_ID);
        }

        @Test
        @DisplayName("성공 - Toss API가 orderCommandService보다 먼저 호출됨")
        void cancel_callOrder() {
            // when
            orderCancellationFacade.cancel(OWNER_ID, ORDER_ID);

            // then
            InOrder inOrder = inOrder(tossPaymentService, orderCommandService);
            inOrder.verify(tossPaymentService).cancelPayment(any(PaymentCancelRequestDto.class));
            inOrder.verify(orderCommandService).cancelOrder(ORDER_ID);
        }

        @Test
        @DisplayName("실패 - 다른 사용자의 주문 취소 시 UNAUTHORIZED_ACCESS 예외")
        void cancel_unauthorized() {
            // when & then
            assertThatThrownBy(() -> orderCancellationFacade.cancel(OTHER_USER_ID, ORDER_ID))
                    .isInstanceOf(ExpectedException.class)
                    .extracting(e -> ((ExpectedException) e).getErrorCode())
                    .isEqualTo(ErrorCode.UNAUTHORIZED_ACCESS);

            verify(tossPaymentService, never()).cancelPayment(any());
            verify(orderCommandService, never()).cancelOrder(any());
        }

        @Test
        @DisplayName("실패 - Toss API 실패 시 주문 상태를 변경하지 않음")
        void cancel_tossApiFail() {
            // given
            doThrow(new ExpectedException(ErrorCode.PAYMENT_CANCEL_FAILED))
                    .when(tossPaymentService).cancelPayment(any(PaymentCancelRequestDto.class));

            // when & then
            assertThatThrownBy(() -> orderCancellationFacade.cancel(OWNER_ID, ORDER_ID))
                    .isInstanceOf(ExpectedException.class);

            verify(orderCommandService, never()).cancelOrder(any());
            verify(tossPaymentService, never()).markCanceled(any());
        }

        @Test
        @DisplayName("실패 - 취소 불가 상태면 IllegalStateException 전파")
        void cancel_illegalState() {
            // given
            doThrow(new IllegalStateException("상태 변경 불가"))
                    .when(orderCommandService).cancelOrder(ORDER_ID);

            // when & then
            assertThatThrownBy(() -> orderCancellationFacade.cancel(OWNER_ID, ORDER_ID))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("refund")
    class Refund {

        @Test
        @DisplayName("성공 - Toss 환불 API 호출 후 주문 환불 및 결제 상태 반영")
        void refund_success() {
            // when
            orderCancellationFacade.refund(OWNER_ID, ORDER_ID);

            // then
            ArgumentCaptor<PaymentRefundRequestDto> captor = ArgumentCaptor.forClass(PaymentRefundRequestDto.class);
            verify(tossPaymentService).refundPayment(captor.capture());
            assertThat(captor.getValue().getPaymentKey()).isEqualTo(PAYMENT_KEY);
            assertThat(captor.getValue().getCancelReason()).isEqualTo("고객 환불 요청");
            assertThat(captor.getValue().getRefundAmount()).isNull();

            verify(orderCommandService).refundOrder(ORDER_ID);
            verify(tossPaymentService).markCanceled(PAYMENT_ID);
        }

        @Test
        @DisplayName("성공 - Toss API가 orderCommandService보다 먼저 호출됨")
        void refund_callOrder() {
            // when
            orderCancellationFacade.refund(OWNER_ID, ORDER_ID);

            // then
            InOrder inOrder = inOrder(tossPaymentService, orderCommandService);
            inOrder.verify(tossPaymentService).refundPayment(any(PaymentRefundRequestDto.class));
            inOrder.verify(orderCommandService).refundOrder(ORDER_ID);
        }

        @Test
        @DisplayName("실패 - 다른 사용자의 주문 환불 시 UNAUTHORIZED_ACCESS 예외")
        void refund_unauthorized() {
            // when & then
            assertThatThrownBy(() -> orderCancellationFacade.refund(OTHER_USER_ID, ORDER_ID))
                    .isInstanceOf(ExpectedException.class)
                    .extracting(e -> ((ExpectedException) e).getErrorCode())
                    .isEqualTo(ErrorCode.UNAUTHORIZED_ACCESS);

            verify(tossPaymentService, never()).refundPayment(any());
            verify(orderCommandService, never()).refundOrder(any());
        }

        @Test
        @DisplayName("실패 - Toss API 실패 시 주문 상태를 변경하지 않음")
        void refund_tossApiFail() {
            // given
            doThrow(new ExpectedException(ErrorCode.REFUND_FAILED))
                    .when(tossPaymentService).refundPayment(any(PaymentRefundRequestDto.class));

            // when & then
            assertThatThrownBy(() -> orderCancellationFacade.refund(OWNER_ID, ORDER_ID))
                    .isInstanceOf(ExpectedException.class);

            verify(orderCommandService, never()).refundOrder(any());
            verify(tossPaymentService, never()).markCanceled(any());
        }
    }
}
