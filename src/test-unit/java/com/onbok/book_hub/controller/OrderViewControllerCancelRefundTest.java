package com.onbok.book_hub.controller;

import com.onbok.book_hub.cart.application.CartService;
import com.onbok.book_hub.common.exception.ErrorCode;
import com.onbok.book_hub.common.exception.ExpectedException;
import com.onbok.book_hub.delivery.application.DeliveryAddressService;
import com.onbok.book_hub.order.application.OrderCommandService;
import com.onbok.book_hub.order.application.OrderQueryService;
import com.onbok.book_hub.order.application.OrderStatisticsService;
import com.onbok.book_hub.order.domain.model.Order;
import com.onbok.book_hub.order.domain.model.OrderStatus;
import com.onbok.book_hub.order.presentation.view.OrderViewController;
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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
@DisplayName("OrderViewController 주문 취소/환불 단위 테스트")
class OrderViewControllerCancelRefundTest {

    @Mock
    private CartService cartService;
    @Mock
    private DeliveryAddressService deliveryAddressService;
    @Mock
    private OrderCommandService orderCommandService;
    @Mock
    private OrderQueryService orderQueryService;
    @Mock
    private OrderStatisticsService orderStatisticsService;
    @Mock
    private TossPaymentService tossPaymentService;

    @InjectMocks
    private OrderViewController orderViewController;

    private User owner;
    private User otherUser;
    private TossPayment tossPayment;
    private Order order;
    private RedirectAttributesModelMap redirectAttributes;

    private static final Long ORDER_ID = 1L;
    private static final String PAYMENT_KEY = "test_payment_key_123";

    @BeforeEach
    void setUp() {
        owner = User.builder().email("owner@test.com").build();
        ReflectionTestUtils.setField(owner, "id", 100L);
        otherUser = User.builder().email("other@test.com").build();
        ReflectionTestUtils.setField(otherUser, "id", 200L);
        tossPayment = TossPayment.builder().paymentKey(PAYMENT_KEY).build();
        order = Order.builder()
                .user(owner)
                .totalAmount(30000)
                .status(OrderStatus.PAYMENT_COMPLETED)
                .tossPayment(tossPayment)
                .build();
        redirectAttributes = new RedirectAttributesModelMap();

        lenient().when(orderQueryService.findById(ORDER_ID)).thenReturn(order);
    }

    @Nested
    @DisplayName("cancelOrder")
    class CancelOrder {

        @Test
        @DisplayName("성공 - Toss 결제 취소 API 호출 후 주문 취소")
        void cancelOrder_success() {
            // when
            String result = orderViewController.cancelOrder(owner, ORDER_ID, redirectAttributes);

            // then
            ArgumentCaptor<PaymentCancelRequestDto> captor = ArgumentCaptor.forClass(PaymentCancelRequestDto.class);
            verify(tossPaymentService).cancelPayment(captor.capture());
            assertThat(captor.getValue().getPaymentKey()).isEqualTo(PAYMENT_KEY);
            assertThat(captor.getValue().getCancelReason()).isEqualTo("고객 주문 취소");

            verify(orderCommandService).cancelOrder(ORDER_ID);
            assertThat(redirectAttributes.getFlashAttributes().get("msg")).isEqualTo("주문이 취소되었습니다.");
            assertThat(result).isEqualTo("redirect:/view/orders/detail/" + ORDER_ID);
        }

        @Test
        @DisplayName("성공 - Toss API가 orderCommandService보다 먼저 호출됨")
        void cancelOrder_callOrder() {
            // when
            orderViewController.cancelOrder(owner, ORDER_ID, redirectAttributes);

            // then
            InOrder inOrder = inOrder(tossPaymentService, orderCommandService);
            inOrder.verify(tossPaymentService).cancelPayment(any(PaymentCancelRequestDto.class));
            inOrder.verify(orderCommandService).cancelOrder(ORDER_ID);
        }

        @Test
        @DisplayName("실패 - 다른 사용자의 주문 취소 시 실패 메시지 반환")
        void cancelOrder_unauthorized() {
            // when
            String result = orderViewController.cancelOrder(otherUser, ORDER_ID, redirectAttributes);

            // then
            verify(tossPaymentService, never()).cancelPayment(any());
            verify(orderCommandService, never()).cancelOrder(any());
            assertThat(redirectAttributes.getFlashAttributes().get("msg").toString())
                    .contains("주문 취소 실패");
            assertThat(result).isEqualTo("redirect:/view/orders/detail/" + ORDER_ID);
        }

        @Test
        @DisplayName("실패 - Toss API 실패 시 orderCommandService 호출되지 않음")
        void cancelOrder_tossApiFail() {
            // given
            doThrow(new ExpectedException(ErrorCode.PAYMENT_CANCEL_FAILED))
                    .when(tossPaymentService).cancelPayment(any(PaymentCancelRequestDto.class));

            // when
            String result = orderViewController.cancelOrder(owner, ORDER_ID, redirectAttributes);

            // then
            verify(orderCommandService, never()).cancelOrder(any());
            assertThat(redirectAttributes.getFlashAttributes().get("msg").toString())
                    .contains("주문 취소 실패");
            assertThat(result).isEqualTo("redirect:/view/orders/detail/" + ORDER_ID);
        }

        @Test
        @DisplayName("실패 - IllegalStateException 발생 시 실패 메시지 반환")
        void cancelOrder_illegalState() {
            // given
            doThrow(new IllegalStateException("상태 변경 불가"))
                    .when(tossPaymentService).cancelPayment(any(PaymentCancelRequestDto.class));

            // when
            String result = orderViewController.cancelOrder(owner, ORDER_ID, redirectAttributes);

            // then
            assertThat(redirectAttributes.getFlashAttributes().get("msg").toString())
                    .contains("주문 취소 실패");
            assertThat(result).isEqualTo("redirect:/view/orders/detail/" + ORDER_ID);
        }
    }

    @Nested
    @DisplayName("refundOrder")
    class RefundOrder {

        @Test
        @DisplayName("성공 - Toss 환불 API 호출 후 주문 환불")
        void refundOrder_success() {
            // when
            String result = orderViewController.refundOrder(owner, ORDER_ID, redirectAttributes);

            // then
            ArgumentCaptor<PaymentRefundRequestDto> captor = ArgumentCaptor.forClass(PaymentRefundRequestDto.class);
            verify(tossPaymentService).refundPayment(captor.capture());
            assertThat(captor.getValue().getPaymentKey()).isEqualTo(PAYMENT_KEY);
            assertThat(captor.getValue().getCancelReason()).isEqualTo("고객 환불 요청");
            assertThat(captor.getValue().getRefundAmount()).isNull();

            verify(orderCommandService).refundOrder(ORDER_ID);
            assertThat(redirectAttributes.getFlashAttributes().get("msg")).isEqualTo("환불이 완료되었습니다.");
            assertThat(result).isEqualTo("redirect:/view/orders/detail/" + ORDER_ID);
        }

        @Test
        @DisplayName("성공 - Toss API가 orderCommandService보다 먼저 호출됨")
        void refundOrder_callOrder() {
            // when
            orderViewController.refundOrder(owner, ORDER_ID, redirectAttributes);

            // then
            InOrder inOrder = inOrder(tossPaymentService, orderCommandService);
            inOrder.verify(tossPaymentService).refundPayment(any(PaymentRefundRequestDto.class));
            inOrder.verify(orderCommandService).refundOrder(ORDER_ID);
        }

        @Test
        @DisplayName("실패 - 다른 사용자의 주문 환불 시 실패 메시지 반환")
        void refundOrder_unauthorized() {
            // when
            String result = orderViewController.refundOrder(otherUser, ORDER_ID, redirectAttributes);

            // then
            verify(tossPaymentService, never()).refundPayment(any());
            verify(orderCommandService, never()).refundOrder(any());
            assertThat(redirectAttributes.getFlashAttributes().get("msg").toString())
                    .contains("환불 실패");
            assertThat(result).isEqualTo("redirect:/view/orders/detail/" + ORDER_ID);
        }

        @Test
        @DisplayName("실패 - Toss API 실패 시 orderCommandService 호출되지 않음")
        void refundOrder_tossApiFail() {
            // given
            doThrow(new ExpectedException(ErrorCode.REFUND_FAILED))
                    .when(tossPaymentService).refundPayment(any(PaymentRefundRequestDto.class));

            // when
            String result = orderViewController.refundOrder(owner, ORDER_ID, redirectAttributes);

            // then
            verify(orderCommandService, never()).refundOrder(any());
            assertThat(redirectAttributes.getFlashAttributes().get("msg").toString())
                    .contains("환불 실패");
            assertThat(result).isEqualTo("redirect:/view/orders/detail/" + ORDER_ID);
        }

        @Test
        @DisplayName("실패 - IllegalStateException 발생 시 실패 메시지 반환")
        void refundOrder_illegalState() {
            // given
            doThrow(new IllegalStateException("상태 변경 불가"))
                    .when(tossPaymentService).refundPayment(any(PaymentRefundRequestDto.class));

            // when
            String result = orderViewController.refundOrder(owner, ORDER_ID, redirectAttributes);

            // then
            assertThat(redirectAttributes.getFlashAttributes().get("msg").toString())
                    .contains("환불 실패");
            assertThat(result).isEqualTo("redirect:/view/orders/detail/" + ORDER_ID);
        }
    }
}
