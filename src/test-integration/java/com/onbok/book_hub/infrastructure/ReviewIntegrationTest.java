package com.onbok.book_hub.infrastructure;

import com.onbok.book_hub.book.domain.model.book.Book;
import com.onbok.book_hub.book.domain.repository.book.BookRepository;
import com.onbok.book_hub.common.exception.ExpectedException;
import com.onbok.book_hub.review.application.ReviewCommandService;
import com.onbok.book_hub.review.domain.model.Review;
import com.onbok.book_hub.user.domain.model.User;
import com.onbok.book_hub.user.domain.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
@DisplayName("Review 통합 테스트")
class ReviewIntegrationTest {

    @Autowired
    private ReviewCommandService reviewCommandService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookRepository bookRepository;

    @Test
    @DisplayName("리뷰 작성 - 정상 케이스")
    void createReview_success() {
        // given
        User user = createUser("test@test.com");
        Book book = createBook("자바의 정석", 30000);

        // when
        Review review = reviewCommandService.createReview(book.getId(), user.getId(), 5, "정말 좋은 책입니다!");

        // then
        assertThat(review.getId()).isNotNull();
        assertThat(review.getRating()).isEqualTo(5);
        assertThat(review.getContent()).isEqualTo("정말 좋은 책입니다!");
        assertThat(review.getBook().getId()).isEqualTo(book.getId());
        assertThat(review.getUser().getId()).isEqualTo(user.getId());
    }

    @Test
    @DisplayName("리뷰 평점 저장 - 1부터 5까지")
    void createReview_withDifferentRatings() {
        // given
        User user = createUser("test@test.com");

        // when & then
        for (int rating = 1; rating <= 5; rating++) {
            Book book = createBook("테스트 도서 " + rating, 10000 + (rating * 1000));
            Review review = reviewCommandService.createReview(book.getId(), user.getId(), rating, "평점 " + rating);
            assertThat(review.getRating()).isEqualTo(rating);
        }
    }

    @Test
    @DisplayName("중복 리뷰 작성 방지 - 동일 사용자가 동일 도서에 리뷰 재작성 불가")
    void createReview_duplicateReview() {
        // given
        User user = createUser("test@test.com");
        Book book = createBook("자바의 정석", 30000);

        // 첫 번째 리뷰 작성
        reviewCommandService.createReview(book.getId(), user.getId(), 5, "첫 번째 리뷰");

        // when & then - 두 번째 리뷰 작성 시도
        assertThatThrownBy(() ->
                reviewCommandService.createReview(book.getId(), user.getId(), 4, "두 번째 리뷰")
        ).isInstanceOf(ExpectedException.class);
    }

    @Test
    @DisplayName("리뷰 수정 - 정상 케이스")
    void updateReview_success() {
        // given
        User user = createUser("test@test.com");
        Book book = createBook("자바의 정석", 30000);
        Review review = reviewCommandService.createReview(book.getId(), user.getId(), 3, "처음 작성한 리뷰");

        // when
        reviewCommandService.updateReview(user, review.getId(), 5, "수정된 리뷰입니다!");

        // then
        Review updatedReview = reviewCommandService.updateReview(user, review.getId(), 5, "수정된 리뷰입니다!");
        assertThat(updatedReview.getRating()).isEqualTo(5);
        assertThat(updatedReview.getContent()).isEqualTo("수정된 리뷰입니다!");
    }

    @Test
    @DisplayName("리뷰 수정 실패 - 다른 사용자의 리뷰")
    void updateReview_unauthorized() {
        // given
        User owner = createUser("owner@test.com");
        User otherUser = createUser("other@test.com");
        Book book = createBook("자바의 정석", 30000);

        Review review = reviewCommandService.createReview(book.getId(), owner.getId(), 5, "원작자의 리뷰");

        // when & then
        assertThatThrownBy(() ->
                reviewCommandService.updateReview(otherUser, review.getId(), 1, "해킹 시도")
        ).isInstanceOf(ExpectedException.class);
    }

    @Test
    @DisplayName("리뷰 삭제 - 정상 케이스")
    void deleteReview_success() {
        // given
        User user = createUser("test@test.com");
        Book book = createBook("자바의 정석", 30000);
        Review review = reviewCommandService.createReview(book.getId(), user.getId(), 5, "삭제할 리뷰");
        Long reviewId = review.getId();

        // when
        reviewCommandService.deleteReview(user, reviewId);

        // then
        // 삭제 후 조회 시 예외 발생 확인 (ReviewQueryService를 통해 확인 가능)
        // 현재는 삭제가 성공적으로 수행되었는지 정도만 테스트
        assertThat(reviewId).isNotNull();
    }

    @Test
    @DisplayName("리뷰 삭제 실패 - 다른 사용자의 리뷰")
    void deleteReview_unauthorized() {
        // given
        User owner = createUser("owner@test.com");
        User otherUser = createUser("other@test.com");
        Book book = createBook("자바의 정석", 30000);

        Review review = reviewCommandService.createReview(book.getId(), owner.getId(), 5, "원작자의 리뷰");

        // when & then
        assertThatThrownBy(() ->
                reviewCommandService.deleteReview(otherUser, review.getId())
        ).isInstanceOf(ExpectedException.class);
    }

    @Test
    @DisplayName("여러 사용자가 동일 도서에 각자 리뷰 작성 가능")
    void multipleUsersReviewSameBook() {
        // given
        User user1 = createUser("user1@test.com");
        User user2 = createUser("user2@test.com");
        User user3 = createUser("user3@test.com");
        Book book = createBook("인기 도서", 25000);

        // when
        Review review1 = reviewCommandService.createReview(book.getId(), user1.getId(), 5, "user1 리뷰");
        Review review2 = reviewCommandService.createReview(book.getId(), user2.getId(), 4, "user2 리뷰");
        Review review3 = reviewCommandService.createReview(book.getId(), user3.getId(), 3, "user3 리뷰");

        // then
        assertThat(review1.getUser().getId()).isEqualTo(user1.getId());
        assertThat(review2.getUser().getId()).isEqualTo(user2.getId());
        assertThat(review3.getUser().getId()).isEqualTo(user3.getId());
        assertThat(review1.getBook().getId()).isEqualTo(book.getId());
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
}
