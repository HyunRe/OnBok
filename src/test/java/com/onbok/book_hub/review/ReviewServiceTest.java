package com.onbok.book_hub.review;

import com.onbok.book_hub.book.domain.model.book.Book;
import com.onbok.book_hub.book.domain.repository.book.BookRepository;
import com.onbok.book_hub.review.application.ReviewCommandService;
import com.onbok.book_hub.review.application.ReviewQueryService;
import com.onbok.book_hub.review.domain.model.Review;
import com.onbok.book_hub.review.domain.repository.ReviewRepository;
import com.onbok.book_hub.review.dto.response.ReviewResponseDto;
import com.onbok.book_hub.user.domain.model.User;
import com.onbok.book_hub.user.domain.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
@DisplayName("ReviewService 통합 테스트 - CQS Pattern")
class ReviewServiceTest {
    @Autowired private ReviewCommandService reviewCommandService;
    @Autowired private ReviewQueryService reviewQueryService;
    @Autowired private ReviewRepository reviewRepository;
    @Autowired private BookRepository bookRepository;
    @Autowired private UserRepository userRepository;

    private User testUser;
    private Book testBook;
    private Review testReview;

    @BeforeEach
    void setUp() {
        // 테스트 사용자 생성
        testUser = User.builder()
                .uname("리뷰 작성자")
                .email("review@test.com")
                .role("ROLE_USER")
                .regDate(LocalDate.now())
                .build();
        testUser = userRepository.save(testUser);

        // 테스트 도서 생성
        testBook = Book.builder()
                .title("리뷰 테스트 도서")
                .author("리뷰 저자")
                .company("리뷰 출판사")
                .price(20000)
                .stock(50)
                .build();
        testBook = bookRepository.save(testBook);
    }

    @AfterEach
    void tearDown() {
        if (testReview != null) reviewRepository.deleteById(testReview.getId());
        if (testBook != null) bookRepository.deleteById(testBook.getId());
        if (testUser != null) userRepository.deleteById(testUser.getId());
    }

    @Test
    @DisplayName("리뷰 작성 - 정상 케이스")
    void createReview_success() {
        // when
        Review review = reviewCommandService.createReview(
                testBook.getId(),
                testUser.getId(),
                5,
                "정말 좋은 책입니다!"
        );
        testReview = review;

        // then
        assertThat(review).isNotNull();
        assertThat(review.getRating()).isEqualTo(5);
        assertThat(review.getContent()).isEqualTo("정말 좋은 책입니다!");
        assertThat(review.getBook().getId()).isEqualTo(testBook.getId());
        assertThat(review.getUser().getId()).isEqualTo(testUser.getId());
    }

    @Test
    @DisplayName("리뷰 작성 - 중복 작성 시 예외")
    void createReview_duplicate() {
        // given
        testReview = reviewCommandService.createReview(testBook.getId(), testUser.getId(), 4, "첫 리뷰");

        // when & then
        assertThatThrownBy(() ->
                reviewCommandService.createReview(testBook.getId(), testUser.getId(), 5, "중복 리뷰"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("이미 리뷰를 작성하셨습니다");
    }

    @Test
    @DisplayName("리뷰 수정 - 정상 케이스")
    void updateReview_success() {
        // given
        testReview = reviewCommandService.createReview(testBook.getId(), testUser.getId(), 3, "초기 리뷰");

        // when
        Review updatedReview = reviewCommandService.updateReview(testUser, 5L, 5,"수정된 리뷰 내용");

        // then
        assertThat(updatedReview.getRating()).isEqualTo(5);
        assertThat(updatedReview.getContent()).isEqualTo("수정된 리뷰 내용");
        assertThat(updatedReview.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("리뷰 삭제")
    void deleteReview() {
        // given
        testReview = reviewCommandService.createReview(testBook.getId(), testUser.getId(), 4, "삭제할 리뷰");
        Long reviewId = testReview.getId();

        // when
        reviewCommandService.deleteReview(testUser, reviewId);

        // then
        Review deletedReview = reviewRepository.findById(reviewId).orElse(null);
        assertThat(deletedReview).isNull();
        testReview = null; // tearDown에서 다시 삭제하지 않도록
    }

    @Test
    @DisplayName("도서별 리뷰 조회 - 페이징")
    void getReviewsByBook() {
        // given
        reviewCommandService.createReview(testBook.getId(), testUser.getId(), 5, "리뷰 1");

        User user2 = User.builder()
                .uname("사용자2")
                .email("user2@test.com")
                .role("ROLE_USER")
                .regDate(LocalDate.now())
                .build();
        userRepository.save(user2);

        reviewCommandService.createReview(testBook.getId(), user2.getId(), 4, "리뷰 2");

        // when
        Page<ReviewResponseDto> reviews = reviewQueryService.getReviewsByBook(testBook.getId(), 1);

        // then
        assertThat(reviews.getContent()).hasSize(2);
        assertThat(reviews.getTotalElements()).isEqualTo(2);

        // cleanup
        userRepository.deleteById(user2.getId());
    }

    @Test
    @DisplayName("사용자별 리뷰 조회")
    void getReviewsByUser() {
        // given
        testReview = reviewCommandService.createReview(testBook.getId(), testUser.getId(), 5, "내 리뷰");

        // when
        List<ReviewResponseDto> reviews = reviewQueryService.getReviewsByUser(testUser.getId());

        // then
        assertThat(reviews).isNotEmpty();
        assertThat(reviews).anyMatch(dto -> dto.getId().equals(testReview.getId()));
    }

    @Test
    @DisplayName("평균 평점 계산")
    void getAverageRating() {
        // given
        reviewCommandService.createReview(testBook.getId(), testUser.getId(), 5, "리뷰 1");

        User user2 = User.builder()
                .uname("사용자3")
                .email("user3@test.com")
                .role("ROLE_USER")
                .regDate(LocalDate.now())
                .build();
        userRepository.save(user2);

        reviewCommandService.createReview(testBook.getId(), user2.getId(), 3, "리뷰 2");

        // when
        Double avgRating = reviewQueryService.getAverageRating(testBook.getId());

        // then
        assertThat(avgRating).isEqualTo(4.0); // (5 + 3) / 2 = 4.0

        // cleanup
        userRepository.deleteById(user2.getId());
    }

    @Test
    @DisplayName("리뷰 개수 조회")
    void getReviewCount() {
        // given
        testReview = reviewCommandService.createReview(testBook.getId(), testUser.getId(), 5, "리뷰");

        // when
        long count = reviewQueryService.getReviewCount(testBook.getId());

        // then
        assertThat(count).isEqualTo(1);
    }

    @Test
    @DisplayName("존재하지 않는 도서에 리뷰 작성 시 예외")
    void createReview_bookNotFound() {
        // when & then
        assertThatThrownBy(() ->
                reviewCommandService.createReview(999999L, testUser.getId(), 5, "리뷰"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("도서를 찾을 수 없습니다");
    }
}
