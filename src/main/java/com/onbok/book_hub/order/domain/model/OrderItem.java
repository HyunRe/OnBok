package com.onbok.book_hub.order.domain.model;

import com.onbok.book_hub.book.domain.model.book.Book;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "order_items")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "order_id", referencedColumnName = "id")
    private Order order;

    @ManyToOne
    @JoinColumn(name = "book_id", referencedColumnName = "id")
    private Book book;

    private int quantity;
    private int subPrice;

    @Builder
    public  OrderItem(Order order, Book book, int quantity, int subPrice) {
        this.order = order;
        this.book = book;
        this.quantity = quantity;
        this.subPrice = subPrice;
    }
}
