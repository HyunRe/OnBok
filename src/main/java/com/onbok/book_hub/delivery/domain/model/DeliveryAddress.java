package com.onbok.book_hub.delivery.domain.model;

import com.onbok.book_hub.user.domain.model.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "deliveries")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DeliveryAddress {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String alias;           // 배송지 별칭 (예: 집, 회사, 우리집 등)

    private String recipientName;   // 수령인 이름
    private String zipCode;         // 우편번호
    private String basicAddress;    // 기본 주소 (도로명/지번)
    private String detailAddress;   // 상세 주소 (동, 호수, 건물명 등)
    private String tel;             // 수령인 연락처
    private String memo;            // 배송 요청사항 (예: 문 앞에 놓아주세요)

    @Builder
    public DeliveryAddress(User user, String alias, String recipientName, String zipCode, String basicAddress, String detailAddress, String tel, String memo) {
        this.user = user;
        this.alias = alias;
        this.recipientName = recipientName;
        this.zipCode = zipCode;
        this.basicAddress = basicAddress;
        this.detailAddress = detailAddress;
        this.tel = tel;
        this.memo = memo;
    }

    // 배송지 정보 업데이트
    public void update(String alias, String recipientName, String zipCode, String basicAddress, String detailAddress, String tel, String memo) {
        this.alias = alias;
        this.recipientName = recipientName;
        this.zipCode = zipCode;
        this.basicAddress = basicAddress;
        this.detailAddress = detailAddress;
        this.tel = tel;
        this.memo = memo;
    }
}
