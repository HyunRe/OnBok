package com.onbok.book_hub.payment.domain.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "toss_payments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TossPayment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;                        // orderId

    private String paymentKey;              // 결제 고유 키 (토스 발급)
    private String name;                    // 주문 명칭 (orderName)
    private String status;                  // 결제 처리 상태 (READY, DONE, CANCELED 등)
    private LocalDateTime approvalTime;     // 결제 승인 시각 (approvedAt)
    private String paymentType;             // 결제 수단 (카드, 가상계좌, 계좌이체, 휴대폰, 상품권 등)
    private int totalPayment;               // 총 결제 금액 (상품 가격 + 배송비 등 포함)
    private String version;                 // 토스페이먼츠 API 버전 규격

    @Builder
    public TossPayment(Long id, String paymentKey, String name, String status, LocalDateTime approvalTime, String paymentType, int totalPayment, String version) {
        this.id = id;
        this.paymentKey = paymentKey;
        this.name = name;
        this.status = status;
        this.approvalTime = approvalTime;
        this.paymentType = paymentType;
        this.totalPayment = totalPayment;
        this.version = version;
    }

    /**
     * 결제 상태 업데이트 (Webhook 처리용)
     */
    public void updateStatus(String status, LocalDateTime approvalTime) {
        this.status = status;
        if (approvalTime != null) {
            this.approvalTime = approvalTime;
        }
    }

    /**
     * 결제 취소/환불 시 상태 업데이트
     */
    public void cancel() {
        this.status = "CANCELED";
    }
}
