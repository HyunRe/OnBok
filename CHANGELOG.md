# OnBok Book-Hub 변경사항 문서

## 📅 2026년 1월 13일 업데이트

---

## 🎯 주요 기능 추가

### 1. Toss Payments Webhook 처리 (1순위)

#### 🔧 변경된 파일

**TossPayment 엔티티 개선**
- 파일: `src/main/java/com/onbok/book_hub/payment/domain/model/TossPayment.java`
- 추가 메서드:
  - `updateStatus(String status, LocalDateTime approvalTime)`: 결제 상태 업데이트
  - `cancel()`: 결제 취소 처리

**Webhook DTO 생성**
- 파일: `src/main/java/com/onbok/book_hub/payment/dto/TossWebhookRequestDto.java`
- 역할: Toss에서 전송하는 Webhook 이벤트 데이터 수신

**TossPaymentService 확장**
- 파일: `src/main/java/com/onbok/book_hub/payment/application/TossPaymentService.java`
- 추가 메서드:
  - `handleWebhook(TossWebhookRequestDto webhook)`: Webhook 이벤트 처리
  - `updateOrderStatus(Long orderId, String paymentStatus)`: 결제 상태에 따른 주문 상태 자동 업데이트
- 기능:
  - 결제 완료(DONE) → 주문 상태 PAYMENT_COMPLETED
  - 결제 취소(CANCELED) → 주문 취소 + 재고 복구
  - 결제 실패(EXPIRED/ABORTED) → 주문 취소

**PaymentApiController 개선**
- 파일: `src/main/java/com/onbok/book_hub/payment/presentation/api/PaymentApiController.java`
- 추가 엔드포인트:
  - `POST /api/payments/webhook`: Toss Webhook 수신
  - `GET /api/payments/{paymentId}`: 결제 상세 정보 조회

#### 📝 사용 방법

```bash
# Toss에서 Webhook 설정
# URL: https://your-domain.com/api/payments/webhook
# Method: POST

# 결제 상세 조회
curl -X GET "http://localhost:8080/api/payments/1"
```

---

### 2. 차트 시각화 (Chart.js) (2순위)

#### 🔧 변경된 파일

**차트 DTO 생성**
- `src/main/java/com/onbok/book_hub/order/dto/chart/DailySalesChartDto.java`
  - 일별 매출 및 주문 건수 데이터
- `src/main/java/com/onbok/book_hub/order/dto/chart/CategorySalesChartDto.java`
  - 카테고리별 판매 비율 데이터

**OrderStatisticsService 확장**
- 파일: `src/main/java/com/onbok/book_hub/order/application/OrderStatisticsService.java`
- 추가 메서드:
  - `getDailySalesChartData(int days)`: 최근 N일간 일별 매출 추이
  - `getCategorySalesChartData(int days)`: 카테고리별 판매 통계

**OrderApiController 개선**
- 파일: `src/main/java/com/onbok/book_hub/order/presentation/api/OrderApiController.java`
- 추가 엔드포인트:
  - `GET /api/orders/chart/daily-sales?days={N}`: 일별 매출 데이터
  - `GET /api/orders/chart/category-sales?days={N}`: 카테고리별 판매 데이터

**OrderViewController 개선**
- 파일: `src/main/java/com/onbok/book_hub/order/presentation/view/OrderViewController.java`
- 추가 엔드포인트:
  - `GET /view/orders/charts`: 통계 대시보드 페이지 (ADMIN 권한 필요)

**통계 대시보드 페이지**
- 파일: `src/main/resources/templates/order/charts.html`
- 기능:
  - 📈 라인 차트: 일별 매출 추이
  - 📊 바 차트: 일별 주문 건수
  - 🍩 도넛 차트: 카테고리별 판매 비율
  - 🔘 기간 선택 버튼 (7일/14일/30일)
  - 📱 반응형 디자인

#### 📝 사용 방법

```bash
# API 호출
curl -X GET "http://localhost:8080/api/orders/chart/daily-sales?days=7"
curl -X GET "http://localhost:8080/api/orders/chart/category-sales?days=30"

# 웹 페이지 접속 (ADMIN 권한 필요)
http://localhost:8080/view/orders/charts
```

#### 🎨 차트 종류

1. **일별 매출 추이 (Line Chart)**
   - X축: 날짜
   - Y축: 매출 금액 (원)
   - 특징: 영역 채우기, 부드러운 곡선

2. **일별 주문 건수 (Bar Chart)**
   - X축: 날짜
   - Y축: 주문 건수
   - 특징: 막대 그래프

3. **카테고리별 판매 비율 (Doughnut Chart)**
   - 데이터: 각 카테고리별 판매 수량
   - 특징: 퍼센티지 표시, 색상 구분

---

### 3. 검색 고도화 (3순위)

#### 🔧 변경된 파일

**카테고리 기능 추가**

1. **Book 엔티티**
   - 파일: `src/main/java/com/onbok/book_hub/book/domain/model/book/Book.java`
   - 추가 필드: `private String category;`

2. **BookEs 엔티티**
   - 파일: `src/main/java/com/onbok/book_hub/book/domain/model/bookEs/BookEs.java`
   - 추가 필드: `private String category;` (ElasticSearch 인덱싱)
   - 설정: MultiField로 검색과 정렬 모두 지원

**인기 검색어 기능**

1. **SearchKeyword 엔티티**
   - 파일: `src/main/java/com/onbok/book_hub/book/domain/model/search/SearchKeyword.java`
   - 필드:
     - `keyword`: 검색어
     - `searchCount`: 검색 횟수
   - 메서드: `increaseSearchCount()`: 검색 횟수 증가

2. **SearchKeywordRepository**
   - 파일: `src/main/java/com/onbok/book_hub/book/domain/repository/search/SearchKeywordRepository.java`
   - 쿼리 메서드:
     - `findByKeyword(String keyword)`: 검색어로 조회
     - `findTop10ByOrderBySearchCountDesc()`: Top 10 인기 검색어

3. **SearchKeywordService**
   - 파일: `src/main/java/com/onbok/book_hub/book/application/service/search/SearchKeywordService.java`
   - 메서드:
     - `recordSearch(String keyword)`: 검색어 저장 및 횟수 증가
     - `getPopularKeywords()`: 인기 검색어 Top 10 조회

4. **BookApiController 개선**
   - 파일: `src/main/java/com/onbok/book_hub/book/presentation/book/api/BookApiController.java`
   - 추가 엔드포인트:
     - `GET /api/books/popular-keywords`: 인기 검색어 조회

#### 📝 사용 방법

```bash
# 인기 검색어 조회
curl -X GET "http://localhost:8080/api/books/popular-keywords"

# 응답 예시
{
  "data": [
    {"keyword": "자바", "searchCount": 150},
    {"keyword": "스프링부트", "searchCount": 120},
    {"keyword": "알고리즘", "searchCount": 95}
  ]
}
```

#### 🔄 검색어 저장 흐름

1. 사용자가 도서 검색 실행
2. BookQueryService 또는 BookEsService에서 `searchKeywordService.recordSearch(query)` 호출
3. 검색어가 이미 존재하면 searchCount 증가
4. 새로운 검색어면 DB에 저장 (searchCount = 1)

---

## 🔨 기타 개선사항

### 배송지 관리 개선

**DeliveryAddress 엔티티**
- 파일: `src/main/java/com/onbok/book_hub/delivery/domain/model/DeliveryAddress.java`
- 변경: User와 @ManyToOne 관계 추가
- 효과: 사용자별 배송지 관리 가능

**DeliveryAddressService**
- 파일: `src/main/java/com/onbok/book_hub/delivery/application/DeliveryAddressService.java`
- 개선: `insertDeliveryAddress(User user, DeliveryAddress deliveryAddress)`
- 추가 메서드:
  - `findByUser(User user)`: 사용자별 배송지 목록 조회
  - `countByUser(User user)`: 사용자별 배송지 개수 조회

**OrderApiController**
- 파일: `src/main/java/com/onbok/book_hub/order/presentation/api/OrderApiController.java`
- 변경: `@CurrentUser` 파라미터 추가
- 추가 엔드포인트:
  - `GET /api/orders/delivery-addresses`: 내 배송지 목록 조회

---

### 리뷰 Controller 분리

**ReviewApiController**
- 파일: `src/main/java/com/onbok/book_hub/review/presentation/api/ReviewApiController.java`
- 경로: `/api/reviews/*`
- 역할: CUD(Create, Update, Delete) 작업
- 엔드포인트:
  - `POST /api/reviews/create`: 리뷰 작성
  - `POST /api/reviews/update`: 리뷰 수정
  - `POST /api/reviews/delete`: 리뷰 삭제

**ReviewViewController**
- 파일: `src/main/java/com/onbok/book_hub/review/presentation/view/ReviewViewController.java`
- 경로: `/view/reviews/*`
- 역할: 조회 및 View 렌더링
- 엔드포인트:
  - `GET /view/reviews/book/{bid}`: 특정 도서의 리뷰 목록 (페이징)
  - `GET /view/reviews/my-reviews`: 내가 작성한 리뷰 목록

---

### BookEsViewController 리팩토링

**변경사항**
- 파일: `src/main/java/com/onbok/book_hub/book/presentation/bookEs/view/BookEsViewController.java`
- 개선:
  - PaginationUtil 사용 (BookViewController와 동일한 구조)
  - HttpSession 파라미터 추가
  - BookEs import 추가

**BookEsListResponseDto 생성**
- 파일: `src/main/java/com/onbok/book_hub/book/dto/response/BookEsListResponseDto.java`
- 역할: BookEs 목록과 PaginationInfo를 함께 반환

**BookEsService 개선**
- 파일: `src/main/java/com/onbok/book_hub/book/application/service/bookEs/BookEsService.java`
- 변경: `getPagedBooks()` 메서드가 `BookEsListResponseDto` 반환
- 효과: Controller에서 페이지네이션 계산 로직 제거

---

### 사용자 인증 개선

**UserRepository**
- 파일: `src/main/java/com/onbok/book_hub/user/domain/repository/UserRepository.java`
- 추가: `Optional<User> findByEmail(String email);`

**UserQueryService**
- 파일: `src/main/java/com/onbok/book_hub/user/application/UserQueryService.java`
- 추가: `User findByEmail(String email);`

**UserAuthService**
- 파일: `src/main/java/com/onbok/book_hub/user/application/UserAuthService.java`
- 변경: `login(String email, String pwd)` (기존: `login(Long id, String pwd)`)
- 효과: 일반적인 로그인 방식 (이메일 + 패스워드)

**UserViewController**
- 파일: `src/main/java/com/onbok/book_hub/user/presentation/UserViewController.java`
- 개선:
  - 로그아웃 메서드 제거 (Spring Security가 자동 처리)
  - Session 직접 관리 제거
  - 불필요한 import 정리

---

## 📊 데이터베이스 변경사항

### 새로운 테이블

1. **search_keywords**
   ```sql
   CREATE TABLE search_keywords (
       id BIGINT AUTO_INCREMENT PRIMARY KEY,
       keyword VARCHAR(255) NOT NULL UNIQUE,
       search_count BIGINT DEFAULT 1,
       created_at DATETIME,
       updated_at DATETIME
   );
   ```

### 컬럼 추가

1. **books 테이블**
   ```sql
   ALTER TABLE books ADD COLUMN category VARCHAR(255);
   ```

2. **deliveries 테이블**
   ```sql
   ALTER TABLE deliveries ADD COLUMN user_id BIGINT NOT NULL;
   ALTER TABLE deliveries ADD FOREIGN KEY (user_id) REFERENCES users(id);
   ```

### ElasticSearch 인덱스 변경

**books 인덱스에 category 필드 추가**
```json
{
  "category": {
    "type": "text",
    "analyzer": "my_nori_analyzer",
    "fields": {
      "keyword": {
        "type": "keyword"
      }
    }
  }
}
```

---

## 🚀 새로운 API 엔드포인트 목록

### Payment API
```
POST   /api/payments/webhook                      - Toss Webhook 수신
GET    /api/payments/{paymentId}                  - 결제 상세 조회
```

### Order API
```
GET    /api/orders/chart/daily-sales?days={N}     - 일별 매출 차트 데이터
GET    /api/orders/chart/category-sales?days={N}  - 카테고리별 판매 차트 데이터
GET    /api/orders/delivery-addresses             - 내 배송지 목록 조회
POST   /api/orders/delivery-address               - 배송지 저장
```

### Book API
```
GET    /api/books/popular-keywords                - 인기 검색어 Top 10
```

### Review API (새로 분리)
```
POST   /api/reviews/create                        - 리뷰 작성
POST   /api/reviews/update                        - 리뷰 수정
POST   /api/reviews/delete                        - 리뷰 삭제
```

### View Endpoints
```
GET    /view/orders/charts                        - 통계 대시보드 (ADMIN)
GET    /view/reviews/book/{bid}                   - 도서 리뷰 목록
GET    /view/reviews/my-reviews                   - 내가 작성한 리뷰
```

---

## 📦 새로운 의존성

### Chart.js
- CDN: `https://cdn.jsdelivr.net/npm/chart.js@4.4.0/dist/chart.umd.min.js`
- 용도: 통계 차트 시각화
- 사용 위치: `src/main/resources/templates/order/charts.html`

---

## 🔐 권한 설정

### ADMIN 권한 필요한 엔드포인트
```
GET /view/orders/charts          - 통계 대시보드
GET /view/orders/listAll         - 전체 주문 목록
```

### 로그인 필요한 엔드포인트
```
POST /api/books/cart                      - 장바구니 담기
GET  /api/orders/delivery-addresses       - 내 배송지 목록
POST /api/orders/delivery-address         - 배송지 저장
GET  /view/reviews/my-reviews             - 내 리뷰 목록
POST /api/reviews/*                       - 리뷰 CUD
```

---

## 🧪 테스트 방법

### 1. Webhook 테스트
```bash
# 로컬 테스트용 curl
curl -X POST http://localhost:8080/api/payments/webhook \
  -H "Content-Type: application/json" \
  -d '{
    "eventType": "PAYMENT_STATUS_CHANGED",
    "createdAt": "2026-01-13T10:00:00",
    "data": {
      "paymentKey": "test_payment_key",
      "orderId": "1",
      "status": "DONE",
      "approvedAt": "2026-01-13T10:00:00",
      "orderName": "테스트 주문",
      "method": "카드",
      "totalAmount": 15000,
      "version": "2022-11-16"
    }
  }'
```

### 2. 차트 데이터 확인
```bash
# 최근 7일 매출
curl http://localhost:8080/api/orders/chart/daily-sales?days=7

# 카테고리별 판매 (30일)
curl http://localhost:8080/api/orders/chart/category-sales?days=30
```

### 3. 인기 검색어 확인
```bash
curl http://localhost:8080/api/books/popular-keywords
```

---

## 📝 개발 노트

### 구조 개선
- ✅ Controller 레이어 분리 (API vs View)
- ✅ Service 레이어에서 비즈니스 로직 처리
- ✅ DTO를 통한 데이터 전송
- ✅ PaginationUtil을 사용한 일관된 페이지네이션

### 보안 강화
- ✅ @CurrentUser를 통한 인증 사용자 확인
- ✅ @CheckPermission을 통한 권한 검증
- ✅ Spring Security를 통한 자동 인증/인가

### 성능 최적화
- ✅ ElasticSearch 활용 (전문 검색)
- ✅ 낙관적 락 사용 (재고 관리)
- ✅ 카테고리 인덱싱 (빠른 필터링)

---

## 🐛 알려진 이슈

1. **MyUserDetails 클래스 누락**
   - 위치: `PermissionAspect.java`에서 참조
   - 해결 필요: MyUserDetails 클래스 생성 또는 타입 변경

2. **카테고리 필터링 미구현**
   - Book/BookEs에 category 필드는 추가됨
   - BookQueryService/BookEsService에 필터 로직은 아직 미구현

---

## 🎯 향후 개선 방향

### 단기 (1-2주)
- [ ] 카테고리 필터링 구현
- [ ] 평점별 필터링 추가
- [ ] 검색어 자동완성 UI 개선

### 중기 (1개월)
- [ ] 리뷰 평점 기반 추천 시스템
- [ ] 주문 통계 월별/분기별 확장
- [ ] 실시간 재고 알림

### 장기 (3개월)
- [ ] AI 기반 도서 추천
- [ ] 소셜 로그인 확장 (카카오, 구글)
- [ ] 모바일 앱 개발

---

## 📞 문의

프로젝트 관련 문의사항이 있으시면 이슈를 등록해주세요.

- Repository: [book-hub](https://github.com/your-repo/book-hub)
- Issues: [GitHub Issues](https://github.com/your-repo/book-hub/issues)

---

**마지막 업데이트**: 2026년 1월 13일
**버전**: 1.1.0
