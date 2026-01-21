package com.onbok.book_hub.book.presentation.bookEs.api;

import com.onbok.book_hub.book.application.service.bookEs.BookEsService;
import com.onbok.book_hub.book.infrastructure.CsvFileReaderService;
import com.onbok.book_hub.common.response.OnBokResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "BookEs", description = "도서 일레스틱 서치 API")
@RestController
@RequestMapping("/api/bookEs")
@RequiredArgsConstructor
public class BookEsApiController {
    private final BookEsService bookEsService;
    private final CsvFileReaderService csvFileReaderService;

    @Operation(summary = "초기 데이터 적재", description = "CSV 파일을 읽어 일래스틱서치에 인덱싱합니다")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/yes24")
    public OnBokResponse<String> yes24() {
        csvFileReaderService.csvFileToElasticSearch();
        return OnBokResponse.success("일래스틱서치에 데이터를 저장했습니다.");
    }

    @Operation(summary = "자동완성 (단어)", description = "필드와 쿼리를 기반으로 단어 단위 자동완성 리스트를 반환합니다")
    @GetMapping("/autocomplete")
    public OnBokResponse<List<String>> autocomplete(@RequestParam(name="f", defaultValue = "title") String field,
                                                    @RequestParam(name="q") String query) {
        List<String> result = bookEsService.autocomplete(field, query);
        return OnBokResponse.success(result);
    }

    @Operation(summary = "자동완성 (구문)", description = "필드와 쿼리를 기반으로 구문 단위(Phrase) 자동완성 리스트를 반환합니다.")
    @GetMapping("/autocomplete-phrase")
    public OnBokResponse<List<String>> autocompletePhrase(@RequestParam(name="f", defaultValue = "title") String field,
                                           @RequestParam(name="q") String query) {
        List<String> result = bookEsService.autocompletePhrase(field, query);
        return OnBokResponse.success(result);
    }
}
