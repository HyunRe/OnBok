package com.onbok.book_hub.cart.application;

import com.onbok.book_hub.book.domain.model.book.Book;
import com.onbok.book_hub.book.domain.repository.book.BookRepository;
import com.onbok.book_hub.cart.domain.model.Cart;
import com.onbok.book_hub.cart.domain.repository.CartRepository;
import com.onbok.book_hub.common.exception.ErrorCode;
import com.onbok.book_hub.common.exception.ExpectedException;
import com.onbok.book_hub.user.domain.model.User;
import com.onbok.book_hub.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CartService {
    private final CartRepository cartRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;

    public Cart findById(Long id) {
        return cartRepository.findById(id).orElseThrow(() -> new ExpectedException(ErrorCode.CART_NOT_FOUND));
    }

    public List<Cart> getCartItemsByUser(Long userId) {
        return cartRepository.findByUserId(userId);
    }

    public void addToCart(Long userId, Long bookId, int quantity) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ExpectedException(ErrorCode.USER_NOT_FOUND));
        Book book = bookRepository.findById(bookId).orElseThrow(() -> new ExpectedException(ErrorCode.BOOK_NOT_FOUND));

        Cart cart = Cart.builder()
                .user(user).book(book).quantity(quantity)
                .build();
        cartRepository.save(cart);
    }

    public void updateCart(Cart cart) {
        cartRepository.save(cart);
    }

    public void removeFromCart(long cid) {
        cartRepository.deleteById(cid);
    }

    public void clearCart(Long userId) {
        List<Cart> cartList = cartRepository.findByUserId(userId);
        cartRepository.deleteAll(cartList);
    }
}
