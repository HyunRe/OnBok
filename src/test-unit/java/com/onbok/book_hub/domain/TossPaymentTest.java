package com.onbok.book_hub.domain;

import com.onbok.book_hub.payment.domain.model.TossPayment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@DisplayName("TossPayment 도메인 단위 테스트")
class TossPaymentTest {

    @Test
    @DisplayName("결제 생성 - 정상 케이스")
    void createPayment_success() {
        // given & when
        TossPayment payment = TossPayment.builder()
                .paymentKey("test_payment_key_123")
                .name("자바의 정석 외 2건")
                .status("READY")
                .paymentType("CARD")
                .totalPayment(50000)
                .version("2022-11-16")
                .build();

        // then
        assertThat(payment.getPaymentKey()).isEqualTo("test_payment_key_123");
        assertThat(payment.getName()).isEqualTo("자바의 정석 외 2건");
        assertThat(payment.getStatus()).isEqualTo("READY");
        assertThat(payment.getTotalPayment()).isEqualTo(50000);
    }

    @Test
    @DisplayName("결제 상태 업데이트 - READY에서 DONE으로 변경")
    void updateStatus_fromReadyToDone() {
        // given
        TossPayment payment = TossPayment.builder()
                .paymentKey("test_payment_key")
                .status("READY")
                .totalPayment(30000)
                .build();

        // when
        LocalDateTime approvalTime = LocalDateTime.now();
        payment.updateStatus("DONE", approvalTime);

        // then
        assertThat(payment.getStatus()).isEqualTo("DONE");
        assertThat(payment.getApprovalTime()).isEqualTo(approvalTime);
    }

    @Test
    @DisplayName("결제 취소 - cancel() 메서드 호출")
    void cancelPayment() {
        // given
        TossPayment payment = TossPayment.builder()
                .paymentKey("test_payment_key")
                .status("DONE")
                .totalPayment(30000)
                .approvalTime(LocalDateTime.now())
                .build();

        // when
        payment.cancel();

        // then
        assertThat(payment.getStatus()).isEqualTo("CANCELED");
    }

    @Test
    @DisplayName("결제 상태 업데이트 - 승인 시간 null인 경우")
    void updateStatus_withNullApprovalTime() {
        // given
        LocalDateTime originalApprovalTime = LocalDateTime.of(2026, 1, 20, 10, 0);
        TossPayment payment = TossPayment.builder()
                .paymentKey("test_payment_key")
                .status("READY")
                .approvalTime(originalApprovalTime)
                .totalPayment(30000)
                .build();

        // when
        payment.updateStatus("IN_PROGRESS", null);

        // then
        assertThat(payment.getStatus()).isEqualTo("IN_PROGRESS");
        assertThat(payment.getApprovalTime()).isEqualTo(originalApprovalTime); // 기존 시간 유지
    }

    @Test
    @DisplayName("다양한 결제 수단 테스트 - CARD, VIRTUAL_ACCOUNT, etc")
    void paymentTypes() {
        // given & when
        TossPayment cardPayment = TossPayment.builder()
                .paymentKey("test_key_1")
                .paymentType("CARD")
                .totalPayment(50000)
                .build();

        TossPayment virtualAccountPayment = TossPayment.builder()
                .paymentKey("test_key_2")
                .paymentType("VIRTUAL_ACCOUNT")
                .totalPayment(60000)
                .build();

        TossPayment transferPayment = TossPayment.builder()
                .paymentKey("test_key_3")
                .paymentType("TRANSFER")
                .totalPayment(70000)
                .build();

        // then
        assertThat(cardPayment.getPaymentType()).isEqualTo("CARD");
        assertThat(virtualAccountPayment.getPaymentType()).isEqualTo("VIRTUAL_ACCOUNT");
        assertThat(transferPayment.getPaymentType()).isEqualTo("TRANSFER");
    }
}
