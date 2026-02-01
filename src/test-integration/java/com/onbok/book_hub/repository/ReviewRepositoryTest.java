package com.onbok.book_hub.repository;

import com.onbok.book_hub.book.domain.model.book.Book;
import com.onbok.book_hub.book.domain.repository.book.BookRepository;
import com.onbok.book_hub.review.domain.model.Review;
import com.onbok.book_hub.review.domain.repository.ReviewRepository;
import com.onbok.book_hub.user.domain.model.User;
import com.onbok.book_hub.user.domain.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@EntityScan(basePackages = "com.onbok.book_hub")
@EnableJpaRepositories(basePackages = "com.onbok.book_hub")
@DisplayName("Review Repository 계층 테스트 - @DataJpaTest")
class ReviewRepositoryTest {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("리뷰 저장 - 정상 케이스")
    void saveReview_success() {
        // given
        User user = createUser("test@test.com");
        Book book = createBook("자바의 정석", 30000);

        Review review = Review.builder()
                .user(user)
                .book(book)
                .rating(5)
                .content("정말 좋은 책입니다!")
                .build();

        // when
        Review savedReview = reviewRepository.save(review);
        entityManager.flush();
        entityManager.clear();

        // then
        assertThat(savedReview.getId()).isNotNull();
        assertThat(savedReview.getRating()).isEqualTo(5);
        assertThat(savedReview.getContent()).isEqualTo("정말 좋은 책입니다!");
        assertThat(savedReview.getUser().getEmail()).isEqualTo("test@test.com");
        assertThat(savedReview.getBook().getTitle()).isEqualTo("자바의 정석");
    }

    @Test
    @DisplayName("사용자별 리뷰 조회 - findByUserId")
    void findByUserId_success() {
        // given
        User user = createUser("user@test.com");
        Book book1 = createBook("책1", 10000);
        Book book2 = createBook("책2", 20000);

        Review review1 = createReview(user, book1, 5, "리뷰1");
        Review review2 = createReview(user, book2, 4, "리뷰2");

        entityManager.flush();
        entityManager.clear();

        // when
        List<Review> reviews = reviewRepository.findByUserId(user.getId());

        // then
        assertThat(reviews).hasSize(2);
        assertThat(reviews).extracting("content").containsExactlyInAnyOrder("리뷰1", "리뷰2");
    }

    @Test
    @DisplayName("도서별 리뷰 조회 - findByBookId")
    void findByBookId_success() {
        // given
        User user1 = createUser("user1@test.com");
        User user2 = createUser("user2@test.com");
        Book book = createBook("인기 도서", 25000);

        Review review1 = createReview(user1, book, 5, "user1 리뷰");
        Review review2 = createReview(user2, book, 4, "user2 리뷰");

        entityManager.flush();
        entityManager.clear();

        // when
        List<Review> reviews = reviewRepository.findByBookId(book.getId());

        // then
        assertThat(reviews).hasSize(2);
        assertThat(reviews).extracting("rating").containsExactlyInAnyOrder(5, 4);
    }

    @Test
    @DisplayName("중복 리뷰 체크 - findByUserIdAndBookId")
    void findByUserIdAndBookId_checkDuplication() {
        // given
        User user = createUser("user@test.com");
        Book book = createBook("테스트 도서", 15000);

        Review review = createReview(user, book, 5, "첫 리뷰");

        entityManager.flush();
        entityManager.clear();

        // when
        Optional<Review> existingReview = reviewRepository.findByUserIdAndBookId(user.getId(), book.getId());

        // then
        assertThat(existingReview).isPresent();
        assertThat(existingReview.get().getContent()).isEqualTo("첫 리뷰");
    }

    @Test
    @DisplayName("중복 리뷰 없음 - findByUserIdAndBookId 반환값 empty")
    void findByUserIdAndBookId_noDuplication() {
        // given
        User user = createUser("user@test.com");
        Book book = createBook("테스트 도서", 15000);

        // when
        Optional<Review> existingReview = reviewRepository.findByUserIdAndBookId(user.getId(), book.getId());

        // then
        assertThat(existingReview).isEmpty();
    }

    @Test
    @DisplayName("평점별 리뷰 조회 - findByRating")
    void findByRating_success() {
        // given
        User user = createUser("user@test.com");
        Book book1 = createBook("책1", 10000);
        Book book2 = createBook("책2", 20000);
        Book book3 = createBook("책3", 30000);

        Review fiveStarReview1 = createReview(user, book1, 5, "5점 리뷰1");
        Review fiveStarReview2 = createReview(user, book2, 5, "5점 리뷰2");
        Review threeStarReview = createReview(user, book3, 3, "3점 리뷰");

        entityManager.flush();
        entityManager.clear();

        // when
        List<Review> fiveStarReviews = reviewRepository.findByRating(5);
        List<Review> threeStarReviews = reviewRepository.findByRating(3);

        // then
        assertThat(fiveStarReviews).hasSize(2);
        assertThat(threeStarReviews).hasSize(1);
    }

    @Test
    @DisplayName("리뷰 평점 범위 조회 - findByRatingGreaterThanEqual")
    void findByRatingGreaterThanEqual_success() {
        // given
        User user = createUser("user@test.com");
        Book book1 = createBook("책1", 10000);
        Book book2 = createBook("책2", 20000);
        Book book3 = createBook("책3", 30000);
        Book book4 = createBook("책4", 40000);

        createReview(user, book1, 5, "5점");
        createReview(user, book2, 4, "4점");
        createReview(user, book3, 3, "3점");
        createReview(user, book4, 2, "2점");

        entityManager.flush();
        entityManager.clear();

        // when
        List<Review> highRatedReviews = reviewRepository.findByRatingGreaterThanEqual(4);

        // then
        assertThat(highRatedReviews).hasSize(2); // 5점, 4점만
        assertThat(highRatedReviews).extracting("rating").containsExactlyInAnyOrder(5, 4);
    }

    @Test
    @DisplayName("리뷰 ID로 조회 - findById")
    void findById_success() {
        // given
        User user = createUser("user@test.com");
        Book book = createBook("테스트 도서", 15000);
        Review review = createReview(user, book, 5, "테스트 리뷰");

        entityManager.flush();
        entityManager.clear();

        // when
        Review foundReview = reviewRepository.findById(review.getId()).orElse(null);

        // then
        assertThat(foundReview).isNotNull();
        assertThat(foundReview.getId()).isEqualTo(review.getId());
        assertThat(foundReview.getContent()).isEqualTo("테스트 리뷰");
    }

    @Test
    @DisplayName("리뷰 삭제 - deleteById")
    void deleteById_success() {
        // given
        User user = createUser("user@test.com");
        Book book = createBook("테스트 도서", 15000);
        Review review = createReview(user, book, 5, "삭제할 리뷰");
        Long reviewId = review.getId();

        entityManager.flush();
        entityManager.clear();

        // when
        reviewRepository.deleteById(reviewId);
        entityManager.flush();
        entityManager.clear();

        // then
        Optional<Review> deletedReview = reviewRepository.findById(reviewId);
        assertThat(deletedReview).isEmpty();
    }

    @Test
    @DisplayName("여러 사용자의 리뷰가 동일 도서에 저장 가능")
    void multipleUsersReviewSameBook() {
        // given
        User user1 = createUser("user1@test.com");
        User user2 = createUser("user2@test.com");
        User user3 = createUser("user3@test.com");
        Book book = createBook("인기 도서", 25000);

        createReview(user1, book, 5, "user1 리뷰");
        createReview(user2, book, 4, "user2 리뷰");
        createReview(user3, book, 3, "user3 리뷰");

        entityManager.flush();
        entityManager.clear();

        // when
        List<Review> reviews = reviewRepository.findByBookId(book.getId());

        // then
        assertThat(reviews).hasSize(3);
        assertThat(reviews).extracting("rating").containsExactlyInAnyOrder(5, 4, 3);
    }

    // === Helper Methods ===

    private User createUser(String email) {
        User user = User.builder()
                .email(email)
                .uname("테스트 유저")
                .build();
        return userRepository.save(user);
    }

    private Book createBook(String title, int price) {
        Book book = Book.builder()
                .title(title)
                .author("테스트 저자")
                .company("테스트 출판사")
                .price(price)
                .stock(100)
                .category("IT")
                .build();
        return bookRepository.save(book);
    }

    private Review createReview(User user, Book book, int rating, String content) {
        Review review = Review.builder()
                .user(user)
                .book(book)
                .rating(rating)
                .content(content)
                .build();
        return reviewRepository.save(review);
    }
}
