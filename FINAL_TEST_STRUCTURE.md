# 최종 테스트 구조 (계층별 분리)

## 완료된 구조

```
src/
├── test/
│   ├── java/                    # ❌ 삭제됨 (비어있음)
│   └── resources/               # ✅ 공통 설정
│       └── application.yaml
│
├── test-unit/                   # 단위 테스트
│   └── java/com/onbok/book_hub/
│       ├── service/             # 서비스 계층 단위 테스트
│       │   ├── RecommendationServiceTest.java
│       │   ├── CartCalculationServiceTest.java
│       │   ├── BookEsServiceTest.java
│       │   └── OAuth2UserServiceTest.java
│       │
│       ├── domain/              # 도메인 계층 단위 테스트
│       │   ├── BookStockTest.java
│       │   ├── CartTest.java
│       │   ├── DeliveryAddressTest.java
│       │   └── TossPaymentTest.java
│       │
│       └── util/                # 유틸리티 테스트 (추후 추가)
│
└── test-integration/            # 통합 테스트
    └── java/com/onbok/book_hub/
        ├── api/                 # API 계층 테스트 (추후 추가)
        │   └── (Controller + MockMvc 테스트)
        │
        ├── repository/          # Repository 계층 테스트
        │   ├── CartRepositoryTest.java
        │   ├── OrderRepositoryTest.java
        │   ├── ReviewRepositoryTest.java
        │   ├── DeliveryAddressRepositoryTest.java
        │   └── ImageRepositoryTest.java
        │
        └── infrastructure/      # Infrastructure 계층 테스트
            ├── ElasticsearchIntegrationTest.java
            ├── ImageLocalUploadTest.java
            ├── ImageS3ServiceTest.java
            ├── OrderIntegrationTest.java
            ├── ReviewIntegrationTest.java
            ├── DeliveryAddressIntegrationTest.java
            └── TransactionStabilityTest.java
```

---

## 계층별 분류 기준

### 📦 단위 테스트 (Unit Test)

#### 1. **service/** - 서비스 계층
- **특징**: Mock/Stub을 활용한 격리된 테스트
- **어노테이션**: `@ExtendWith(MockitoExtension.class)`, `@Mock`, `@InjectMocks`
- **테스트 대상**: 비즈니스 로직, 계산, 추천 알고리즘
- **파일**:
  - `RecommendationServiceTest.java` - 추천 로직 검증
  - `CartCalculationServiceTest.java` - 장바구니 계산 로직
  - `BookEsServiceTest.java` - ElasticSearch 서비스 Mock 테스트
  - `OAuth2UserServiceTest.java` - OAuth2 사용자 서비스

#### 2. **domain/** - 도메인 계층
- **특징**: 순수 Java 객체 테스트 (Spring 없음)
- **어노테이션**: `@Test`만 사용
- **테스트 대상**: 엔티티 비즈니스 로직, 값 객체 검증
- **파일**:
  - `BookStockTest.java` - 재고 증감 로직
  - `CartTest.java` - 장바구니 도메인 로직
  - `DeliveryAddressTest.java` - 배송지 도메인 로직
  - `TossPaymentTest.java` - 결제 상태 전환 로직

#### 3. **util/** - 유틸리티 (추후 추가)
- **특징**: 정적 메서드, 헬퍼 함수 테스트
- **예시**: DateUtil, StringUtil, ValidationUtil 등

---

### 🔗 통합 테스트 (Integration Test)

#### 1. **api/** - API 계층 (추후 추가)
- **특징**: Controller + MockMvc를 사용한 E2E 테스트
- **어노테이션**: `@WebMvcTest`, `@AutoConfigureMockMvc`
- **테스트 대상**: REST API 엔드포인트, HTTP 요청/응답
- **예시 (추후 추가)**:
  - `BookControllerTest.java`
  - `OrderControllerTest.java`
  - `UserControllerTest.java`

#### 2. **repository/** - Repository 계층
- **특징**: JPA Repository 쿼리 메서드 테스트
- **어노테이션**: `@DataJpaTest`
- **테스트 대상**: CRUD, 커스텀 쿼리, JOIN 쿼리
- **파일**:
  - `CartRepositoryTest.java` - 장바구니 쿼리 검증
  - `OrderRepositoryTest.java` - 주문 쿼리 검증
  - `ReviewRepositoryTest.java` - 리뷰 쿼리 검증
  - `DeliveryAddressRepositoryTest.java` - 배송지 쿼리 검증
  - `ImageRepositoryTest.java` - 이미지 쿼리 검증

#### 3. **infrastructure/** - Infrastructure 계층
- **특징**: 외부 시스템 연동, 전체 플로우 테스트
- **어노테이션**: `@SpringBootTest`, `@Transactional`, `@Testcontainers`
- **테스트 대상**: ElasticSearch, S3, Redis, Kafka, 트랜잭션 등
- **파일**:
  - `ElasticsearchIntegrationTest.java` - ElasticSearch 검색
  - `ImageLocalUploadTest.java` - 로컬 파일 업로드
  - `ImageS3ServiceTest.java` - S3 파일 업로드
  - `OrderIntegrationTest.java` - 주문 플로우
  - `ReviewIntegrationTest.java` - 리뷰 플로우
  - `DeliveryAddressIntegrationTest.java` - 배송지 관리
  - `TransactionStabilityTest.java` - 트랜잭션 안정성

---

## 테스트 실행 명령어

### 단위 테스트만 실행 (빠름 ~13초)
```bash
./gradlew unitTest
```

### 통합 테스트만 실행 (~20초)
```bash
./gradlew integrationTest
```

### 전체 테스트 실행 (~42초)
```bash
./gradlew test
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

## 주요 변경사항

### ✅ 완료된 작업

1. **src/test/java 삭제**
   - 비어있는 디렉토리 제거
   - 모든 테스트 파일 이동 완료

2. **application.yaml 통합**
   - `src/test/resources/application.yaml` 하나만 사용
   - 단위/통합 테스트가 동일한 설정 공유
   - 중복 제거로 관리 간소화

3. **계층별 디렉토리 분리**
   - 단위 테스트: service, domain, util
   - 통합 테스트: api, repository, infrastructure
   - 명확한 책임 분리

4. **모든 테스트 성공**
   - 단위 테스트: 8개 파일, 모두 통과
   - 통합 테스트: 12개 파일, 모두 통과
   - 총 20개 테스트 파일

---

## 테스트 작성 가이드

### 새로운 테스트 추가 위치

#### Service 계층 테스트 추가 시
```bash
src/test-unit/java/com/onbok/book_hub/service/
```

**예시**:
```java
// src/test-unit/java/com/onbok/book_hub/service/UserServiceTest.java
package com.onbok.book_hub.user.application;  // 원래 패키지 유지

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void registerUser_success() {
        // 테스트 코드
    }
}
```

#### Repository 계층 테스트 추가 시
```bash
src/test-integration/java/com/onbok/book_hub/repository/
```

**예시**:
```java
// src/test-integration/java/com/onbok/book_hub/repository/BookRepositoryTest.java
package com.onbok.book_hub.book.domain.repository.book;  // 원래 패키지 유지

@DataJpaTest
class BookRepositoryTest {
    @Autowired
    private BookRepository bookRepository;

    @Test
    void findByTitle_success() {
        // 테스트 코드
    }
}
```

#### API 계층 테스트 추가 시 (추후)
```bash
src/test-integration/java/com/onbok/book_hub/api/
```

**예시**:
```java
// src/test-integration/java/com/onbok/book_hub/api/BookControllerTest.java
package com.onbok.book_hub.book.presentation.book.api;

@WebMvcTest(BookApiController.class)
class BookControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void getBooks_success() throws Exception {
        mockMvc.perform(get("/api/books"))
               .andExpect(status().isOk());
    }
}
```

---

## 패키지 구조 주의사항

### ⚠️ 중요: 패키지 선언은 유지
테스트 파일이 `src/test-unit/java/com/onbok/book_hub/service/` 디렉토리에 있어도,
**패키지 선언은 원래 테스트 대상 클래스의 패키지를 따릅니다.**

```java
// ❌ 잘못된 예
// 파일 위치: src/test-unit/java/com/onbok/book_hub/service/RecommendationServiceTest.java
package com.onbok.book_hub.service;  // 틀림!

// ✅ 올바른 예
// 파일 위치: src/test-unit/java/com/onbok/book_hub/service/RecommendationServiceTest.java
package com.onbok.book_hub.recommendation.application;  // 원래 패키지 유지
```

**이유**:
- 테스트 대상 클래스와 같은 패키지에 있어야 package-private 메서드 접근 가능
- IDE의 자동 import 기능 정상 작동
- 일반적인 Java 관례 준수

---

## 성능 비교

| 테스트 유형 | 실행 시간 | 파일 수 | 용도 |
|------------|----------|---------|------|
| 단위 테스트 | ~13초 | 8개 | 개발 중 빠른 피드백 |
| 통합 테스트 | ~20초 | 12개 | 배포 전 최종 검증 |
| 전체 테스트 | ~42초 | 20개 | CI/CD 파이프라인 |

---

## 추천 워크플로우

### 개발 중 (TDD)
1. 단위 테스트 먼저 작성
2. 구현 코드 작성
3. 단위 테스트 실행 (`./gradlew unitTest`)
4. 리팩토링
5. 통합 테스트 작성
6. 통합 테스트 실행 (`./gradlew integrationTest`)

### CI/CD 파이프라인
```yaml
# GitHub Actions 예시
jobs:
  test:
    steps:
      - name: Unit Tests
        run: ./gradlew unitTest

      - name: Integration Tests (if unit tests pass)
        run: ./gradlew integrationTest

      - name: Build (if all tests pass)
        run: ./gradlew build
```

---

## 다음 단계 (추후 확장)

### 1. API 계층 테스트 추가
- Controller + MockMvc 테스트
- REST API 엔드포인트 검증
- 위치: `src/test-integration/java/com/onbok/book_hub/api/`

### 2. Util 계층 테스트 추가
- 정적 유틸리티 메서드 테스트
- 위치: `src/test-unit/java/com/onbok/book_hub/util/`

### 3. 성능 테스트 추가
- JMeter 또는 Gatling 사용
- 위치: `src/test-performance/`

### 4. E2E 테스트 추가
- Selenium 또는 Playwright 사용
- 위치: `src/test-e2e/`

---

## 요약

✅ **src/test/java** - 삭제됨 (비어있음)
✅ **src/test/resources** - 공통 설정 (application.yaml)
✅ **test-unit/service** - 서비스 계층 단위 테스트 (4개)
✅ **test-unit/domain** - 도메인 계층 단위 테스트 (4개)
✅ **test-integration/repository** - Repository 계층 (@DataJpaTest, 5개)
✅ **test-integration/infrastructure** - Infrastructure 계층 (7개)
🔜 **test-integration/api** - API 계층 (추후 추가)
🔜 **test-unit/util** - 유틸리티 계층 (추후 추가)

**총 테스트**: 20개 파일, 모두 통과 ✅
**빌드 시간**: ~42초
**테스트 커버리지**: 주요 비즈니스 로직 및 데이터 계층 커버
