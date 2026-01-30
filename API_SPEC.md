# OnBok Book-Hub API 명세서

## 📌 기본 정보

- **Base URL**: `http://localhost:8080`
- **인증 방식**: JWT (Bearer Token)
- **응답 형식**: JSON (OnBokResponse 래퍼)
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **API Docs**: http://localhost:8080/v3/api-docs

---

## 🔐 인증

대부분의 API는 JWT 인증이 필요합니다. 요청 헤더에 다음을 포함해야 합니다:

```
Authorization: Bearer {JWT_TOKEN}
```

---

## 📚 1. Book API

### 1.1 장바구니에 담기
- **URL**: `POST /api/books/cart`
- **인증**: 필수
- **설명**: 도서를 장바구니에 담습니다
- **Request Body**:
```json
{
  "id": 1,
  "quantity": 2
}
```
- **Response**:
```json
{
  "success": true,
  "data": {
    "currentCount": 5,
    "message": "장바구니에 담겼습니다."
  }
}
```

### 1.2 인기 검색어 조회
- **URL**: `GET /api/books/popular-keywords`
- **인증**: 불필요
- **설명**: 검색 횟수 기준 상위 10개의 인기 검색어를 조회합니다
- **Response**:
```json
{
  "success": true,
  "data": [
    {
      "keyword": "자바",
      "count": 150
    }
  ]
}
```

---

## 🔍 2. BookEs API (ElasticSearch)

### 2.1 초기 데이터 적재
- **URL**: `GET /api/bookEs/yes24`
- **인증**: 필수 (ADMIN 권한)
- **설명**: CSV 파일을 읽어 일래스틱서치에 인덱싱합니다

### 2.2 자동완성 (단어)
- **URL**: `GET /api/bookEs/autocomplete?f={field}&q={query}`
- **인증**: 불필요
- **설명**: 필드와 쿼리를 기반으로 단어 단위 자동완성 리스트를 반환합니다
- **Query Parameters**:
  - `f`: 검색 필드 (기본값: title)
  - `q`: 검색어

### 2.3 자동완성 (구문)
- **URL**: `GET /api/bookEs/autocomplete-phrase?f={field}&q={query}`
- **인증**: 불필요
- **설명**: 필드와 쿼리를 기반으로 구문 단위(Phrase) 자동완성 리스트를 반환합니다
- **Query Parameters**:
  - `f`: 검색 필드 (기본값: title)
  - `q`: 검색어

---

## 🛒 3. Cart API

### 3.1 장바구니 수량 변경
- **URL**: `POST /api/carts/update`
- **인증**: 필수
- **설명**: 장바구니 아이템의 수량을 수정합니다 (0이면 삭제)
- **Request Body**:
```json
{
  "id": 1,
  "quantity": 3
}
```
- **Response**:
```json
{
  "success": true,
  "data": {
    "subTotal": 45000,
    "totalPrice": 45000,
    "itemCount": 2
  }
}
```

---

## 📦 4. Order API

### 4.1 일별 매출 차트 데이터
- **URL**: `GET /api/orders/chart/daily-sales?days={days}`
- **인증**: 필수
- **설명**: 최근 N일간의 일별 매출 및 주문 건수 데이터를 조회합니다
- **Query Parameters**:
  - `days`: 조회 기간 (기본값: 7)
- **Response**:
```json
{
  "success": true,
  "data": {
    "labels": ["2026-01-19", "2026-01-20"],
    "salesData": [150000, 200000],
    "orderCountData": [10, 15]
  }
}
```

### 4.2 카테고리별 판매 비율 차트 데이터
- **URL**: `GET /api/orders/chart/category-sales?days={days}`
- **인증**: 필수
- **설명**: 최근 N일간의 카테고리별 판매 수량 데이터를 조회합니다
- **Query Parameters**:
  - `days`: 조회 기간 (기본값: 30)

---

## 💳 5. Payment API

### 5.1 결제 실패 처리
- **URL**: `GET /api/payments/failure?code={code}&message={message}`
- **인증**: 불필요
- **설명**: 결제 과정에서 발생한 실패 코드와 메시지를 수신하여 출력합니다

### 5.2 Toss Payments Webhook
- **URL**: `POST /api/payments/webhook`
- **인증**: 불필요 (Toss에서 호출)
- **설명**: Toss에서 결제 상태 변경 시 호출되는 Webhook 엔드포인트
- **Request Body**:
```json
{
  "eventType": "PAYMENT_STATUS_CHANGED",
  "data": {}
}
```

### 5.3 결제 상세 조회
- **URL**: `GET /api/payments/{paymentId}`
- **인증**: 필수
- **설명**: 특정 결제의 상세 정보를 조회합니다

---

## ⭐ 6. Review API

### 6.1 리뷰 작성
- **URL**: `POST /api/reviews/create`
- **인증**: 필수
- **설명**: 도서 ID와 평점, 내용을 입력하여 새로운 리뷰를 등록합니다
- **Request Body**:
```json
{
  "bookId": 1,
  "rating": 5,
  "content": "정말 좋은 책입니다!"
}
```

### 6.2 리뷰 수정
- **URL**: `POST /api/reviews/update`
- **인증**: 필수
- **설명**: 기존에 작성한 리뷰의 평점과 내용을 수정합니다
- **Request Body**:
```json
{
  "id": 1,
  "rating": 4,
  "content": "수정된 리뷰 내용"
}
```

### 6.3 리뷰 삭제
- **URL**: `POST /api/reviews/delete?id={id}`
- **인증**: 필수
- **설명**: 리뷰 ID를 통해 등록된 리뷰를 삭제합니다

---

## 🤖 7. Recommendation API

### 7.1 주문 내역 기반 추천
- **URL**: `GET /api/recommendations/order-based?limit={limit}`
- **인증**: 필수
- **설명**: 현재 사용자의 과거 주문 기록을 분석하여 유사한 도서를 추천합니다
- **Query Parameters**:
  - `limit`: 추천 개수 (기본값: 10)

### 7.2 리뷰 기반 추천
- **URL**: `GET /api/recommendations/review-based?limit={limit}`
- **인증**: 필수
- **설명**: 사용자가 작성한 리뷰와 평점을 바탕으로 선호할 만한 도서를 추천합니다
- **Query Parameters**:
  - `limit`: 추천 개수 (기본값: 10)

### 7.3 협업 필터링 기반 추천
- **URL**: `GET /api/recommendations/collaborative?limit={limit}`
- **인증**: 필수
- **설명**: 유사한 취향을 가진 다른 사용자들이 구매한 도서를 분석하여 추천합니다
- **Query Parameters**:
  - `limit`: 추천 개수 (기본값: 10)

### 7.4 인기 도서 추천
- **URL**: `GET /api/recommendations/popular?limit={limit}`
- **인증**: 불필요
- **설명**: 전체 사용자들 사이에서 판매량이 높은 인기 도서 목록을 반환합니다
- **Query Parameters**:
  - `limit`: 추천 개수 (기본값: 10)

### 7.5 평점 높은 도서 추천
- **URL**: `GET /api/recommendations/highly-rated?limit={limit}`
- **인증**: 불필요
- **설명**: 사용자 평점이 높은 고득점 도서 목록을 반환합니다
- **Query Parameters**:
  - `limit`: 추천 개수 (기본값: 10)

### 7.6 개인화 복합 추천
- **URL**: `GET /api/recommendations/personalized?limit={limit}`
- **인증**: 필수
- **설명**: 주문 이력, 리뷰, 협업 필터링 결과를 일정 비율로 혼합하여 사용자 맞춤형 결과를 제공합니다
- **Query Parameters**:
  - `limit`: 총 추천 개수 (기본값: 10, 각 알고리즘당 1/3씩 할당)
- **Response**:
```json
{
  "success": true,
  "data": {
    "type": "PERSONALIZED_MIXED",
    "orderBasedBooks": [],
    "reviewBasedBooks": [],
    "collaborativeBooks": []
  }
}
```

---

## 📍 8. DeliveryAddress API

### 8.1 배송지 목록 조회
- **URL**: `GET /api/delivery-addresses`
- **인증**: 필수
- **설명**: 현재 사용자의 배송지 목록을 조회합니다
- **Response**:
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "alias": "집",
      "recipient": "홍길동",
      "phone": "010-1234-5678",
      "address": "서울시 강남구",
      "detailAddress": "테헤란로 123",
      "zipCode": "06234",
      "isDefault": true
    }
  ]
}
```

### 8.2 배송지 상세 조회
- **URL**: `GET /api/delivery-addresses/{id}`
- **인증**: 필수
- **설명**: 특정 배송지의 상세 정보를 조회합니다

### 8.3 배송지 등록
- **URL**: `POST /api/delivery-addresses`
- **인증**: 필수
- **설명**: 새로운 배송지를 등록합니다
- **Request Body**:
```json
{
  "alias": "회사",
  "recipient": "홍길동",
  "phone": "010-1234-5678",
  "address": "서울시 강남구",
  "detailAddress": "테헤란로 456",
  "zipCode": "06235",
  "isDefault": false
}
```

### 8.4 배송지 수정
- **URL**: `PUT /api/delivery-addresses/{id}`
- **인증**: 필수
- **설명**: 기존 배송지 정보를 수정합니다
- **Request Body**: 8.3과 동일

### 8.5 배송지 삭제
- **URL**: `DELETE /api/delivery-addresses/{id}`
- **인증**: 필수
- **설명**: 배송지를 삭제합니다

---

## 🖼️ 9. Image API

### 9.1 로컬 파일 업로드
- **URL**: `POST /api/images/upload/local`
- **인증**: 필수
- **설명**: 서버의 로컬 스토리지에 파일을 업로드합니다
- **Request**: multipart/form-data
  - `file`: 업로드할 이미지 파일
- **Response**:
```json
{
  "success": true,
  "data": {
    "id": 1,
    "originalFilename": "book.jpg",
    "storedFilename": "uuid-book.jpg",
    "fileUrl": "http://localhost:8080/uploads/uuid-book.jpg",
    "fileSize": 1024000,
    "storageType": "LOCAL"
  }
}
```

### 9.2 S3 URL 등록
- **URL**: `POST /api/images/upload/s3-url`
- **인증**: 필수
- **설명**: 이미 업로드된 S3 객체의 URL을 데이터베이스에 등록합니다
- **Query Parameters**:
  - `url`: S3 URL
  - `filename`: 원본 파일명 (선택)

### 9.3 S3 파일 직접 업로드
- **URL**: `POST /api/images/upload/s3`
- **인증**: 필수
- **설명**: S3 버킷으로 파일을 직접 업로드합니다 (현재는 구현 예정)
- **Request**: multipart/form-data
  - `file`: 업로드할 이미지 파일

---

## 📋 공통 응답 구조

모든 API는 다음과 같은 공통 응답 구조를 사용합니다:

### 성공 응답
```json
{
  "success": true,
  "data": {},
  "message": "성공 메시지",
  "timestamp": "2026-01-25T12:00:00"
}
```

### 실패 응답
```json
{
  "success": false,
  "error": {
    "code": "ERROR_CODE",
    "message": "오류 메시지"
  },
  "timestamp": "2026-01-25T12:00:00"
}
```

---

## 🔒 권한별 API 접근

### 인증 불필요
- 인기 검색어 조회
- ElasticSearch 자동완성
- 인기 도서 추천
- 평점 높은 도서 추천

### USER 권한
- 대부분의 API (장바구니, 주문, 리뷰, 배송지 등)

### ADMIN 권한
- ElasticSearch 초기 데이터 적재
- 주문/매출 통계 조회

---

## 📌 참고 사항

1. **페이지네이션**: 현재 대부분의 조회 API는 limit 파라미터만 지원하며, offset 기반 페이징은 구현되지 않음
2. **정렬**: 각 API마다 기본 정렬 기준이 다르며, 커스텀 정렬 파라미터는 제공하지 않음
3. **필터링**: 검색 API는 ElasticSearch를 통해 제공되며, 일반 조회 API는 기본적인 필터링만 지원
4. **Rate Limiting**: 현재 API Rate Limiting은 구현되지 않음
5. **CORS**: 개발 환경에서는 모든 Origin 허용, 운영 환경에서는 화이트리스트 설정 필요

---

**마지막 업데이트**: 2026년 1월 25일
