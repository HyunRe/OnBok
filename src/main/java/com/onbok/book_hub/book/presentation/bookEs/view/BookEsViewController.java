package com.onbok.book_hub.book.presentation.bookEs.view;

import com.onbok.book_hub.book.application.service.bookEs.BookEsService;
import com.onbok.book_hub.book.domain.model.bookEs.BookEs;
import com.onbok.book_hub.book.dto.response.BookEsListResponseDto;
import com.onbok.book_hub.book.infrastructure.CsvFileReaderService;
import com.onbok.book_hub.common.pagination.PaginationInfo;
import com.onbok.book_hub.common.annotation.CurrentUser;
import com.onbok.book_hub.user.domain.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/view/bookEs")
@RequiredArgsConstructor
public class BookEsViewController {
    private final BookEsService bookEsService;
    private final CsvFileReaderService csvFileReaderService;

    @GetMapping("/list")
    public String list(@CurrentUser User user,
                       @RequestParam(name="p", defaultValue = "1") int page,
                       @RequestParam(name="f", defaultValue = "title") String field,
                       @RequestParam(name="q", defaultValue = "") String query,
                       @RequestParam(name="sf", defaultValue = "title") String sortField,
                       @RequestParam(name="sd", defaultValue = "asc") String sortDirection,
                       Model model) {

        BookEsListResponseDto response = bookEsService.getPagedBooks(page, field, query, sortField, sortDirection);
        PaginationInfo paginationInfo = response.paginationInfo();

        model.addAttribute("menu", "elastic");
        model.addAttribute("currentBookEsPage", page);
        model.addAttribute("bookEsDtoList", response.bookEsList());
        model.addAttribute("field", field);
        model.addAttribute("query", query);
        model.addAttribute("totalPages", paginationInfo.totalPages());
        model.addAttribute("startPage", paginationInfo.startPage());
        model.addAttribute("endPage", paginationInfo.endPage());
        model.addAttribute("pageList", paginationInfo.pageList());
        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDirection", sortDirection);
        return "bookEs/list";
    }

    @GetMapping("/detail/{bookId}")
    public String detail(@PathVariable String bookId,
                         @RequestParam(name="q", defaultValue = "") String query,
                         Model model) {
        BookEs bookEs = bookEsService.findById(bookId);
        if (!query.isEmpty()) {
            String highlightedSummary = bookEs.getSummary()
                    .replaceAll(query, "<span style='background-color: skyblue;'>" + query + "</span>");
            bookEs.updateSummary(highlightedSummary);
        }
        model.addAttribute("bookEs", bookEs);
        return "bookEs/detail";
    }

    @GetMapping("/insert")
    public String insertForm() {
        return "bookEs/insert";
    }

    @PostMapping("/insert")
    public String insertProc(BookEs b) {
        BookEs bookEs = BookEs.builder()
                .title(b.getTitle()).author(b.getAuthor()).company(b.getCompany())
                .price(b.getPrice()).imageUrl(b.getImageUrl()).summary(b.getSummary())
                .build();
        bookEsService.insertBookEs(bookEs);
        return "redirect:/bookEs/list";
    }

    @GetMapping("/delete/{bookId}")
    public String delete(@PathVariable String bookId) {
        bookEsService.deleteBookEs(bookId);
        return "redirect:/bookEs/list";
    }
}
