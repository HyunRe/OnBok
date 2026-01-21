package com.onbok.book_hub.book.presentation.book.view;

import com.onbok.book_hub.book.application.service.book.BookCommandService;
import com.onbok.book_hub.book.application.service.book.BookQueryService;
import com.onbok.book_hub.book.application.service.book.BookViewService;
import com.onbok.book_hub.book.domain.model.book.Book;
import com.onbok.book_hub.book.dto.response.BookListResponseDto;
import com.onbok.book_hub.book.infrastructure.CsvFileReaderService;
import com.onbok.book_hub.common.pagination.PaginationInfo;
import com.onbok.book_hub.cart.application.CartService;
import com.onbok.book_hub.cart.domain.model.Cart;
import com.onbok.book_hub.cart.dto.request.CartAddRequestDto;
import com.onbok.book_hub.common.annotation.CurrentUser;
import com.onbok.book_hub.review.application.ReviewQueryService;
import com.onbok.book_hub.review.dto.response.ReviewResponseDto;
import com.onbok.book_hub.user.domain.model.User;
import org.springframework.data.domain.Page;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/view/books")
@RequiredArgsConstructor
public class BookViewController {
    private final CsvFileReaderService csvFileReaderService;
    private final BookCommandService bookCommandService;
    private final BookQueryService bookQueryService;
    private final BookViewService bookViewService;
    private final CartService cartService;
    private final ReviewQueryService reviewQueryService;

    @GetMapping("/list")
    public String list(@CurrentUser User user,
                       @RequestParam(name="p", defaultValue = "1") int page,
                       @RequestParam(name="f", defaultValue = "title") String field,
                       @RequestParam(name="q", defaultValue = "") String query,
                       Model model) {
        int cartCount = 0;
        if (user != null) {
            List<Cart> cartList = cartService.getCartItemsByUser(user.getId());
            cartCount = cartList.size();
        }

        BookListResponseDto response = bookQueryService.getBookListWithPagination(page, field, query);
        PaginationInfo paginationInfo = response.getPaginationInfo();

        model.addAttribute("menu", "book");
        model.addAttribute("currentBookPage", page);
        model.addAttribute("bookList", response.getBooks());
        model.addAttribute("field", field);
        model.addAttribute("query", query);
        model.addAttribute("totalPages", paginationInfo.totalPages());
        model.addAttribute("startPage", paginationInfo.startPage());
        model.addAttribute("endPage", paginationInfo.endPage());
        model.addAttribute("pageList", paginationInfo.pageList());
        model.addAttribute("cartCount", cartCount);

        return "mall/list";
    }

    @GetMapping("/detail/{id}")
    public String detail(@PathVariable Long id,
                         @RequestParam(name="q", defaultValue = "") String query,
                         @RequestParam(name="reviewPage", defaultValue = "1") int reviewPage,
                         Model model) {
        Book book = bookQueryService.findById(id);
        book = bookViewService.highlightSummary(book, query);

        // 리뷰 데이터 조회
        Page<ReviewResponseDto> reviews = reviewQueryService.getReviewsByBook(id, reviewPage);
        Double avgRating = reviewQueryService.getAverageRating(id);
        long reviewCount = reviewQueryService.getReviewCount(id);

        model.addAttribute("book", book);
        model.addAttribute("reviews", reviews.getContent());
        model.addAttribute("reviewTotalPages", reviews.getTotalPages());
        model.addAttribute("reviewCurrentPage", reviewPage);
        model.addAttribute("averageRating", avgRating != null ? avgRating : 0.0);
        model.addAttribute("reviewCount", reviewCount);
        model.addAttribute("menu", "book");

        return "mall/detail";
    }

    @GetMapping("/insert")
    public String insertForm() {
        return "book/insert";
    }

    @PostMapping("/insert")
    public String insertProc(Book book) {
        bookCommandService.insertBook(book);
        return "redirect:/view/books/list";
    }

    @PostMapping("/detail")
    public String addItemToCart(@CurrentUser User user,
                                @RequestParam("id") Long bookId,
                                @RequestParam("quantity") int quantity,
                                Model model) {
        try {
            if (quantity > 0) {
                cartService.addToCart(user.getId(), bookId, quantity);
                model.addAttribute("msg", "장바구니에 담겼습니다.");
                model.addAttribute("url", "/view/books/list");
            } else {
                model.addAttribute("msg", "수량을 선택해주세요.");
                model.addAttribute("url", "/view/books/detail/" + bookId);
            }
        } catch (Exception e) {
            model.addAttribute("msg", "장바구니 담기에 실패했습니다: " + e.getMessage());
            model.addAttribute("url", "/view/books/detail/" + bookId);
        }
        return "common/alertMsg";
    }

    // 초기 데이터
    @GetMapping("/yes24")
    public String yes24() {
        csvFileReaderService.csvFileToDB();
        return "redirect:/view/books/list";
    }
}
