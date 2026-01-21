package com.onbok.book_hub.recommendation.presentation.view;

import com.onbok.book_hub.book.domain.model.book.Book;
import com.onbok.book_hub.common.annotation.CurrentUser;
import com.onbok.book_hub.recommendation.application.RecommendationService;
import com.onbok.book_hub.user.domain.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/view/recommendations")
@RequiredArgsConstructor
public class RecommendationViewController {
    private final RecommendationService recommendationService;

    /**
     * 추천 메인 페이지
     */
    @GetMapping
    public String recommendations(@CurrentUser User user, Model model) {
        // 여러 추천 알고리즘의 결과를 모두 가져옴
        List<Book> orderBased = recommendationService.getRecommendationsByOrderHistory(user.getId(), 5);
        List<Book> reviewBased = recommendationService.getRecommendationsByReviews(user.getId(), 5);
        List<Book> collaborative = recommendationService.getCollaborativeFilteringRecommendations(user.getId(), 5);
        List<Book> popular = recommendationService.getPopularBooks(10);
        List<Book> highlyRated = recommendationService.getHighlyRatedBooks(10);

        model.addAttribute("orderBased", orderBased);
        model.addAttribute("reviewBased", reviewBased);
        model.addAttribute("collaborative", collaborative);
        model.addAttribute("popular", popular);
        model.addAttribute("highlyRated", highlyRated);
        model.addAttribute("menu", "recommendation");

        return "recommendation/main";
    }

    /**
     * 개인화 추천 페이지
     */
    @GetMapping("/personalized")
    public String personalized(@CurrentUser User user,
                               @RequestParam(defaultValue = "20") int limit,
                               Model model) {
        int perAlgorithm = limit / 3;

        List<Book> orderBased = recommendationService.getRecommendationsByOrderHistory(user.getId(), perAlgorithm);
        List<Book> reviewBased = recommendationService.getRecommendationsByReviews(user.getId(), perAlgorithm);
        List<Book> collaborative = recommendationService.getCollaborativeFilteringRecommendations(user.getId(), perAlgorithm);

        model.addAttribute("orderBased", orderBased);
        model.addAttribute("reviewBased", reviewBased);
        model.addAttribute("collaborative", collaborative);
        model.addAttribute("menu", "recommendation");

        return "recommendation/personalized";
    }

    /**
     * 인기 도서 페이지
     */
    @GetMapping("/popular")
    public String popular(@RequestParam(defaultValue = "20") int limit, Model model) {
        List<Book> popularBooks = recommendationService.getPopularBooks(limit);
        model.addAttribute("books", popularBooks);
        model.addAttribute("title", "인기 도서");
        model.addAttribute("menu", "recommendation");
        return "recommendation/list";
    }

    /**
     * 평점 높은 도서 페이지
     */
    @GetMapping("/highly-rated")
    public String highlyRated(@RequestParam(defaultValue = "20") int limit, Model model) {
        List<Book> highlyRatedBooks = recommendationService.getHighlyRatedBooks(limit);
        model.addAttribute("books", highlyRatedBooks);
        model.addAttribute("title", "평점 높은 도서");
        model.addAttribute("menu", "recommendation");
        return "recommendation/list";
    }
}
