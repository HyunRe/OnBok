# ONBOK Backend

> 도서 검색부터 주문·결제까지 원스톱 구매를 지원하는 서점 플랫폼으로, 검색 정확도를 개선하고 안전한 결제 흐름을 설계하여 실제 서비스 수준의 전자상거래 경험을 구현한 백엔드 시스템

---

# 1. 프로젝트 개요

## 1-1. 프로젝트 목표

### 일반 목표

- 단순 CRUD 기반 쇼핑몰이 아닌, 실사용 가능한 전자상거래 플랫폼 구현
- 도서 검색부터 주문·결제까지 원스톱 구매 경험 제공
- 사용자 친화적이고 안정적인 백엔드 서비스 제공

### 핵심 목표

- **Elasticsearch + Nori 형태소 분석기** 기반 고도화된 검색 시스템 구축
- **Toss Payments** 연동을 통한 실제 결제 흐름 구현
- **도메인 중심 설계**를 통한 주문·결제 상태 일관성 보장
- **낙관적 락**을 활용한 재고 동시성 제어

---

## 1-2. 핵심 기능

- Elasticsearch 기반 도서 검색 (Nori 한글 형태소 분석)
- 자동완성 및 부분 일치 검색
- 협업 필터링 기반 사용자 맞춤 추천
- Toss Payments 결제 요청, 승인, 취소, 환불 처리
- 주문 상태 전이 관리 및 재고 동시성 제어
- JWT + OAuth2 기반 인증/인가
- 예외 처리 통합 관리

---

## 1-3. 기술 스택

| 분류 | 기술 |
|------|------|
| Language | Java 17 |
| Framework | Spring Boot 3.x |
| Security | Spring Security, JWT (jjwt), OAuth2 (Google, Naver, GitHub) |
| ORM | Spring Data JPA |
| Database | MariaDB |
| Search | ElasticSearch 8.x (Nori 형태소 분석기) |
| Payment | Toss Payments API |
| Infrastructure | Docker, Testcontainers |
| Test | JUnit5, Mockito, AssertJ, Testcontainers |
| Docs | Swagger (springdoc-openapi) |

---

## 1-4. 시스템 아키텍처

```
                          ┌──────────┐
                          │  Client  │
                          │(Browser) │
                          └────┬─────┘
                               │
                               ▼
                   ┌───────────────────────┐
                   │   Spring Boot Server  │
                   │ (REST API + Security) │
                   └───────────┬───────────┘
                               │
      ┌────────────────────────┼────────────────────────┐
      │                        │                        │
      ▼                        ▼                        ▼
┌───────────┐          ┌─────────────┐          ┌─────────────┐
│  MariaDB  │          │ElasticSearch│          │    Toss     │
│   (DB)    │          │  (Search)   │          │  Payments   │
└───────────┘          └─────────────┘          └─────────────┘
```

### 설계 원칙

- **DDD 기반 패키지 구조**
    - 도메인별 패키지 분리 (book, cart, order, payment, user, review, recommendation 등)
    - 계층 분리: application / domain / presentation / dto
- **계층별 역할 분리**
    - Controller → 요청/응답만 담당
    - Service → 비즈니스 로직 집중 (CQS 패턴: Command/Query 분리)
    - Entity → 도메인 로직 캡슐화 (상태 전이, 유효성 검증)
- **동시성 제어**
    - `@Version` 낙관적 락을 활용한 재고 동시성 제어
    - 트랜잭션 원자성 보장 (주문 취소/환불 시 재고 복구)
- **상태 관리**
    - Enum 기반 상태 관리 (OrderStatus, ImageStorageType 등)
    - 상태 전이 규칙 검증 (`canTransitionTo` 메서드)

---

# 2. 도메인 설계

## 2-1. 핵심 엔티티

```
Order
├── OrderItem (주문 상품)
├── TossPayment (결제 정보)
├── DeliveryAddress (배송지)
└── User (주문자)

Book
├── stock (재고)
├── version (낙관적 락)
└── BookEs (Elasticsearch 문서)
```

### 주요 엔티티

| 엔티티 | 역할 |
|--------|------|
| Order | 주문 관리 및 상태 전이 |
| OrderItem | 주문 상품 정보 |
| Book | 도서 정보 및 재고 관리 |
| TossPayment | Toss 결제 정보 |
| User | 사용자 인증 및 프로필 |
| Review | 도서 리뷰 및 별점 |
| DeliveryAddress | 배송지 관리 |

---

## 2-2. 상태 관리 (Enum 기반)

### OrderStatus 상태 전이

| 상태 | 설명 | 전이 가능 상태 |
|------|------|----------------|
| PENDING | 주문 대기 | PAYMENT_COMPLETED, CANCELLED |
| PAYMENT_COMPLETED | 결제 완료 | PREPARING, CANCELLED |
| PREPARING | 상품 준비중 | SHIPPED, CANCELLED |
| SHIPPED | 배송중 | DELIVERED |
| DELIVERED | 배송 완료 | REFUNDED |
| CANCELLED | 주문 취소 | (최종 상태) |
| REFUNDED | 환불 완료 | (최종 상태) |

```
PENDING → PAYMENT_COMPLETED → PREPARING → SHIPPED → DELIVERED
            ↓                    ↓                      ↓
         CANCELLED           CANCELLED              REFUNDED
```

상태 변경은 반드시 엔티티 내부 메서드를 통해 수행

```java
public void changeStatus(OrderStatus newStatus) {
    if (!this.status.canTransitionTo(newStatus)) {
        throw new IllegalStateException(
            String.format("주문 상태를 %s에서 %s로 변경할 수 없습니다.",
                this.status.getDescription(), newStatus.getDescription())
        );
    }
    this.status = newStatus;
}
```

서비스 계층이 아닌 **엔티티 내부에서 상태 전이 규칙 검증**

---

# 3. 내가 담당한 부분

## 3-1. Elasticsearch 검색 시스템 구축

### 문제

- 기본 SQL LIKE 검색으로는 한글 형태소 분석 불가
- "스프링 부트"로 검색 시 "스프링부트" 결과 누락

### 적용 방식

- **Nori 형태소 분석기** 적용 (한글 토큰화)
- **MultiField 매핑**: Text + Keyword 이중 필드 구성
- **가중치 기반 멀티 필드 검색**: 제목(2.0) > 저자(1.5) > 요약(1.0)
- **Fuzzy 검색**: 오타 자동 교정 (AUTO fuzziness)

```java
@Query("""
{
  "multi_match": {
    "query": "?0",
    "fields": ["title^2", "author^1.5", "summary^1"],
    "type": "best_fields"
  }
}
""")
Page<BookEs> searchByMultiField(String keyword, Pageable pageable);
```

### 개선 효과

- 한글 검색 정확도 향상
- 오타 허용으로 사용자 경험 개선
- 관련성 기반 정렬로 검색 품질 향상

---

## 3-2. 협업 필터링 추천 알고리즘 구현

### 문제

- 베스트셀러 기반 추천은 개인화 부족
- 사용자별 맞춤 추천 필요

### 적용 방식

5가지 추천 알고리즘 구현:

| 알고리즘 | 설명 | 점수 계산 |
|----------|------|-----------|
| 구매 기반 | 구매 이력의 동일 저자/출판사 도서 | 저자 +3, 출판사 +1 |
| 리뷰 기반 | 4점 이상 평가한 도서의 동일 저자 | +5점 |
| 협업 필터링 | 유사 사용자(공통 구매) 기반 추천 | 유사도 * 가중치 |
| 인기 도서 | 전체 주문 수량 기준 | 주문량 순 |
| 고평점 도서 | 평균 평점 기준 (최소 3개 리뷰) | 평점 순 |

```java
// 협업 필터링: 유사 사용자 찾기
Map<Long, Integer> similarUsers = new HashMap<>();
for (Long otherUserId : allUserIds) {
    Set<Long> commonBooks = getCommonPurchasedBooks(userId, otherUserId);
    if (!commonBooks.isEmpty()) {
        similarUsers.put(otherUserId, commonBooks.size());
    }
}
```

### 개선 효과

- 개인화된 추천으로 구매 전환율 향상 기대
- 다양한 추천 로직으로 콜드 스타트 문제 해결

---

## 3-3. 낙관적 락 기반 재고 동시성 제어

### 문제

- 동시 주문 시 재고 음수 발생 가능
- 비관적 락은 성능 저하 우려

### 적용 방식

```java
@Entity
public class Book {
    @Version
    private Long version;

    private int stock;

    public void decreaseStock(int quantity) {
        if (this.stock < quantity) {
            throw new IllegalStateException("재고가 부족합니다.");
        }
        this.stock -= quantity;
    }

    public void increaseStock(int quantity) {
        this.stock += quantity;
    }
}
```

- `@Version` 필드로 동시 수정 감지
- 충돌 시 `OptimisticLockException` 발생 → 재시도

### 개선 효과

- 데이터 정합성 보장
- 비관적 락 대비 높은 동시 처리량

---

## 3-4. Toss Payments 결제 연동

### 적용 방식

```java
// 결제 승인
public String approvePayment(PaymentApproveRequestDto request) {
    String confirmUrl = API_BASE_URL + "/v1/payments/confirm";
    // POST request with paymentKey, orderId, amount
}

// 결제 취소
public void cancelPayment(PaymentCancelRequestDto request) {
    String cancelUrl = API_BASE_URL + "/v1/payments/" + paymentKey + "/cancel";
    // POST request with cancelReason
}

// Webhook 처리 (결제 상태 동기화)
@Transactional
public void handleWebhook(TossWebhookRequestDto dto) {
    // DONE → OrderStatus.PAYMENT_COMPLETED
    // CANCELED → OrderStatus.CANCELLED
}
```

### 결제 흐름

1. 클라이언트 → Toss 결제창 → 결제 완료
2. Toss → 서버 Webhook 호출 → 주문 상태 변경
3. 취소/환불 시 재고 자동 복구

---

## 3-5. 예외 처리 통합 설계

### 적용 방식

- **ErrorCode Enum**: 18개 에러 코드 정의
- **ExpectedException**: 비즈니스 예외 래핑
- **GlobalExceptionHandler**: 중앙 집중 예외 처리

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ExpectedException.class)
    public ResponseEntity<OnBokResponse<String>> handleExpectedException(ExpectedException e) {
        return ResponseEntity.badRequest()
            .body(OnBokResponse.error(e.getErrorCode()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationException(MethodArgumentNotValidException e) {
        // @Valid 검증 실패 처리
    }
}
```

### 응답 형식

```json
{
  "status": 400,
  "message": "존재하지 않는 도서입니다."
}
```

---

## 3-6. N+1 문제 해결

### 문제

- 주문 목록 조회 시 OrderItem, User 각각 쿼리 발생
- 100건 조회 시 300+ 쿼리

### 해결

- Fetch Join 적용
- `@EntityGraph` 활용
- 필요한 필드만 DTO 프로젝션

---

## 3-7. 트랜잭션 범위 최소화

- `@Transactional(readOnly = true)` 조회 분리
- 쓰기 작업 최소 범위 지정
- 불필요한 락 방지

---

# 4. 테스트 전략

```
src/test-unit/          # 단위 테스트 (9개)
src/test-integration/   # 통합 테스트 (13개)
```

Gradle Custom SourceSet으로 분리 관리

## 4-1. 단위 테스트 (9개)

| 파일 | 대상 |
|------|------|
| BookStockTest | 재고 증감, 동시성 시나리오 |
| CartTest | 장바구니 도메인 로직 |
| DeliveryAddressTest | 배송지 도메인 로직 |
| TossPaymentTest | 결제 도메인 로직 |
| CartCalculationServiceTest | 장바구니 금액 계산 |
| RecommendationServiceTest | 추천 알고리즘 5가지 검증 |
| BookEsServiceTest | Elasticsearch 검색 |
| OAuth2UserServiceTest | OAuth2 인증 로직 |
| OrderViewControllerCancelRefundTest | 주문 취소/환불 컨트롤러 |

## 4-2. 통합 테스트 (13개)

### Repository 테스트 (5개)

| 파일 | 대상 |
|------|------|
| CartRepositoryTest | 장바구니 CRUD |
| DeliveryAddressRepositoryTest | 배송지 CRUD |
| ImageRepositoryTest | 이미지 저장/조회 |
| OrderRepositoryTest | 주문 상태별 조회 |
| ReviewRepositoryTest | 리뷰 CRUD, 중복 체크 |

### Infrastructure 테스트 (8개)

| 파일 | 대상 |
|------|------|
| DeliveryAddressIntegrationTest | 배송지 서비스 플로우 |
| OrderIntegrationTest | 주문 생성 및 재고 감소 |
| ReviewIntegrationTest | 리뷰 작성/수정/삭제 |
| ImageLocalUploadTest | 로컬 이미지 업로드 |
| ImageS3ServiceTest | S3 URL 등록 |
| TransactionRollbackTest | 재고 부족 시 롤백 |
| TransactionStabilityTest | 트랜잭션 원자성 |
| ElasticsearchIntegrationTest | ES 검색 (Docker) |

## 4-3. 실행

```bash
# 단위 테스트
./gradlew unitTest

# 통합 테스트
./gradlew integrationTest

# 전체 테스트
./gradlew check
```

---

# 5. 트러블슈팅

## 5-1. Elasticsearch Nori 플러그인 누락

- **문제**: Testcontainers ES 이미지에 Nori 플러그인 미포함
- **증상**: `Unknown tokenizer type [nori_tokenizer]` 에러
- **해결**: 컨테이너 시작 시 플러그인 설치 명령 추가

```java
.withCommand("sh", "-c",
    "bin/elasticsearch-plugin install analysis-nori && bin/elasticsearch")
```

## 5-2. 재고 동시성 문제

- **문제**: 동시 주문 시 재고가 음수로 감소
- **해결**: `@Version` 낙관적 락 적용

## 5-3. N+1 쿼리 문제

- **문제**: 연관 엔티티 조회 시 쿼리 폭발
- **해결**: Fetch Join 적용

## 5-4. 통합 테스트 프로파일 미적용

- **문제**: IntelliJ에서 통합 테스트 실행 시 로컬 DB 연결 시도
- **해결**: `@ActiveProfiles("test")` 추가 + 별도 리소스 디렉토리 구성

---

# 6. 배운 점

- **도메인 설계는 "기능 구현"보다 "규칙 정의"에 가깝다.**
    - OrderStatus 상태 전이 규칙을 Enum 내부에 정의
    - 잘못된 상태 전이를 컴파일 타임에 방지

- **비즈니스 로직은 서비스가 아닌 엔티티가 가져야 한다.**
    - `decreaseStock()`, `changeStatus()` 등 도메인 메서드
    - 응집도 높은 설계

- **테스트가 가능한 구조는 곧 책임이 명확한 구조다.**
    - 단위/통합 테스트 분리
    - Mock 없이 도메인 로직 테스트 가능

- **성능 문제는 설계 단계에서 예방하는 것이 가장 좋다.**
    - N+1 문제 사전 인지
    - 낙관적 락 선택 이유 명확화

- **검색 시스템은 언어 특성을 고려해야 한다.**
    - 한글은 형태소 분석기 필수
    - Nori 토크나이저로 검색 품질 대폭 향상

---

# 7. 실행 방법

## 7-1. 필수 프로그램

- JDK 17 이상
- MariaDB 10.x 또는 MySQL 8
- Elasticsearch 8.x (Nori 플러그인)
- Docker (통합 테스트용)

## 7-2. 데이터베이스 생성

```sql
CREATE DATABASE book_hub CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

## 7-3. 설정 파일 생성

```bash
cp src/main/resources/application-secret.yaml.example \
   src/main/resources/application-secret.yaml
# DB 비밀번호, Toss API 키 등 설정
```

## 7-4. 애플리케이션 실행

```bash
./gradlew bootRun --args='--spring.profiles.active=local'
```

## 7-5. 접속

- 웹: http://localhost:8080
- Swagger: http://localhost:8080/swagger-ui.html
