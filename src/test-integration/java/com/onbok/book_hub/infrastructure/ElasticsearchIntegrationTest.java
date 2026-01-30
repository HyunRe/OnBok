package com.onbok.book_hub.elasticsearch;

import com.onbok.book_hub.book.application.service.bookEs.BookEsService;
import com.onbok.book_hub.book.domain.model.bookEs.BookEs;
import com.onbok.book_hub.book.domain.repository.bookEs.BookEsRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest(properties = {
        "spring.data.elasticsearch.repositories.enabled=true"
})
@EnabledIf("isDockerAvailable")
@DisplayName("ElasticSearch 통합 테스트 (Docker 필요)")
class ElasticsearchIntegrationTest {

    static boolean isDockerAvailable() {
        try {
            DockerClientFactory.instance().client();
            return true;
        } catch (Exception e) {
            System.out.println("⚠️  Docker를 사용할 수 없어 ElasticSearch 테스트를 스킵합니다.");
            System.out.println("   Docker Desktop을 실행하고 다시 시도하세요.");
            return false;
        }
    }

    @Container
    static ElasticsearchContainer elasticsearchContainer = new ElasticsearchContainer(
            "docker.elastic.co/elasticsearch/elasticsearch:8.11.0"
    ).withEnv("xpack.security.enabled", "false")
     .withEnv("discovery.type", "single-node");

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.elasticsearch.uris", elasticsearchContainer::getHttpHostAddress);
        // Test에서 ElasticSearch autoconfigure exclusion을 override
        registry.add("spring.autoconfigure.exclude", () -> "");
    }

    @Autowired
    private BookEsService bookEsService;

    @Autowired
    private BookEsRepository bookEsRepository;

    @BeforeEach
    void setUp() {
        // 테스트 전 기존 데이터 삭제
        bookEsRepository.deleteAll();
    }

    @Test
    @DisplayName("ElasticSearch 키워드 검색 - 제목 기반")
    void searchByTitle_success() {
        // given
        BookEs book1 = createBook("1", "스프링 부트 완벽 가이드", "저자A", "출판사A", 35000);
        BookEs book2 = createBook("2", "스프링 인 액션", "저자B", "출판사B", 40000);
        BookEs book3 = createBook("3", "자바의 정석", "저자C", "출판사C", 30000);

        bookEsService.insertBookEs(book1);
        bookEsService.insertBookEs(book2);
        bookEsService.insertBookEs(book3);

        // 인덱싱 대기 (테스트 환경에서 필요)
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // when
        Page<BookEs> results = bookEsService.searchByTitle("스프링", 1);

        // then
        assertThat(results.getContent()).hasSize(2);
        assertThat(results.getContent())
                .extracting("title")
                .contains("스프링 부트 완벽 가이드", "스프링 인 액션");
    }

    @Test
    @DisplayName("ElasticSearch 키워드 검색 - 저자 기반")
    void searchByAuthor_success() {
        // given
        BookEs book1 = createBook("1", "책1", "김철수", "출판사A", 25000);
        BookEs book2 = createBook("2", "책2", "김영희", "출판사B", 30000);
        BookEs book3 = createBook("3", "책3", "이철수", "출판사C", 28000);

        bookEsService.insertBookEs(book1);
        bookEsService.insertBookEs(book2);
        bookEsService.insertBookEs(book3);

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // when
        Page<BookEs> results = bookEsService.searchByAuthor("철수", 1);

        // then
        assertThat(results.getContent()).hasSize(2);
        assertThat(results.getContent())
                .extracting("author")
                .contains("김철수", "이철수");
    }

    @Test
    @DisplayName("ElasticSearch 키워드 검색 - 출판사 기반")
    void searchByCompany_success() {
        // given
        BookEs book1 = createBook("1", "책1", "저자A", "한빛미디어", 30000);
        BookEs book2 = createBook("2", "책2", "저자B", "한빛아카데미", 35000);
        BookEs book3 = createBook("3", "책3", "저자C", "위키북스", 28000);

        bookEsService.insertBookEs(book1);
        bookEsService.insertBookEs(book2);
        bookEsService.insertBookEs(book3);

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // when
        Page<BookEs> results = bookEsService.searchByCompany("한빛", 1);

        // then
        assertThat(results.getContent()).hasSize(2);
        assertThat(results.getContent())
                .extracting("company")
                .contains("한빛미디어", "한빛아카데미");
    }

    @Test
    @DisplayName("ElasticSearch 복합 필드 검색 - 제목 + 저자 + 요약")
    void searchByMultiField_success() {
        // given
        BookEs book1 = createBook("1", "자바 프로그래밍", "홍길동", "출판사A", 30000);
        book1.updateSummary("자바 기초부터 고급까지 다루는 책");

        BookEs book2 = createBook("2", "파이썬 입문", "김철수", "출판사B", 25000);
        book2.updateSummary("파이썬 초보자를 위한 입문서");

        bookEsService.insertBookEs(book1);
        bookEsService.insertBookEs(book2);

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // when
        Page<BookEs> results = bookEsService.searchByMultiField("자바", 1);

        // then
        assertThat(results.getContent()).isNotEmpty();
        assertThat(results.getContent().get(0).getTitle()).contains("자바");
    }

    @Test
    @DisplayName("ElasticSearch 가격 범위 검색")
    void searchByKeywordAndPriceRange_success() {
        // given
        BookEs book1 = createBook("1", "저렴한 책", "저자A", "출판사A", 15000);
        BookEs book2 = createBook("2", "중간 가격 책", "저자B", "출판사B", 25000);
        BookEs book3 = createBook("3", "비싼 책", "저자C", "출판사C", 50000);

        bookEsService.insertBookEs(book1);
        bookEsService.insertBookEs(book2);
        bookEsService.insertBookEs(book3);

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // when
        Page<BookEs> results = bookEsService.searchByKeywordAndPriceRange("책", 20000, 40000, 1);

        // then
        assertThat(results.getContent()).hasSize(1);
        assertThat(results.getContent().get(0).getPrice()).isEqualTo(25000);
    }

    @Test
    @DisplayName("ElasticSearch 삭제 - 도서 삭제")
    void deleteBook_success() {
        // given
        BookEs book = createBook("100", "삭제할 책", "저자", "출판사", 20000);
        bookEsService.insertBookEs(book);

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // when
        bookEsService.deleteBookEs("100");

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // then
        assertThat(bookEsRepository.findById("100")).isEmpty();
    }

    @Test
    @DisplayName("ElasticSearch 검색 결과 정확성 - 정확한 키워드 매칭")
    void searchAccuracy_exactMatch() {
        // given
        BookEs book1 = createBook("1", "스프링 부트", "저자A", "출판사A", 30000);
        BookEs book2 = createBook("2", "리액트 네이티브", "저자B", "출판사B", 35000);

        bookEsService.insertBookEs(book1);
        bookEsService.insertBookEs(book2);

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // when
        Page<BookEs> springResults = bookEsService.searchByTitle("스프링", 1);
        Page<BookEs> reactResults = bookEsService.searchByTitle("리액트", 1);

        // then
        assertThat(springResults.getContent()).hasSize(1);
        assertThat(springResults.getContent().get(0).getTitle()).isEqualTo("스프링 부트");

        assertThat(reactResults.getContent()).hasSize(1);
        assertThat(reactResults.getContent().get(0).getTitle()).isEqualTo("리액트 네이티브");
    }

    // === Helper Methods ===

    private BookEs createBook(String id, String title, String author, String company, int price) {
        return BookEs.builder()
                .bookId(id)
                .title(title)
                .author(author)
                .company(company)
                .price(price)
                .summary("테스트 요약")
                .build();
    }
}
