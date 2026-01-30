package com.onbok.book_hub.recommendation;

import com.onbok.book_hub.book.domain.model.book.Book;
import com.onbok.book_hub.book.domain.repository.book.BookRepository;
import com.onbok.book_hub.order.domain.model.Order;
import com.onbok.book_hub.order.domain.model.OrderItem;
import com.onbok.book_hub.order.domain.repository.OrderRepository;
import com.onbok.book_hub.recommendation.application.RecommendationService;
import com.onbok.book_hub.review.domain.model.Review;
import com.onbok.book_hub.review.domain.repository.ReviewRepository;
import com.onbok.book_hub.user.domain.model.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Recommendation 단위 테스트 - 추천 규칙 검증")
class RecommendationServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private RecommendationService recommendationService;

    @Test
    @DisplayName("주문 기반 추천 - 이미 구매한 상품 제외")
    void getRecommendationsByOrderHistory_excludesPurchasedBooks() {
        // given
        User user = User.testBuilder().id(1L).email("test@test.com").build();
        Book purchasedBook = Book.testBuilder()
                .title("자바의 정석")
                .author("저자A")
                .company("출판사A")
                .build();
        ReflectionTestUtils.setField(purchasedBook, "id", 1L);

        Book recommendBook = Book.testBuilder()
                .title("스프링 인 액션")
                .author("저자A")
                .company("출판사B")
                .build();
        ReflectionTestUtils.setField(recommendBook, "id", 2L);

        Order order = Order.builder()
                .user(user)
                .orderItems(List.of(
                        OrderItem.builder().book(purchasedBook).quantity(1).build()
                ))
                .build();

        when(orderRepository.findByUserIdOrderByIdDesc(1L)).thenReturn(List.of(order));
        when(bookRepository.findById(anyLong())).thenAnswer(invocation -> {
            Long id = invocation.getArgument(0);
            if (id.equals(1L)) return Optional.of(purchasedBook);
            if (id.equals(2L)) return Optional.of(recommendBook);
            return Optional.empty();
        });
        when(bookRepository.findByAuthor(anyString())).thenReturn(List.of(purchasedBook, recommendBook));
        when(bookRepository.findByCompany(anyString())).thenReturn(new ArrayList<>());

        // when
        List<Book> recommendations = recommendationService.getRecommendationsByOrderHistory(1L, 10);

        // then
        assertThat(recommendations).doesNotContain(purchasedBook); // 구매한 책은 제외
        assertThat(recommendations).contains(recommendBook); // 같은 저자의 다른 책은 포함
    }

    @Test
    @DisplayName("리뷰 기반 추천 - 평점 4점 이상만 필터링")
    void getRecommendationsByReviews_filtersByHighRating() {
        // given
        User user = User.testBuilder()
                .email("test@test.com")
                .build();
        ReflectionTestUtils.setField(user, "id", 1L);

        Book highRatingBook = Book.testBuilder()
                .title("자바의 정석")
                .author("저자A")
                .company("출판사A")
                .build();
        ReflectionTestUtils.setField(highRatingBook, "id", 1L);

        Book lowRatingBook = Book.testBuilder()
                .title("별로인 책")
                .author("저자B")
                .company("출판사B")
                .build();
        ReflectionTestUtils.setField(lowRatingBook, "id", 2L);

        Book recommendBook = Book.testBuilder()
                .title("스프링 인 액션")
                .author("저자A")
                .company("출판사C")
                .build();
        ReflectionTestUtils.setField(recommendBook, "id", 3L);

        Review highReview = Review.builder()
                .user(user)
                .book(highRatingBook)
                .rating(5)
                .content("정말 좋아요!")
                .build();
        Review lowReview = Review.builder()
                .user(user)
                .book(lowRatingBook)
                .rating(2)
                .content("별로...")
                .build();

        when(reviewRepository.findByUserIdOrderByCreatedAtDesc(1L))
                .thenReturn(List.of(highReview, lowReview));
        when(bookRepository.findById(anyLong())).thenAnswer(invocation -> {
            Long id = invocation.getArgument(0);
            if (id.equals(1L)) return Optional.of(highRatingBook);
            if (id.equals(3L)) return Optional.of(recommendBook);
            return Optional.empty();
        });
        when(bookRepository.findByAuthor(anyString())).thenReturn(List.of(highRatingBook, recommendBook));

        // when
        List<Book> recommendations = recommendationService.getRecommendationsByReviews(1L, 10);

        // then
        // 평점 4점 이상(5점)을 준 책(highRatingBook)의 저자 책만 추천되어야 함
        assertThat(recommendations).contains(recommendBook);
        // lowRatingBook의 저자 책은 추천되지 않아야 함 (평점 2점)
    }

    @Test
    @DisplayName("협업 필터링 - 이미 구매한 상품 제외")
    void getCollaborativeFilteringRecommendations_excludesPurchasedBooks() {
        // given
        User user1 = User.testBuilder()
                .email("user1@test.com")
                .build();
        ReflectionTestUtils.setField(user1, "id", 1L);

        User user2 = User.testBuilder()
                .email("user2@test.com")
                .build();
        ReflectionTestUtils.setField(user2, "id", 2L);

        Book commonBook = Book.testBuilder()
                .title("공통 책")
                .author("저자A")
                .company("출판사A")
                .build();
        ReflectionTestUtils.setField(commonBook, "id", 1L);

        Book user1OnlyBook = Book.testBuilder()
                .title("user1 전용 책")
                .author("저자B")
                .company("출판사B")
                .build();
        ReflectionTestUtils.setField(user1OnlyBook, "id", 2L);

        Book user2OnlyBook = Book.testBuilder()
                .title("user2 전용 책")
                .author("저자A")
                .company("출판사C")
                .build();
        ReflectionTestUtils.setField(user2OnlyBook, "id", 3L);

        Order user1Order = Order.builder()
                .user(user1)
                .orderItems(List.of(
                        OrderItem.builder().book(commonBook).quantity(1).build(),
                        OrderItem.builder().book(user1OnlyBook).quantity(1).build()
                ))
                .build();

        Order user2Order = Order.builder()
                .user(user2)
                .orderItems(List.of(
                        OrderItem.builder().book(commonBook).quantity(1).build(),
                        OrderItem.builder().book(user2OnlyBook).quantity(1).build()
                ))
                .build();

        when(orderRepository.findByUserIdOrderByIdDesc(1L)).thenReturn(List.of(user1Order));
        when(orderRepository.findAll()).thenReturn(List.of(user1Order, user2Order));
        when(orderRepository.findByUserIdOrderByIdDesc(2L)).thenReturn(List.of(user2Order));
        when(bookRepository.findById(anyLong())).thenAnswer(invocation -> {
            Long id = invocation.getArgument(0);
            if (id.equals(1L)) return Optional.of(commonBook);
            if (id.equals(2L)) return Optional.of(user1OnlyBook);
            if (id.equals(3L)) return Optional.of(user2OnlyBook);
            return Optional.empty();
        });

        // when
        List<Book> recommendations = recommendationService.getCollaborativeFilteringRecommendations(1L, 10);

        // then
        assertThat(recommendations).contains(user2OnlyBook); // user2만 산 책 추천
        assertThat(recommendations).doesNotContain(commonBook); // 이미 구매한 공통 책 제외
        assertThat(recommendations).doesNotContain(user1OnlyBook); // 이미 구매한 책 제외
    }

    @Test
    @DisplayName("인기 도서 추천 - 주문 수량 기준 정렬")
    void getPopularBooks_sortsByOrderCount() {
        // given
        Book book1 = Book.testBuilder()
                .title("인기 1위")
                .author("저자A")
                .company("출판사A")
                .build();
        ReflectionTestUtils.setField(book1, "id", 1L);

        Book book2 = Book.testBuilder()
                .title("인기 2위")
                .author("저자B")
                .company("출판사B")
                .build();
        ReflectionTestUtils.setField(book2, "id", 2L);

        Book book3 = Book.testBuilder()
                .title("인기 3위")
                .author("저자A")
                .company("출판사C")
                .build();
        ReflectionTestUtils.setField(book3, "id", 3L);

        Order order1 = Order.builder()
                .orderItems(List.of(
                        OrderItem.builder().book(book1).quantity(10).build(),
                        OrderItem.builder().book(book2).quantity(5).build()
                ))
                .build();

        Order order2 = Order.builder()
                .orderItems(List.of(
                        OrderItem.builder().book(book3).quantity(3).build()
                ))
                .build();

        when(orderRepository.findAll()).thenReturn(List.of(order1, order2));
        when(bookRepository.findById(anyLong())).thenAnswer(invocation -> {
            Long id = invocation.getArgument(0);
            if (id.equals(1L)) return Optional.of(book1);
            if (id.equals(2L)) return Optional.of(book2);
            if (id.equals(3L)) return Optional.of(book3);
            return Optional.empty();
        });

        // when
        List<Book> popularBooks = recommendationService.getPopularBooks(3);

        // then
        assertThat(popularBooks).hasSize(3);
        assertThat(popularBooks.get(0)).isEqualTo(book1); // 10개 주문
        assertThat(popularBooks.get(1)).isEqualTo(book2); // 5개 주문
        assertThat(popularBooks.get(2)).isEqualTo(book3); // 3개 주문
    }

    @Test
    @DisplayName("평점 높은 도서 추천 - 최소 리뷰 3개 이상 필터링")
    void getHighlyRatedBooks_requiresMinimumReviews() {
        // given
        Book popularBook = Book.testBuilder()
                .title("인기 도서 (리뷰 5개)")
                .author("저자A")
                .company("출판사A")
                .build();
        ReflectionTestUtils.setField(popularBook, "id", 1L);

        Book unpopularBook = Book.testBuilder()
                .title("비인기 도서 (리뷰 1개)")
                .author("저자B")
                .company("출판사B")
                .build();
        ReflectionTestUtils.setField(unpopularBook, "id", 2L);

        List<Review> reviews = new ArrayList<>();

        // popularBook: 리뷰 5개 (평균 4.6점)
        for (int i = 0; i < 5; i++) {
            reviews.add(Review.builder().book(popularBook).rating(5).build());
        }

        // unpopularBook: 리뷰 1개 (5점이지만 개수 부족)
        reviews.add(Review.builder().book(unpopularBook).rating(5).build());

        when(reviewRepository.findAll()).thenReturn(reviews);
        when(bookRepository.findById(anyLong())).thenAnswer(invocation -> {
            Long id = invocation.getArgument(0);
            if (id.equals(1L)) return Optional.of(popularBook);
            return Optional.empty();
        });

        // when
        List<Book> highlyRatedBooks = recommendationService.getHighlyRatedBooks(10);

        // then
        assertThat(highlyRatedBooks).contains(popularBook); // 리뷰 5개 -> 포함
        assertThat(highlyRatedBooks).doesNotContain(unpopularBook); // 리뷰 1개 -> 제외
    }
}
