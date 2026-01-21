package com.onbok.book_hub.review.presentation.view;

import com.onbok.book_hub.common.annotation.CurrentUser;
import com.onbok.book_hub.review.application.ReviewQueryService;
import com.onbok.book_hub.review.dto.response.ReviewResponseDto;
import com.onbok.book_hub.user.domain.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/view/reviews")
@RequiredArgsConstructor
public class ReviewViewController {
    private final ReviewQueryService reviewQueryService;

    /**
     * 특정 도서의 리뷰 목록 조회 (페이징)
     */
    @GetMapping("/book/{bookId}")
    public String getReviewsByBook(@PathVariable Long bookId,
                                   @RequestParam(defaultValue = "1") int page,
                                   Model model) {
        Page<ReviewResponseDto> reviews = reviewQueryService.getReviewsByBook(bookId, page);
        Double avgRating = reviewQueryService.getAverageRating(bookId);
        long reviewCount = reviewQueryService.getReviewCount(bookId);

        model.addAttribute("bookId", bookId);
        model.addAttribute("reviews", reviews.getContent());
        model.addAttribute("totalPages", reviews.getTotalPages());
        model.addAttribute("currentPage", page);
        model.addAttribute("averageRating", avgRating);
        model.addAttribute("reviewCount", reviewCount);
        model.addAttribute("menu", "book");

        return "review/list";
    }

    /**
     * 내가 작성한 리뷰 목록
     */
    @GetMapping("/my-reviews")
    public String getMyReviews(@CurrentUser User user, Model model) {
        List<ReviewResponseDto> reviews = reviewQueryService.getReviewsByUser(user.getId());
        model.addAttribute("reviews", reviews);
        model.addAttribute("menu", "review");
        return "review/my-reviews";
    }
}
