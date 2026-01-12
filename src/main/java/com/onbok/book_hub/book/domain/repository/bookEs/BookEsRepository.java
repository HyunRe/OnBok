package com.onbok.book_hub.book.domain.repository.bookEs;

import com.onbok.book_hub.book.domain.model.bookEs.BookEs;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface BookEsRepository extends ElasticsearchRepository<BookEs, String> {
    Page<BookEs> findAll(Pageable pageable);

    // 가격 범위 필터링
    Page<BookEs> findByPriceBetween(int minPrice, int maxPrice, Pageable pageable);

    // @Query 어노테이션 (명시적 쿼리 - Nori 분석기 활용)
    /**
     * 제목으로 검색 (Nori + Fuzziness)
     */
    @Query("""
    {
      "match": {
        "title": {
          "query": "?0",
          "fuzziness": "AUTO"
        }
      }
    }
    """)
    Page<BookEs> searchByTitle(String keyword, Pageable pageable);

    /**
     * 저자로 검색 (Nori + Fuzziness)
     */
    @Query("""
    {
      "match": {
        "author": {
          "query": "?0",
          "fuzziness": "AUTO"
        }
      }
    }
    """)
    Page<BookEs> searchByAuthor(String keyword, Pageable pageable);

    /**
     * 출판사로 검색
     */
    @Query("""
    {
      "match": {
        "company": {
          "query": "?0",
          "fuzziness": "AUTO"
        }
      }
    }
    """)
    Page<BookEs> searchByCompany(String keyword, Pageable pageable);

    /**
     * 제목 + 저자 + 요약 복합 검색 (가중치 적용)
     * title^2: 제목에 2배 가중치
     * author^1.5: 저자에 1.5배 가중치
     * summary^1: 요약에 1배 가중치
     */
    @Query("""
    {
      "multi_match": {
        "query": "?0",
        "fields": ["title^2", "author^1.5", "summary"],
        "fuzziness": "AUTO",
        "type": "best_fields"
      }
    }
    """)
    Page<BookEs> searchByMultiField(String keyword, Pageable pageable);

    /**
     * 가격 범위 + 키워드 복합 검색
     */
    @Query("""
    {
      "bool": {
        "must": {
          "multi_match": {
            "query": "?0",
            "fields": ["title^2", "author"]
          }
        },
        "filter": {
          "range": {
            "price": {
              "gte": ?1,
              "lte": ?2
            }
          }
        }
      }
    }
    """)
    Page<BookEs> searchByKeywordAndPriceRange(String keyword, int minPrice, int maxPrice, Pageable pageable);
}
