package com.onbok.book_hub.book.application.service.bookEs;

import com.onbok.book_hub.book.domain.model.bookEs.BookEs;
import com.onbok.book_hub.book.domain.repository.bookEs.BookEsRepository;
import com.onbok.book_hub.book.dto.BookEsDto;
import com.onbok.book_hub.book.dto.response.BookEsListResponseDto;
import com.onbok.book_hub.common.pagination.PaginationInfo;
import com.onbok.book_hub.common.pagination.PaginationUtil;
import com.onbok.book_hub.common.exception.ErrorCode;
import com.onbok.book_hub.common.exception.ExpectedException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.data.elasticsearch.core.query.StringQuery;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BookEsService {
    public static final int PAGE_SIZE = 10;
    private final BookEsRepository bookEsRepository;
    private final ElasticsearchTemplate elasticsearchTemplate;

    // ========== Repository @Query 사용 (간단한 쿼리) ==========

    public BookEs findById(String bookId) {
        return bookEsRepository.findById(bookId).orElseThrow(() -> new ExpectedException(ErrorCode.BOOK_ES_NOT_FOUND));
    }

    public void insertBookEs(BookEs bookEs) {
        bookEsRepository.save(bookEs);
    }

    public void deleteBookEs(String bookId) {
        bookEsRepository.deleteById(bookId);
    }

    // 제목으로 검색 (간단한 케이스)
    public Page<BookEs> searchByTitle(String keyword, int page) {
        Pageable pageable = PageRequest.of(page - 1, PAGE_SIZE);
        return bookEsRepository.searchByTitle(keyword, pageable);
    }

    // 저자로 검색 (간단한 케이스)
    public Page<BookEs> searchByAuthor(String keyword, int page) {
        Pageable pageable = PageRequest.of(page - 1, PAGE_SIZE);
        return bookEsRepository.searchByAuthor(keyword, pageable);
    }

    // 출판사로 검색 (간단한 케이스)
    public Page<BookEs> searchByCompany(String keyword, int page) {
        Pageable pageable = PageRequest.of(page - 1, PAGE_SIZE);
        return bookEsRepository.searchByCompany(keyword, pageable);
    }

    // 복합 필드 검색 (제목 + 저자 + 요약, 가중치 적용)
    public Page<BookEs> searchByMultiField(String keyword, int page) {
        Pageable pageable = PageRequest.of(page - 1, PAGE_SIZE);
        return bookEsRepository.searchByMultiField(keyword, pageable);
    }

    // 키워드 + 가격 범위 검색
    public Page<BookEs> searchByKeywordAndPriceRange(String keyword, int minPrice, int maxPrice, int page) {
        Pageable pageable = PageRequest.of(page - 1, PAGE_SIZE);
        return bookEsRepository.searchByKeywordAndPriceRange(keyword, minPrice, maxPrice, pageable);
    }

    // ========== ElasticsearchTemplate 사용 (동적 쿼리) ==========

    // 동적 필드 기반 검색 (field 파라미터로 검색 대상 필드 선택 가능)
    public BookEsListResponseDto getPagedBooks(int page, String field, String keyword, String sortField, String sortDirection) {
        Pageable pageable = PageRequest.of(page - 1, PAGE_SIZE);
        // 정렬 필드에 keyword 서브 필드 사용
        String sortFieldToUse = sortField + ".keyword";
        Sort.Direction direction = sortDirection.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Query query = NativeQuery.builder()
                .withQuery(buildMatchQuery(field, keyword))
                .withSort(Sort.by(Sort.Order.desc("_score")))       // 1. matchScore 기준 정렬
                .withSort(Sort.by(direction, sortFieldToUse))     // 2. titel/author.keyword 기준 정렬
                .withTrackScores(true)
                .withPageable(pageable)
                .build();
        SearchHits<BookEs> searchHits = elasticsearchTemplate.search(query, BookEs.class);
        List<BookEsDto> bookEsDtoList = searchHits
                .getSearchHits()
                .stream()
                .map(hit -> new BookEsDto(hit.getContent(), hit.getScore()))
                .toList();

        long totalHits = searchHits.getTotalHits();
        Page<BookEsDto> pagedResult = new PageImpl<>(bookEsDtoList, pageable, totalHits);
        int totalPages = pagedResult.getTotalPages();

        PaginationInfo paginationInfo = PaginationUtil.calculatePagination(page, totalPages);

        return new BookEsListResponseDto(bookEsDtoList, paginationInfo);
    }

    private Query buildMatchQuery(String field, String keyword) {
        if (keyword.isEmpty()) {
            return new StringQuery("{\"match_all\": {}}");
        }
        String queryString = String.format("""
                        {
                            "match": {
                                "%s": {
                                    "query": "%s",
                                    "fuzziness": "AUTO"
                                }
                            }
                        }
                """,
                field, keyword
        );
        return new StringQuery(queryString);
    }

    // 자동완성 기능 - Prefix Query 사용
    public List<String> autocomplete(String field, String prefix) {
        if (prefix == null || prefix.trim().isEmpty()) {
            return List.of();
        }

        String queryString = String.format("""
                {
                    "prefix": {
                        "%s": {
                            "value": "%s"
                        }
                    }
                }
                """,
                field, prefix.trim()
        );

        Pageable pageable = PageRequest.of(0, 10);  // 최대 10개의 자동완성 결과
        Query query = NativeQuery.builder()
                .withQuery(new StringQuery(queryString))
                .withPageable(pageable)
                .build();

        SearchHits<BookEs> searchHits = elasticsearchTemplate.search(query, BookEs.class);

        // 중복 제거된 제목/저자 목록 반환
        return searchHits.getSearchHits()
                .stream()
                .map(hit -> {
                    BookEs book = hit.getContent();
                    if ("title".equals(field)) {
                        return book.getTitle();
                    } else if ("author".equals(field)) {
                        return book.getAuthor();
                    } else if ("company".equals(field)) {
                        return book.getCompany();
                    }
                    return book.getTitle();
                })
                .distinct()
                .toList();
    }

    // 자동완성 기능 - Match Phrase Prefix Query 사용 (더 유연한 자동완성)
    public List<String> autocompletePhrase(String field, String phrase) {
        if (phrase == null || phrase.trim().isEmpty()) {
            return List.of();
        }

        String queryString = String.format("""
                {
                    "match_phrase_prefix": {
                        "%s": {
                            "query": "%s",
                            "max_expansions": 10
                        }
                    }
                }
                """,
                field, phrase.trim()
        );

        Pageable pageable = PageRequest.of(0, 10);
        Query query = NativeQuery.builder()
                .withQuery(new StringQuery(queryString))
                .withPageable(pageable)
                .build();

        SearchHits<BookEs> searchHits = elasticsearchTemplate.search(query, BookEs.class);

        return searchHits.getSearchHits()
                .stream()
                .map(hit -> {
                    BookEs book = hit.getContent();
                    if ("title".equals(field)) {
                        return book.getTitle();
                    } else if ("author".equals(field)) {
                        return book.getAuthor();
                    } else if ("company".equals(field)) {
                        return book.getCompany();
                    }
                    return book.getTitle();
                })
                .distinct()
                .toList();
    }
}
