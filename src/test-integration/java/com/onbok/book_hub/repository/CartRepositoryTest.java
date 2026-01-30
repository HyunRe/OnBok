package com.onbok.book_hub.cart;

import com.onbok.book_hub.book.domain.model.book.Book;
import com.onbok.book_hub.book.domain.repository.book.BookRepository;
import com.onbok.book_hub.cart.domain.model.Cart;
import com.onbok.book_hub.cart.domain.repository.CartRepository;
import com.onbok.book_hub.user.domain.model.User;
import com.onbok.book_hub.user.domain.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("Cart Repository 계층 테스트 - @DataJpaTest")
class CartRepositoryTest {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("장바구니 저장 - 정상 케이스")
    void saveCart_success() {
        // given
        User user = createUser("test@test.com");
        Book book = createBook("자바의 정석", 30000, 100);

        Cart cart = Cart.builder()
                .user(user)
                .book(book)
                .quantity(2)
                .build();

        // when
        Cart savedCart = cartRepository.save(cart);
        entityManager.flush();
        entityManager.clear();

        // then
        assertThat(savedCart.getId()).isNotNull();
        assertThat(savedCart.getQuantity()).isEqualTo(2);
        assertThat(savedCart.getUser().getEmail()).isEqualTo("test@test.com");
        assertThat(savedCart.getBook().getTitle()).isEqualTo("자바의 정석");
    }

    @Test
    @DisplayName("사용자별 장바구니 조회 - findByUserId")
    void findByUserId_success() {
        // given
        User user = createUser("user@test.com");
        Book book1 = createBook("책1", 10000, 50);
        Book book2 = createBook("책2", 20000, 30);
        Book book3 = createBook("책3", 30000, 80);

        Cart cart1 = createCart(user, book1, 2);
        Cart cart2 = createCart(user, book2, 1);
        Cart cart3 = createCart(user, book3, 3);

        entityManager.flush();
        entityManager.clear();

        // when
        List<Cart> carts = cartRepository.findByUserId(user.getId());

        // then
        assertThat(carts).hasSize(3);
        assertThat(carts).extracting("quantity").containsExactlyInAnyOrder(2, 1, 3);
    }

    @Test
    @DisplayName("사용자와 도서로 장바구니 조회 - findByUserIdAndBookId")
    void findByUserIdAndBookId_success() {
        // given
        User user = createUser("user@test.com");
        Book book = createBook("테스트 도서", 15000, 100);

        Cart cart = createCart(user, book, 5);

        entityManager.flush();
        entityManager.clear();

        // when
        Optional<Cart> foundCart = cartRepository.findByUserIdAndBookId(user.getId(), book.getId());

        // then
        assertThat(foundCart).isPresent();
        assertThat(foundCart.get().getQuantity()).isEqualTo(5);
        assertThat(foundCart.get().getBook().getTitle()).isEqualTo("테스트 도서");
    }

    @Test
    @DisplayName("장바구니 수량 업데이트")
    void updateCartQuantity_success() {
        // given
        User user = createUser("user@test.com");
        Book book = createBook("테스트 도서", 15000, 100);
        Cart cart = createCart(user, book, 2);

        entityManager.flush();
        entityManager.clear();

        // when
        Cart foundCart = cartRepository.findById(cart.getId()).orElseThrow();
        foundCart.updateQuantity(5);

        entityManager.flush();
        entityManager.clear();

        // then
        Cart updatedCart = cartRepository.findById(cart.getId()).orElseThrow();
        assertThat(updatedCart.getQuantity()).isEqualTo(5);
    }

    @Test
    @DisplayName("장바구니 삭제 - deleteById")
    void deleteCart_success() {
        // given
        User user = createUser("user@test.com");
        Book book = createBook("테스트 도서", 15000, 100);
        Cart cart = createCart(user, book, 2);
        Long cartId = cart.getId();

        entityManager.flush();
        entityManager.clear();

        // when
        cartRepository.deleteById(cartId);
        entityManager.flush();
        entityManager.clear();

        // then
        Optional<Cart> deletedCart = cartRepository.findById(cartId);
        assertThat(deletedCart).isEmpty();
    }

    @Test
    @DisplayName("사용자별 장바구니 전체 삭제 - deleteByUserId")
    void deleteByUserId_success() {
        // given
        User user = createUser("user@test.com");
        Book book1 = createBook("책1", 10000, 50);
        Book book2 = createBook("책2", 20000, 30);

        createCart(user, book1, 2);
        createCart(user, book2, 3);

        entityManager.flush();
        entityManager.clear();

        // when
        cartRepository.deleteByUserId(user.getId());
        entityManager.flush();
        entityManager.clear();

        // then
        List<Cart> remainingCarts = cartRepository.findByUserId(user.getId());
        assertThat(remainingCarts).isEmpty();
    }

    @Test
    @DisplayName("도서별 장바구니 조회 - findByBookId")
    void findByBookId_success() {
        // given
        User user1 = createUser("user1@test.com");
        User user2 = createUser("user2@test.com");
        Book book = createBook("인기 도서", 25000, 100);

        createCart(user1, book, 2);
        createCart(user2, book, 3);

        entityManager.flush();
        entityManager.clear();

        // when
        List<Cart> carts = cartRepository.findByBookId(book.getId());

        // then
        assertThat(carts).hasSize(2);
        assertThat(carts).extracting("quantity").containsExactlyInAnyOrder(2, 3);
    }

    @Test
    @DisplayName("여러 사용자가 각자 장바구니를 가질 수 있음")
    void multipleUsersWithCarts() {
        // given
        User user1 = createUser("user1@test.com");
        User user2 = createUser("user2@test.com");
        Book book1 = createBook("책1", 10000, 50);
        Book book2 = createBook("책2", 20000, 30);

        createCart(user1, book1, 2);
        createCart(user1, book2, 3);
        createCart(user2, book1, 1);

        entityManager.flush();
        entityManager.clear();

        // when
        List<Cart> user1Carts = cartRepository.findByUserId(user1.getId());
        List<Cart> user2Carts = cartRepository.findByUserId(user2.getId());

        // then
        assertThat(user1Carts).hasSize(2);
        assertThat(user2Carts).hasSize(1);
    }

    @Test
    @DisplayName("장바구니 ID로 조회 - findById")
    void findById_success() {
        // given
        User user = createUser("user@test.com");
        Book book = createBook("테스트 도서", 15000, 100);
        Cart cart = createCart(user, book, 5);

        entityManager.flush();
        entityManager.clear();

        // when
        Cart foundCart = cartRepository.findById(cart.getId()).orElse(null);

        // then
        assertThat(foundCart).isNotNull();
        assertThat(foundCart.getId()).isEqualTo(cart.getId());
        assertThat(foundCart.getQuantity()).isEqualTo(5);
    }

    @Test
    @DisplayName("장바구니 개수 조회 - countByUserId")
    void countByUserId_success() {
        // given
        User user = createUser("user@test.com");
        Book book1 = createBook("책1", 10000, 50);
        Book book2 = createBook("책2", 20000, 30);
        Book book3 = createBook("책3", 30000, 80);

        createCart(user, book1, 2);
        createCart(user, book2, 1);
        createCart(user, book3, 3);

        entityManager.flush();
        entityManager.clear();

        // when
        long count = cartRepository.countByUserId(user.getId());

        // then
        assertThat(count).isEqualTo(3);
    }

    @Test
    @DisplayName("장바구니에 동일 도서 중복 저장 방지 확인")
    void preventDuplicateBookInCart() {
        // given
        User user = createUser("user@test.com");
        Book book = createBook("테스트 도서", 15000, 100);

        createCart(user, book, 2);

        entityManager.flush();
        entityManager.clear();

        // when
        Optional<Cart> existingCart = cartRepository.findByUserIdAndBookId(user.getId(), book.getId());

        // then
        assertThat(existingCart).isPresent();
        assertThat(existingCart.get().getQuantity()).isEqualTo(2);
    }

    // === Helper Methods ===

    private User createUser(String email) {
        User user = User.builder()
                .email(email)
                .uname("테스트 유저")
                .build();
        return userRepository.save(user);
    }

    private Book createBook(String title, int price, int stock) {
        Book book = Book.builder()
                .title(title)
                .author("테스트 저자")
                .company("테스트 출판사")
                .price(price)
                .stock(stock)
                .category("IT")
                .build();
        return bookRepository.save(book);
    }

    private Cart createCart(User user, Book book, int quantity) {
        Cart cart = Cart.builder()
                .user(user)
                .book(book)
                .quantity(quantity)
                .build();
        return cartRepository.save(cart);
    }
}
