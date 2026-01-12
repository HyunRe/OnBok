package com.onbok.book_hub.book.dto;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BookStateDto {
    private Long id;
    private String title;
    private String company;
    private int unitPrice;
    private int quantity;
    private int totalPrice;

    @Builder
    public BookStateDto(Long id, String title, String company, int unitPrice, int quantity, int totalPrice) {
        this.id = id;
        this.title = title;
        this.company = company;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
        this.totalPrice = totalPrice;
    }

    // 수량을 추가하고 합계를 갱신
    public void addQuantity(int extraQuantity) {
        this.quantity += extraQuantity;
        this.calculateTotalPrice(); // 수량이 변하면 합계도 반드시 재계산
    }

    // 내부 계산용 메서드 (총합 갱신)
    private void calculateTotalPrice() {
        this.totalPrice = this.unitPrice * this.quantity;
    }
}
