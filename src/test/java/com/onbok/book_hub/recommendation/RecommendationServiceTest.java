package com.onbok.book_hub.recommendation;

import com.onbok.book_hub.book.domain.model.book.Book;
import com.onbok.book_hub.book.domain.repository.book.BookRepository;
import com.onbok.book_hub.order.domain.model.Order;
import com.onbok.book_hub.order.domain.model.OrderItem;
import com.onbok.book_hub.order.domain.model.OrderStatus;
import com.onbok.book_hub.order.domain.repository.OrderRepository;
import com.onbok.book_hub.recommendation.application.RecommendationService;
import com.onbok.book_hub.review.domain.model.Review;
import com.onbok.book_hub.review.domain.repository.ReviewRepository;
import com.onbok.book_hub.user.domain.model.User;
import com.onbok.book_hub.user.domain.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@DisplayName("RecommendationService 통합 테스트")
class RecommendationServiceTest {
    @Autowired private RecommendationService recommendationService;
    @Autowired private UserRepository userRepository;
    @Autowired private BookRepository bookRepository;
    @Autowired private OrderRepository orderRepository;
    @Autowired private ReviewRepository reviewRepository;

    private User testUser;
    private Book book1;
    private Book book2;
    private Book book3;
    private List<Order> testOrders = new ArrayList<>();
    private List<Review> testReviews = new ArrayList<>();

    @BeforeEach
    void setUp() {
        // 테스트 사용자 생성
        testUser = User.builder()
                .uname("추천 테스트 사용자")
                .email("rec@test.com")
                .role("ROLE_USER")
                .regDate(LocalDate.now())
                .build();
        testUser = userRepository.save(testUser);

        // 테스트 도서 3권 생성
        book1 = Book.builder()
                .title("자바의 정석")
                .author("남궁성")
                .company("도우출판")
                .price(30000)
                .stock(100)
                .build();
        book1 = bookRepository.save(book1);

        book2 = Book.builder()
                .title("이펙티브 자바")
                .author("조슈아 블로크")
                .company("인사이트")
                .price(35000)
                .stock(100)
                .build();
        book2 = bookRepository.save(book2);

        book3 = Book.builder()
                .title("자바 프로그래밍 완전정복")
                .author("남궁성")
                .company("도우출판")
                .price(28000)
                .stock(100)
                .build();
        book3 = bookRepository.save(book3);
    }

    @AfterEach
    void tearDown() {
        testReviews.forEach(review -> reviewRepository.deleteById(review.getId()));
        testOrders.forEach(order -> orderRepository.deleteById(order.getId()));
        if (book1 != null) bookRepository.deleteById(book1.getId());
        if (book2 != null) bookRepository.deleteById(book2.getId());
        if (book3 != null) bookRepository.deleteById(book3.getId());
        if (testUser != null) userRepository.deleteById(testUser.getId());
    }

    @Test
    @DisplayName("주문 내역 기반 추천 - 같은 저자의 다른 책 추천")
    void getRecommendationsByOrderHistory() {
        // given - book1(남궁성)을 구매
        Order order = createTestOrder(book1, 1);
        testOrders.add(order);

        // when - 추천 도서 조회
        List<Book> recommendations = recommendationService.getRecommendationsByOrderHistory(testUser.getId(), 10);

        // then - book3(같은 저자 남궁성)이 추천되어야 함
        assertThat(recommendations).isNotEmpty();
        assertThat(recommendations).anyMatch(book -> book.getId() == book3.getId());
    }

    @Test
    @DisplayName("리뷰 기반 추천 - 높은 평점을 준 책과 유사한 책 추천")
    void getRecommendationsByReviews() {
        // given - book1에 5점 리뷰 작성
        Review review = Review.builder()
                .book(book1)
                .user(testUser)
                .rating(5)
                .content("최고의 책!")
                .build();
        review = reviewRepository.save(review);
        testReviews.add(review);

        // when
        List<Book> recommendations = recommendationService.getRecommendationsByReviews(testUser.getId(), 10);

        // then - 같은 저자의 book3가 추천되어야 함
        assertThat(recommendations).isNotEmpty();
        assertThat(recommendations).anyMatch(book -> book.getId() == book3.getId());
    }

    @Test
    @DisplayName("인기 도서 조회 - 주문이 많은 순")
    void getPopularBooks() {
        // given - book1을 여러 번 주문
        Order order1 = createTestOrder(book1, 5);
        Order order2 = createTestOrder(book2, 2);
        testOrders.add(order1);
        testOrders.add(order2);

        // when
        List<Book> popularBooks = recommendationService.getPopularBooks(10);

        // then - book1이 더 많이 주문되었으므로 앞에 와야 함
        assertThat(popularBooks).isNotEmpty();
        if (popularBooks.size() >= 2) {
            assertThat(popularBooks.get(0).getId()).isEqualTo(book1.getId());
        }
    }

    @Test
    @DisplayName("평점 높은 도서 조회")
    void getHighlyRatedBooks() {
        // given - 여러 사용자가 book1에 높은 평점
        createReviews(book1, 5, 3); // 3개의 5점 리뷰
        createReviews(book2, 3, 3); // 3개의 3점 리뷰

        // when
        List<Book> highlyRatedBooks = recommendationService.getHighlyRatedBooks(10);

        // then - book1이 평점이 더 높으므로 앞에 와야 함
        assertThat(highlyRatedBooks).isNotEmpty();
        if (highlyRatedBooks.size() >= 2) {
            assertThat(highlyRatedBooks.get(0).getId()).isEqualTo(book1.getId());
        }
    }

    @Test
    @DisplayName("구매 이력이 없는 경우 인기 도서 반환")
    void getRecommendationsByOrderHistory_noHistory() {
        // given - 다른 사용자의 주문만 있음
        User otherUser = User.builder()
                .uname("다른 사용자")
                .email("other@test.com")
                .role("ROLE_USER")
                .regDate(LocalDate.now())
                .build();
        otherUser = userRepository.save(otherUser);

        Order otherOrder = Order.builder()
                .user(otherUser)
                .totalAmount(30000)
                .status(OrderStatus.PENDING)
                .orderDateTime(LocalDateTime.now())
                .build();
        OrderItem item = OrderItem.builder()
                .order(otherOrder)
                .book(book1)
                .quantity(2)
                .subPrice(60000)
                .build();
        otherOrder.addOrderItem(item);
        orderRepository.save(otherOrder);

        // when - testUser는 구매 이력이 없음
        List<Book> recommendations = recommendationService.getRecommendationsByOrderHistory(testUser.getId(), 10);

        // then - 인기 도서(book1)가 반환되어야 함
        assertThat(recommendations).isNotEmpty();
        assertThat(recommendations).anyMatch(book -> book.getId() == book1.getId());

        // cleanup
        orderRepository.deleteById(otherOrder.getId());
        userRepository.deleteById(otherUser.getId());
    }

    // Helper methods
    private Order createTestOrder(Book book, int quantity) {
        Order order = Order.builder()
                .user(testUser)
                .totalAmount(book.getPrice() * quantity)
                .status(OrderStatus.PENDING)
                .orderDateTime(LocalDateTime.now())
                .build();

        OrderItem item = OrderItem.builder()
                .order(order)
                .book(book)
                .quantity(quantity)
                .subPrice(book.getPrice() * quantity)
                .build();

        order.addOrderItem(item);
        return orderRepository.save(order);
    }

    private void createReviews(Book book, int rating, int count) {
        for (int i = 0; i < count; i++) {
            User user = User.builder()
                    .uname("리뷰어" + i)
                    .email("reviewer" + i + "@test.com")
                    .role("ROLE_USER")
                    .regDate(LocalDate.now())
                    .build();
            userRepository.save(user);

            Review review = Review.builder()
                    .book(book)
                    .user(user)
                    .rating(rating)
                    .content("리뷰 " + i)
                    .build();
            review = reviewRepository.save(review);
            testReviews.add(review);
        }
    }
}
