package com.onbok.book_hub.controller;

import com.onbok.book_hub.common.exception.ErrorCode;
import com.onbok.book_hub.common.exception.ExpectedException;
import com.onbok.book_hub.order.application.facade.OrderCancellationFacade;
import com.onbok.book_hub.order.presentation.view.OrderViewController;
import com.onbok.book_hub.user.domain.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * 취소/환불의 비즈니스 흐름은 OrderCancellationFacadeTest 에서 검증한다.
 * 여기서는 Controller의 책임인 사용자 메시지와 리다이렉트만 검증한다.
 */
@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
@DisplayName("OrderViewController 주문 취소/환불 단위 테스트")
class OrderViewControllerCancelRefundTest {
    @Mock
    private OrderCancellationFacade orderCancellationFacade;

    @InjectMocks
    private OrderViewController orderViewController;

    private User owner;
    private RedirectAttributesModelMap redirectAttributes;

    private static final Long ORDER_ID = 1L;
    private static final Long OWNER_ID = 100L;
    private static final String REDIRECT_DETAIL = "redirect:/view/orders/detail/" + ORDER_ID;

    @BeforeEach
    void setUp() {
        owner = User.builder().email("owner@test.com").build();
        ReflectionTestUtils.setField(owner, "id", OWNER_ID);
        redirectAttributes = new RedirectAttributesModelMap();
    }

    @Nested
    @DisplayName("cancelOrder")
    class CancelOrder {

        @Test
        @DisplayName("성공 - Facade에 위임하고 완료 메시지를 남김")
        void cancelOrder_success() {
            // when
            String result = orderViewController.cancelOrder(owner, ORDER_ID, redirectAttributes);

            // then
            verify(orderCancellationFacade).cancel(OWNER_ID, ORDER_ID);
            assertThat(redirectAttributes.getFlashAttributes().get("msg")).isEqualTo("주문이 취소되었습니다.");
            assertThat(result).isEqualTo(REDIRECT_DETAIL);
        }

        @Test
        @DisplayName("실패 - 권한 없음(ExpectedException) 시 실패 메시지 반환")
        void cancelOrder_unauthorized() {
            // given
            doThrow(new ExpectedException(ErrorCode.UNAUTHORIZED_ACCESS))
                    .when(orderCancellationFacade).cancel(OWNER_ID, ORDER_ID);

            // when
            String result = orderViewController.cancelOrder(owner, ORDER_ID, redirectAttributes);

            // then
            assertThat(redirectAttributes.getFlashAttributes().get("msg").toString())
                    .contains("주문 취소 실패")
                    .contains(ErrorCode.UNAUTHORIZED_ACCESS.getMessage());
            assertThat(result).isEqualTo(REDIRECT_DETAIL);
        }

        @Test
        @DisplayName("실패 - IllegalStateException 발생 시 실패 메시지 반환")
        void cancelOrder_illegalState() {
            // given
            doThrow(new IllegalStateException("상태 변경 불가"))
                    .when(orderCancellationFacade).cancel(OWNER_ID, ORDER_ID);

            // when
            String result = orderViewController.cancelOrder(owner, ORDER_ID, redirectAttributes);

            // then
            assertThat(redirectAttributes.getFlashAttributes().get("msg").toString())
                    .contains("주문 취소 실패")
                    .contains("상태 변경 불가");
            assertThat(result).isEqualTo(REDIRECT_DETAIL);
        }
    }

    @Nested
    @DisplayName("refundOrder")
    class RefundOrder {

        @Test
        @DisplayName("성공 - Facade에 위임하고 완료 메시지를 남김")
        void refundOrder_success() {
            // when
            String result = orderViewController.refundOrder(owner, ORDER_ID, redirectAttributes);

            // then
            verify(orderCancellationFacade).refund(OWNER_ID, ORDER_ID);
            assertThat(redirectAttributes.getFlashAttributes().get("msg")).isEqualTo("환불이 완료되었습니다.");
            assertThat(result).isEqualTo(REDIRECT_DETAIL);
        }

        @Test
        @DisplayName("실패 - 권한 없음(ExpectedException) 시 실패 메시지 반환")
        void refundOrder_unauthorized() {
            // given
            doThrow(new ExpectedException(ErrorCode.UNAUTHORIZED_ACCESS))
                    .when(orderCancellationFacade).refund(OWNER_ID, ORDER_ID);

            // when
            String result = orderViewController.refundOrder(owner, ORDER_ID, redirectAttributes);

            // then
            assertThat(redirectAttributes.getFlashAttributes().get("msg").toString())
                    .contains("환불 실패")
                    .contains(ErrorCode.UNAUTHORIZED_ACCESS.getMessage());
            assertThat(result).isEqualTo(REDIRECT_DETAIL);
        }

        @Test
        @DisplayName("실패 - IllegalStateException 발생 시 실패 메시지 반환")
        void refundOrder_illegalState() {
            // given
            doThrow(new IllegalStateException("상태 변경 불가"))
                    .when(orderCancellationFacade).refund(OWNER_ID, ORDER_ID);

            // when
            String result = orderViewController.refundOrder(owner, ORDER_ID, redirectAttributes);

            // then
            assertThat(redirectAttributes.getFlashAttributes().get("msg").toString())
                    .contains("환불 실패")
                    .contains("상태 변경 불가");
            assertThat(result).isEqualTo(REDIRECT_DETAIL);
        }
    }
}
