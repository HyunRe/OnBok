# 테스트 가이드

## 📋 목차
- [디렉토리 구조](#디렉토리-구조)
- [테스트 실행](#테스트-실행)
- [테스트 종류](#테스트-종류)
- [작성 가이드](#작성-가이드)
- [ElasticSearch 테스트](#elasticsearch-테스트)
- [성능 및 FAQ](#성능-및-faq)

---

## 디렉토리 구조

```
src/
├── test/
│   └── resources/
│       └── application.yaml      # 공통 테스트 설정
│
├── test-unit/                    # 단위 테스트 (빠름, ~13초)
│   └── java/com/onbok/book_hub/
│       ├── service/              # Service 계층 (Mock 기반)
│       │   ├── RecommendationServiceTest.java
│       │   ├── CartCalculationServiceTest.java
│       │   ├── BookEsServiceTest.java
│       │   └── OAuth2UserServiceTest.java
│       │
│       ├── domain/               # Domain 계층 (순수 Java)
│       │   ├── BookStockTest.java
│       │   ├── CartTest.java
│       │   ├── DeliveryAddressTest.java
│       │   └── TossPaymentTest.java
│       │
│       └── util/                 # Utility (추후 추가)
│
└── test-integration/             # 통합 테스트 (느림, ~20초)
    └── java/com/onbok/book_hub/
        ├── api/                  # API 계층 (추후 추가)
        │   └── (Controller + MockMvc)
        │
        ├── repository/           # Repository 계층 (@DataJpaTest)
        │   ├── CartRepositoryTest.java
        │   ├── OrderRepositoryTest.java
        │   ├── ReviewRepositoryTest.java
        │   ├── DeliveryAddressRepositoryTest.java
        │   └── ImageRepositoryTest.java
        │
        └── infrastructure/       # Infrastructure 계층
            ├── ElasticsearchIntegrationTest.java
            ├── ImageLocalUploadTest.java
            ├── ImageS3ServiceTest.java
            ├── OrderIntegrationTest.java
            ├── ReviewIntegrationTest.java
            ├── DeliveryAddressIntegrationTest.java
            └── TransactionStabilityTest.java
```

---

## 테스트 실행

### 기본 명령어

```bash
# 단위 테스트만 (빠름)
./gradlew unitTest

# 통합 테스트만
./gradlew integrationTest

# 전체 테스트
./gradlew test

# 빌드 (테스트 포함)
./gradlew build

# 테스트 없이 빌드
./gradlew build -x test
```

### 특정 계층만 실행

```bash
# Service 계층 단위 테스트만
./gradlew test --tests "*service.*Test"

# Repository 계층 통합 테스트만
./gradlew test --tests "*repository.*Test"

# Infrastructure 계층만
./gradlew test --tests "*infrastructure.*Test"
```

---

## 테스트 종류

### 1. 단위 테스트 (Unit Test)

#### service/ - Service 계층
**특징**:
- Mock/Stub을 활용한 격리된 테스트
- 비즈니스 로직 검증

**어노테이션**: `@ExtendWith(MockitoExtension.class)`, `@Mock`, `@InjectMocks`

**예시**:
```java
@ExtendWith(MockitoExtension.class)
@DisplayName("추천 서비스 단위 테스트")
class RecommendationServiceTest {
    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private RecommendationService recommendationService;

    @Test
    @DisplayName("주문 기반 추천 - 이미 구매한 상품 제외")
    void getRecommendations_excludesPurchased() {
        // given
        when(orderRepository.findByUserId(1L)).thenReturn(orders);

        // when
        List<Book> result = recommendationService.getRecommendations(1L);

        // then
        assertThat(result).doesNotContain(purchasedBook);
    }
}
```

#### domain/ - Domain 계층
**특징**:
- 순수 Java 객체 테스트 (Spring 없음)
- 엔티티 비즈니스 로직 검증

**어노테이션**: `@Test`만 사용

**예시**:
```java
@DisplayName("재고 관리 단위 테스트")
class BookStockTest {
    @Test
    @DisplayName("재고 감소 - 정상")
    void decreaseStock_success() {
        // given
        Book book = Book.builder().stock(10).build();

        // when
        book.decreaseStock(3);

        // then
        assertThat(book.getStock()).isEqualTo(7);
    }

    @Test
    @DisplayName("재고 감소 실패 - 재고 부족")
    void decreaseStock_fail() {
        // given
        Book book = Book.builder().stock(2).build();

        // when & then
        assertThatThrownBy(() -> book.decreaseStock(5))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("재고가 부족합니다");
    }
}
```

---

### 2. 통합 테스트 (Integration Test)

#### repository/ - Repository 계층
**특징**:
- JPA Repository 쿼리 메서드 테스트
- 실제 DB 연동 (H2)

**어노테이션**: `@DataJpaTest`

**예시**:
```java
@DataJpaTest
@DisplayName("장바구니 Repository 테스트")
class CartRepositoryTest {
    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("사용자별 장바구니 조회")
    void findByUserId_success() {
        // given
        User user = entityManager.persist(User.builder()...);
        Cart cart = entityManager.persist(Cart.builder()...);

        // when
        List<Cart> carts = cartRepository.findByUserId(user.getId());

        // then
        assertThat(carts).hasSize(1);
    }
}
```

#### infrastructure/ - Infrastructure 계층
**특징**:
- 외부 시스템 연동 테스트
- 전체 플로우 검증

**어노테이션**: `@SpringBootTest`, `@Transactional`

**예시**:
```java
@SpringBootTest
@Transactional
@DisplayName("주문 통합 테스트")
class OrderIntegrationTest {
    @Autowired
    private OrderService orderService;

    @Autowired
    private BookRepository bookRepository;

    @Test
    @DisplayName("주문 생성 - 재고 감소 확인")
    void createOrder_decreasesStock() {
        // given
        Book book = bookRepository.save(
            Book.builder().stock(10).build()
        );

        // when
        orderService.createOrder(userId, bookId, 3);

        // then
        Book updated = bookRepository.findById(bookId).get();
        assertThat(updated.getStock()).isEqualTo(7);
    }
}
```

---

## 작성 가이드

### 단위 테스트 체크리스트

- [ ] 파일 위치: `src/test-unit/java/com/onbok/book_hub/service` 또는 `domain`
- [ ] 외부 의존성 최소화 (Mock 활용)
- [ ] `@DataJpaTest` 또는 `@ExtendWith(MockitoExtension.class)` 사용
- [ ] 테스트 메서드명: `메서드명_조건_예상결과` 형식
- [ ] `@DisplayName`으로 한글 설명 추가

### 통합 테스트 체크리스트

- [ ] 파일 위치: `src/test-integration/java/com/onbok/book_hub/repository` 또는 `infrastructure`
- [ ] `@SpringBootTest` 또는 `@DataJpaTest` 사용
- [ ] `@Transactional` 추가 (자동 롤백)
- [ ] 여러 계층이 통합된 시나리오 검증
- [ ] `@DisplayName`으로 한글 설명 추가

### 패키지 선언 주의사항

⚠️ **중요**: 테스트 파일이 `test-unit/service/` 디렉토리에 있어도, 패키지는 원래 클래스의 패키지를 따릅니다.

```java
// ❌ 잘못된 예
// 파일: src/test-unit/java/com/onbok/book_hub/service/RecommendationServiceTest.java
package com.onbok.book_hub.service;  // 틀림!

// ✅ 올바른 예
// 파일: src/test-unit/java/com/onbok/book_hub/service/RecommendationServiceTest.java
package com.onbok.book_hub.recommendation.application;  // 원래 패키지 유지
```

---

## ElasticSearch 테스트

### 설정

ElasticSearch 테스트는 **Docker + Testcontainers**를 사용합니다.

#### 1. Docker 없을 때
테스트가 자동으로 스킵됩니다:
```
⚠️  Docker를 사용할 수 없어 ElasticSearch 테스트를 스킵합니다.
   Docker Desktop을 실행하고 다시 시도하세요.
```

#### 2. Docker 실행 중
Testcontainers가 자동으로:
- ElasticSearch 컨테이너 시작
- 테스트 실행
- 컨테이너 종료

### 수동으로 활성화/비활성화

```java
// 비활성화
@Disabled("ElasticSearch 서버 필요")
class ElasticsearchIntegrationTest { ... }

// 활성화 (Docker 필요)
@EnabledIf("isDockerAvailable")
class ElasticsearchIntegrationTest { ... }
```

### 로컬 ElasticSearch 사용

Docker 대신 로컬 ElasticSearch를 사용하려면:

```bash
# ElasticSearch 실행
docker run -d \
  --name elasticsearch \
  -p 9200:9200 \
  -e "discovery.type=single-node" \
  -e "xpack.security.enabled=false" \
  docker.elastic.co/elasticsearch/elasticsearch:8.11.0
```

`src/test/resources/application.yaml` 수정:
```yaml
spring:
  elasticsearch:
    uris: http://localhost:9200
```

---

## 성능 및 FAQ

### 성능 비교

| 테스트 종류 | 실행 시간 | 파일 수 | Context 로드 |
|------------|----------|---------|--------------|
| 단위 테스트 | ~13초 | 8개 | ❌ 없음 |
| 통합 테스트 | ~20초 | 12개 | ✅ 전체 |
| 전체 테스트 | ~42초 | 20개 | - |

### FAQ

#### Q1. src/test/java는 어디갔나요?
**A.** 삭제했습니다. 모든 테스트는 `test-unit` 또는 `test-integration`으로 이동했습니다.

#### Q2. application.yaml을 따로 관리해야 하나요?
**A.** 아니요, `src/test/resources/application.yaml` 하나만 사용합니다. 단위/통합 테스트가 동일한 설정을 공유합니다.

#### Q3. 특정 테스트만 다른 설정을 사용하려면?
**A.** Spring Profile을 사용하세요:

```yaml
# src/test/resources/application-integration.yaml
spring:
  datasource:
    url: jdbc:h2:mem:integrationdb
```

```java
@SpringBootTest
@ActiveProfiles("integration")
class OrderIntegrationTest { ... }
```

#### Q4. Controller 테스트는 왜 없나요?
**A.** 현재는 Service + Integration 테스트로 충분합니다. Controller가 단순하고(Service 호출만), Integration 테스트로 전체 플로우가 검증되기 때문입니다. 추후 API 스펙 보장이 중요해지면 추가할 수 있습니다.

#### Q5. 테스트가 실패하면?
**A.** 다음을 확인하세요:
1. H2 데이터베이스 설정 (`src/test/resources/application.yaml`)
2. Mock 설정이 올바른지
3. `ReflectionTestUtils.setField()`로 ID 설정 여부 (엔티티 테스트 시)
4. ElasticSearch 테스트는 Docker 실행 여부

#### Q6. 테스트 커버리지 확인은?
**A.** JaCoCo 플러그인 사용 (추후 추가 가능):

```gradle
plugins {
    id 'jacoco'
}

test {
    finalizedBy jacocoTestReport
}
```

```bash
./gradlew test jacocoTestReport
open build/reports/jacoco/test/html/index.html
```

---

## 추천 워크플로우

### 개발 중 (TDD)
1. 단위 테스트 먼저 작성
2. 구현 코드 작성
3. `./gradlew unitTest` 실행 (빠른 피드백)
4. 리팩토링
5. 통합 테스트 작성
6. `./gradlew integrationTest` 실행

### CI/CD 파이프라인
```yaml
# GitHub Actions 예시
jobs:
  test:
    steps:
      - name: Unit Tests
        run: ./gradlew unitTest

      - name: Integration Tests
        run: ./gradlew integrationTest

      - name: Build
        run: ./gradlew build
```

---

## 테스트 통계

- **총 테스트 파일**: 20개
- **단위 테스트**: 8개 (service: 4개, domain: 4개)
- **통합 테스트**: 12개 (repository: 5개, infrastructure: 7개)
- **전체 실행 시간**: ~42초
- **테스트 커버리지**: 주요 비즈니스 로직 및 데이터 계층 커버

---

## 참고 자료

- [Spring Boot Testing](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.testing)
- [JUnit 5 User Guide](https://junit.org/junit5/docs/current/user-guide/)
- [Mockito Documentation](https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html)
- [AssertJ Documentation](https://assertj.github.io/doc/)
- [Testcontainers](https://www.testcontainers.org/)
