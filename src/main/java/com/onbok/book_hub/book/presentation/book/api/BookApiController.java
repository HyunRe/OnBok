package com.onbok.book_hub.book.presentation.book.api;

import com.onbok.book_hub.book.application.service.search.SearchKeywordService;
import com.onbok.book_hub.book.domain.model.search.SearchKeyword;
import com.onbok.book_hub.cart.application.CartService;
import com.onbok.book_hub.cart.dto.request.CartAddRequestDto;
import com.onbok.book_hub.cart.dto.response.CartAddResponseDto;
import com.onbok.book_hub.common.annotation.CurrentUser;
import com.onbok.book_hub.common.response.OnBokResponse;
import com.onbok.book_hub.user.domain.model.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Book", description = "도서 API")
@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookApiController {
    private final CartService cartService;
    private final SearchKeywordService searchKeywordService;

    @Operation(summary = "장바구니에 담기", description = "도서를 장바구니에 담습니다")
    @PostMapping("/cart")
    public OnBokResponse<CartAddResponseDto> addToCart(@CurrentUser User user,
                                                       @Valid @RequestBody CartAddRequestDto cartAddRequestDto) {
        cartService.addToCart(user.getId(), cartAddRequestDto.getId(), cartAddRequestDto.getQuantity());
        int currentCount = cartService.getCartItemsByUser(user.getId()).size();
        CartAddResponseDto data = new CartAddResponseDto(currentCount, "장바구니에 담겼습니다.");
        return OnBokResponse.success(data, HttpStatus.CREATED);
    }

    @Operation(summary = "인기 검색어 조회", description = "검색 횟수 기준 상위 10개의 인기 검색어를 조회합니다")
    @GetMapping("/popular-keywords")
    public OnBokResponse<List<SearchKeyword>> getPopularKeywords() {
        List<SearchKeyword> keywords = searchKeywordService.getPopularKeywords();
        return OnBokResponse.success(keywords);
    }
}
