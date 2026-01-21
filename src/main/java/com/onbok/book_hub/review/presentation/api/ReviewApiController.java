package com.onbok.book_hub.review.presentation.api;

import com.onbok.book_hub.common.annotation.CurrentUser;
import com.onbok.book_hub.common.response.OnBokResponse;
import com.onbok.book_hub.review.application.ReviewCommandService;
import com.onbok.book_hub.review.dto.request.ReviewCreateRequestDto;
import com.onbok.book_hub.review.dto.request.ReviewUpdateRequestDto;
import com.onbok.book_hub.user.domain.model.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Review", description = "리뷰 API")
@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewApiController {
    private final ReviewCommandService reviewCommandService;

    @Operation(summary = "리뷰 작성", description = "도서 ID와 평점, 내용을 입력하여 새로운 리뷰를 등록합니다")
    @PostMapping("/create")
    public OnBokResponse<String> createReview(@CurrentUser User user,
                                              @Valid @RequestBody ReviewCreateRequestDto reviewCreateRequestDto) {
        reviewCommandService.createReview(reviewCreateRequestDto.getBookId(), user.getId(), reviewCreateRequestDto.getRating(), reviewCreateRequestDto.getContent());
        return OnBokResponse.success("리뷰가 등록되었습니다.", HttpStatus.CREATED);
    }

    @Operation(summary = "리뷰 수정", description = "기존에 작성한 리뷰의 평점과 내용을 수정합니다")
    @PostMapping("/update")
    public OnBokResponse<String> updateReview(@CurrentUser User user,
                                              @Valid @RequestBody ReviewUpdateRequestDto reviewUpdateRequestDto) {
        reviewCommandService.updateReview(user, reviewUpdateRequestDto.getId(), reviewUpdateRequestDto.getRating(), reviewUpdateRequestDto.getContent());
        return OnBokResponse.success("리뷰가 수정되었습니다.");
    }

    @Operation(summary = "리뷰 삭제", description = "리뷰 ID를 통해 등록된 리뷰를 삭제합니다")
    @PostMapping("/delete")
    public OnBokResponse<String> deleteReview(@CurrentUser User user,
                                              @RequestParam Long id) {
        reviewCommandService.deleteReview(user, id);
        return OnBokResponse.success("리뷰가 삭제되었습니다.");
    }
}
