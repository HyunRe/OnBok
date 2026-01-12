package com.onbok.book_hub.cart.domain.model;

import com.onbok.book_hub.book.domain.model.book.Book;
import com.onbok.book_hub.common.domain.BaseTime;
import com.onbok.book_hub.user.domain.model.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "carts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Cart extends BaseTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "book_id", referencedColumnName = "id")
    private Book book;

    private int quantity;

    @Builder
    public Cart(User user, Book book, int quantity) {
        this.user = user;
        this.book = book;
        this.quantity = quantity;
    }

    public void updateQuantity(int quantity) {
        this.quantity = quantity;
    }
}
