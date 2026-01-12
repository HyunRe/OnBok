package com.onbok.book_hub.recommendation.presentation;

import com.onbok.book_hub.book.domain.model.book.Book;
import com.onbok.book_hub.common.annotation.CurrentUser;
import com.onbok.book_hub.common.response.OnBokResponse;
import com.onbok.book_hub.recommendation.application.RecommendationService;
import com.onbok.book_hub.recommendation.dto.PersonalizedRecommendationResponseDto;
import com.onbok.book_hub.recommendation.dto.RecommendationResponseDto;
import com.onbok.book_hub.recommendation.model.RecommendationType;
import com.onbok.book_hub.user.domain.model.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Recommendation", description = "추천 API")
@RestController
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
public class RecommendationApiController {
    private final RecommendationService recommendationService;

    @Operation(summary = "주문 내역 기반 추천", description = "현재 사용자의 과거 주문 기록을 분석하여 유사한 도서를 추천합니다.")
    @GetMapping("/order-based")
    public OnBokResponse<RecommendationResponseDto> getOrderBasedRecommendations(@CurrentUser User user,
                                                                                 @RequestParam(defaultValue = "10") int limit) {
        List<Book> recommendations = recommendationService.getRecommendationsByOrderHistory(user.getId(), limit);
        RecommendationResponseDto data = new RecommendationResponseDto(RecommendationType.ORDER_BASED, recommendations, recommendations.size());
        return OnBokResponse.success(data);
    }

    @Operation(summary = "리뷰 기반 추천", description = "사용자가 작성한 리뷰와 평점을 바탕으로 선호할 만한 도서를 추천합니다.")
    @GetMapping("/review-based")
    public OnBokResponse<RecommendationResponseDto> getReviewBasedRecommendations(@CurrentUser User user,
                                                                                  @RequestParam(defaultValue = "10") int limit) {
        List<Book> recommendations = recommendationService.getRecommendationsByReviews(user.getId(), limit);
        RecommendationResponseDto data = new RecommendationResponseDto(RecommendationType.REVIEW_BASED, recommendations, recommendations.size());
        return OnBokResponse.success(data);
    }

    @Operation(summary = "협업 필터링 기반 추천", description = "유사한 취향을 가진 다른 사용자들이 구매한 도서를 분석하여 추천합니다.")
    @GetMapping("/collaborative")
    public OnBokResponse<RecommendationResponseDto> getCollaborativeRecommendations(@CurrentUser User user,
                                                                                    @RequestParam(defaultValue = "10") int limit) {
        List<Book> recommendations = recommendationService.getCollaborativeFilteringRecommendations(user.getId(), limit);
        RecommendationResponseDto data = new RecommendationResponseDto(RecommendationType.COLLABORATIVE, recommendations, recommendations.size());
        return OnBokResponse.success(data);
    }

    @Operation(summary = "인기 도서 추천", description = "전체 사용자들 사이에서 판매량이 높은 인기 도서 목록을 반환합니다.")
    @GetMapping("/popular")
    public OnBokResponse<RecommendationResponseDto> getPopularBooks(@RequestParam(defaultValue = "10") int limit) {
        List<Book> popularBooks = recommendationService.getPopularBooks(limit);
        RecommendationResponseDto data = new RecommendationResponseDto(RecommendationType.POPULAR, popularBooks, popularBooks.size());
        return OnBokResponse.success(data);
    }

    @Operation(summary = "평점 높은 도서 추천", description = "사용자 평점이 높은 고득점 도서 목록을 반환합니다.")
    @GetMapping("/highly-rated")
    public OnBokResponse<RecommendationResponseDto> getHighlyRatedBooks(@RequestParam(defaultValue = "10") int limit) {
        List<Book> highlyRatedBooks = recommendationService.getHighlyRatedBooks(limit);
        RecommendationResponseDto data = new RecommendationResponseDto(RecommendationType.HIGHLY_RATED, highlyRatedBooks, highlyRatedBooks.size());
        return OnBokResponse.success(data);
    }

    @Operation(summary = "개인화 복합 추천", description = "주문 이력, 리뷰, 협업 필터링 결과를 일정 비율로 혼합하여 사용자 맞춤형 결과를 제공합니다.")
    @GetMapping("/personalized")
    public OnBokResponse<PersonalizedRecommendationResponseDto> getPersonalizedRecommendations(@CurrentUser User user,
                                                                                               @RequestParam(defaultValue = "10") int limit) {
        int perAlgorithm = limit / 3;

        List<Book> orderBased = recommendationService.getRecommendationsByOrderHistory(user.getId(), perAlgorithm);
        List<Book> reviewBased = recommendationService.getRecommendationsByReviews(user.getId(), perAlgorithm);
        List<Book> collaborative = recommendationService.getCollaborativeFilteringRecommendations(user.getId(), perAlgorithm);

        PersonalizedRecommendationResponseDto data = new PersonalizedRecommendationResponseDto(RecommendationType.PERSONALIZED_MIXED, orderBased, reviewBased, collaborative);
        return OnBokResponse.success(data);
    }
}
