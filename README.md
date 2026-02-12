# OnBok Book-Hub
온라인 서점 플랫폼 - 도서 검색 · 구매 · 추천 시스템 백엔드

ElasticSearch 기반 검색과 협업 필터링 추천 알고리즘을 적용한 Spring Boot 백엔드 시스템입니다.

## 1. 프로젝트 소개

### 기획 배경
기존 온라인 서점은 단순 키워드 검색과 베스트셀러 기반 추천에 의존합니다. OnBok Book-Hub는 다음을 목표로 설계되었습니다:

- **ElasticSearch + Nori 형태소 분석기**를 활용한 한글 검색 최적화
- **협업 필터링 기반 추천 알고리즘** 구현
- **JWT + OAuth2** 기반 인증/인가 시스템
- **Toss Payments** 결제 연동
- **낙관적 락**을 활용한 재고 동시성 제어
- **테스트 자동화** 및 안정적인 코드 품질 관리

## 2. 기술 스택

| 분류 | 기술 |
|------|------|
| Language | Java 17 |
| Framework | Spring Boot 3.x |
| Security | Spring Security, JWT (jjwt), OAuth2 (Google, Naver, GitHub) |
| ORM | Spring Data JPA |
| Database | MariaDB |
| Search | ElasticSearch 8.x (Nori 형태소 분석기) |
| Payment | Toss Payments API |
| Template | Thymeleaf, Bootstrap 5 |
| Infra | Docker, Testcontainers |
| Test | JUnit5, Mockito, AssertJ, Testcontainers |
| Docs | Swagger (springdoc-openapi) |

## 3. 시스템 아키텍처

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
    - 도메인별 패키지 분리 (book, cart, order, payment, user, review 등)
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

## 4. 프로젝트 구조

```
com.onbok.book_hub
├── book/              # 도서 관리 (검색, 조회, 재고)
├── cart/              # 장바구니
├── order/             # 주문 관리 (상태 전이, 취소/환불)
├── payment/           # 결제 (Toss Payments 연동)
├── delivery/          # 배송지 관리
├── review/            # 리뷰 (별점, 후기)
├── recommendation/    # 추천 알고리즘 (협업 필터링)
├── user/              # 사용자, 인증 (OAuth2)
├── image/             # 이미지 관리 (로컬/S3)
└── common/            # 공통 설정, 보안, 예외 처리
```

## 5. 핵심 기능

### 인증 / 인가
- OAuth2 소셜 로그인 (Google, Naver, GitHub)
- JWT 기반 인증
- Spring Security 필터 체인

### ElasticSearch 검색
- Nori 형태소 분석기 기반 한글 검색
- 제목, 저자, 출판사 멀티 필드 검색
- 가격 범위 필터링

### 추천 알고리즘
협업 필터링 기반으로 유사 사용자가 구매한 도서 추천

| 추천 방식 | 설명 |
|-----------|------|
| 구매 기반 | 유사 사용자의 구매 도서 |
| 리뷰 기반 | 평점 4점 이상 도서 필터링 |
| 인기 기반 | 주문 수량 기준 정렬 |

### 주문 상태 관리
```
PENDING → PAYMENT_COMPLETED → PREPARING → SHIPPED → DELIVERED
            ↓                                          ↓
         CANCELLED                                  REFUNDED
```

### 재고 관리
- `@Version` 낙관적 락을 활용한 동시성 제어
- 주문 시 자동 재고 감소
- 주문 취소/환불 시 재고 복구

### 결제 시스템
- Toss Payments API 연동
- 결제 승인, 취소, 환불 처리

## 6. 테스트 전략

```
src/test-unit/          # 단위 테스트 (9개)
src/test-integration/   # 통합 테스트 (13개)
```

Gradle Custom SourceSet으로 분리 관리

### 단위 테스트 (9개)

| 파일 | 대상 |
|------|------|
| BookStockTest | 재고 도메인 로직 |
| CartTest | 장바구니 도메인 로직 |
| DeliveryAddressTest | 배송지 도메인 로직 |
| TossPaymentTest | 결제 도메인 로직 |
| CartCalculationServiceTest | 장바구니 금액 계산 |
| RecommendationServiceTest | 추천 알고리즘 (6가지 규칙) |
| BookEsServiceTest | ElasticSearch 검색 서비스 |
| OAuth2UserServiceTest | OAuth2 인증 로직 |
| OrderViewControllerCancelRefundTest | 주문 취소/환불 컨트롤러 |

### 통합 테스트 - Repository (5개)

| 파일 | 대상 |
|------|------|
| CartRepositoryTest | 장바구니 저장/조회/삭제 |
| DeliveryAddressRepositoryTest | 배송지 CRUD |
| ImageRepositoryTest | 이미지 저장/조회 |
| OrderRepositoryTest | 주문 상태별 조회 |
| ReviewRepositoryTest | 리뷰 CRUD, 중복 체크 |

### 통합 테스트 - Infrastructure (8개)

| 파일 | 대상 |
|------|------|
| DeliveryAddressIntegrationTest | 배송지 서비스 전체 플로우 |
| OrderIntegrationTest | 주문 생성 및 재고 감소 |
| ReviewIntegrationTest | 리뷰 작성/수정/삭제 |
| ImageLocalUploadTest | 로컬 이미지 업로드 |
| ImageS3ServiceTest | S3 이미지 URL 등록 |
| TransactionRollbackTest | 재고 부족 시 트랜잭션 롤백 |
| TransactionStabilityTest | 트랜잭션 원자성 보장 |
| ElasticsearchIntegrationTest | ElasticSearch 검색 (Docker) |

### 실행

```bash
# 단위 테스트
./gradlew unitTest

# 통합 테스트
./gradlew integrationTest

# 전체 테스트
./gradlew check
```

## 7. 실행 방법

### 1. 필수 프로그램 설치
- JDK 17 이상
- MariaDB 10.x 또는 MySQL 8
- ElasticSearch 8.x (Nori 플러그인 필수)
- Docker (통합 테스트용)

### 2. 데이터베이스 생성

```sql
CREATE DATABASE book_hub CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 3. 설정 파일 생성

```bash
# application-secret.yaml 생성 (DB 비밀번호, API 키 등)
cp src/main/resources/application-secret.yaml.example src/main/resources/application-secret.yaml
```

### 4. 애플리케이션 실행

```bash
./gradlew bootRun --args='--spring.profiles.active=local'
```

### 5. 접속

- 웹 애플리케이션: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html
- API Docs: http://localhost:8080/v3/api-docs

## 8. API 문서

애플리케이션 실행 후 Swagger UI에서 전체 API 명세 확인 가능

**주요 API:**
- `GET /api/books` - 도서 목록 조회
- `GET /api/books/search` - 도서 검색
- `POST /api/orders` - 주문 생성
- `POST /view/orders/cancel/{id}` - 주문 취소
- `POST /view/orders/refund/{id}` - 주문 환불

## 9. 트러블슈팅

### 1. ElasticSearch Nori 플러그인 누락
- **문제**: `Unknown tokenizer type [nori_tokenizer]` 에러
- **해결**: ElasticSearch 컨테이너에 `analysis-nori` 플러그인 설치

### 2. 재고 동시성 문제
- **문제**: 동시 주문 시 재고 음수 발생
- **해결**: `@Version` 낙관적 락 적용

### 3. N+1 쿼리 문제
- **문제**: 연관 엔티티 조회 시 쿼리 폭발
- **해결**: Fetch Join 적용

## 10. 프로젝트를 통해 얻은 경험

- ElasticSearch + Nori 형태소 분석기 기반 검색 시스템 구축
- 협업 필터링 추천 알고리즘 설계 및 구현
- JWT + OAuth2 인증 시스템 직접 구현
- Toss Payments API 결제 연동
- 낙관적 락을 활용한 동시성 제어
- CQS 패턴 적용 (Command/Query 분리)
- Testcontainers 기반 통합 테스트 환경 구축
- 단위/통합 테스트 분리 전략

---

**마지막 업데이트**: 2026년 2월
