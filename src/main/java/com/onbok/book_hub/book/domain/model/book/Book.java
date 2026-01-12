package com.onbok.book_hub.book.domain.model.book;

import com.onbok.book_hub.common.domain.BaseTime;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "books")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Book extends BaseTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String author;
    private String company;
    private int price;
    private String imageUrl;

    @Column(length = 8191)
    private String summary;     // column 길이: 255 -> 8191

    private String category;    // 카테고리 (예: "소설", "IT/컴퓨터", "자기계발" 등)

    private int stock;          // 재고 수량

    @Version
    private Long version;       // 낙관적 락을 위한 버전 필드 (재고 관리)

    @Builder
    public Book(String title, String author, String company, int price, String imageUrl, String summary, String category, int stock) {
        this.title = title;
        this.author = author;
        this.company = company;
        this.price = price;
        this.imageUrl = imageUrl;
        this.summary = summary;
        this.category = category;
        this.stock = stock;
    }

    // 재고 감소 메서드
    public void decreaseStock(int quantity) {
        if (this.stock < quantity) {
            throw new IllegalStateException("재고가 부족합니다. 현재 재고: " + this.stock);
        }
        this.stock -= quantity;
    }

    // 재고 증가 메서드
    public void increaseStock(int quantity) {
        this.stock += quantity;
    }

    // 도서 요약 정보를 업데이트
    public void updateSummary(String summary) {
        this.summary = summary;
    }
}
