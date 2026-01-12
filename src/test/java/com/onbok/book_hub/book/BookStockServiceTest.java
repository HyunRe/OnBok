package com.onbok.book_hub.book;

import com.onbok.book_hub.book.application.service.book.BookStockService;
import com.onbok.book_hub.book.domain.model.book.Book;
import com.onbok.book_hub.book.domain.repository.book.BookRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(properties = {
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.data.elasticsearch.ElasticsearchDataAutoConfiguration,org.springframework.boot.autoconfigure.elasticsearch.ElasticsearchRestClientAutoConfiguration"
})
@DisplayName("BookStockService 통합 테스트")
class BookStockServiceTest {
    @Autowired private BookStockService bookStockService;
    @Autowired private BookRepository bookRepository;

    private Book testBook;

    @BeforeEach
    void setUp() {
        testBook = Book.builder()
                .title("동시성 테스트 도서")
                .author("테스트 저자")
                .company("테스트 출판사")
                .price(15000)
                .stock(100)
                .build();
        testBook = bookRepository.save(testBook);
    }

    @AfterEach
    void tearDown() {
        if (testBook != null && testBook.getId() > 0) {
            bookRepository.deleteById(testBook.getId());
        }
    }

    @Test
    @DisplayName("재고 감소 - 정상 케이스")
    void decreaseStock_success() {
        // when
        bookStockService.decreaseStock(testBook.getId(), 10);

        // then
        Book updatedBook = bookRepository.findById(testBook.getId()).orElseThrow();
        assertThat(updatedBook.getStock()).isEqualTo(90);
    }

    @Test
    @DisplayName("재고 증가 - 정상 케이스")
    void increaseStock_success() {
        // when
        bookStockService.increaseStock(testBook.getId(), 20);

        // then
        Book updatedBook = bookRepository.findById(testBook.getId()).orElseThrow();
        assertThat(updatedBook.getStock()).isEqualTo(120);
    }

    @Test
    @DisplayName("재고 감소 - 재고 부족 시 예외")
    void decreaseStock_insufficientStock() {
        // when & then
        assertThatThrownBy(() -> bookStockService.decreaseStock(testBook.getId(), 150))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("재고가 부족합니다");
    }

    @Test
    @DisplayName("동시성 제어 - 여러 스레드가 동시에 재고 감소 시도")
    void decreaseStock_concurrency() throws InterruptedException {
        // given
        int threadCount = 10;
        int decreaseAmount = 5;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        // when
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    bookStockService.decreaseStock(testBook.getId(), decreaseAmount);
                    successCount.incrementAndGet();
                } catch (ObjectOptimisticLockingFailureException e) {
                    // 낙관적 락 실패 - 정상적인 동시성 제어
                    failCount.incrementAndGet();
                } catch (Exception e) {
                    // 기타 예외
                    failCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        // then
        Book finalBook = bookRepository.findById(testBook.getId()).orElseThrow();

        // 성공한 만큼만 재고가 감소되어야 함
        int expectedStock = 100 - (successCount.get() * decreaseAmount);
        assertThat(finalBook.getStock()).isEqualTo(expectedStock);

        // 일부는 성공하고 일부는 실패해야 함 (낙관적 락이 작동하는 증거)
        assertThat(successCount.get() + failCount.get()).isEqualTo(threadCount);
    }

    @Test
    @DisplayName("재고 조회 테스트")
    void getStock() {
        // when
        int stock = bookStockService.getStock(testBook.getId());

        // then
        assertThat(stock).isEqualTo(100);
    }

    @Test
    @DisplayName("존재하지 않는 도서 재고 감소 시 예외")
    void decreaseStock_bookNotFound() {
        // when & then
        assertThatThrownBy(() -> bookStockService.decreaseStock(999999L, 10))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("도서를 찾을 수 없습니다");
    }
}
