package com.onbok.book_hub.review;

import com.onbok.book_hub.review.domain.model.Review;
import com.onbok.book_hub.common.exception.ExpectedException;
import com.onbok.book_hub.common.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Review 도메인 테스트")
class ReviewTest {

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 4, 5})
    @DisplayName("평점 1-5 사이 값으로 업데이트 시 정상 설정")
    void update_validRating(int rating) {
        // given
        Review review = Review.builder()
                .rating(3)
                .content("초기 내용")
                .build();
        String newContent = "수정된 리뷰 내용입니다.";

        // when
        review.update(rating, newContent);

        // then
        assertThat(review.getRating()).isEqualTo(rating);
        assertThat(review.getContent()).isEqualTo(newContent);
    }

    @Test
    @DisplayName("평점 범위를 벗어나면 ExpectedException 발생")
    void update_invalidRatingRange() {
        // given
        Review review = Review.builder().build();

        // when & then
        // 1. 최소값 미만 (0)
        assertThatThrownBy(() -> review.update(0, "내용"))
                .isInstanceOf(ExpectedException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_RATING_RANGE);

        // 2. 최대값 초과 (6)
        assertThatThrownBy(() -> review.update(6, "내용"))
                .isInstanceOf(ExpectedException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_RATING_RANGE);

        // 3. 음수 (-1)
        assertThatThrownBy(() -> review.update(-1, "내용"))
                .isInstanceOf(ExpectedException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_RATING_RANGE);
    }

    @Test
    @DisplayName("빌더를 통한 리뷰 생성 테스트")
    void builder_createReview() {
        // given
        String content = "정말 유익한 책입니다.";
        int rating = 5;

        // when
        Review review = Review.builder()
                .rating(rating)
                .content(content)
                .build();

        // then
        assertThat(review.getRating()).isEqualTo(rating);
        assertThat(review.getContent()).isEqualTo(content);
    }
}