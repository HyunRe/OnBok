# 📚 OnBok Book-Hub

온라인 서점 플랫폼 - Spring Boot 기반의 도서 구매 및 추천 시스템

## 🚀 프로젝트 소개

OnBok Book-Hub는 사용자가 도서를 검색하고, 구매하고, 리뷰를 작성할 수 있는 종합 온라인 서점 플랫폼입니다.

### 주요 기능

- 📖 **도서 관리**: 도서 검색, 조회, 재고 관리
- 🔍 **ElasticSearch 검색**: Nori 형태소 분석기를 활용한 한글 검색
- 🛒 **장바구니 & 주문**: 실시간 재고 관리와 주문 처리
- 💳 **Toss Payments 연동**: 안전한 결제 시스템
- ⭐ **리뷰 시스템**: 별점 및 후기 작성
- 🤖 **추천 알고리즘**: 협업 필터링 기반 도서 추천
- 🔐 **OAuth2 소셜 로그인**: Google, Naver, GitHub 로그인 지원
- 📊 **통계 대시보드**: 주문 및 매출 통계 (관리자)
- 📦 **배송지 관리**: 여러 배송지 등록 및 관리

## 🛠️ 기술 스택

### Backend
- **Java 17**
- **Spring Boot 3.x**
- **Spring Data JPA** (Hibernate)
- **Spring Security** (JWT, OAuth2)
- **MariaDB/MySQL**
- **ElasticSearch 8.x**

### Frontend
- **Thymeleaf**
- **Bootstrap 5**
- **JavaScript/jQuery**

### Tools
- **Gradle**
- **Swagger/OpenAPI**
- **Lombok**

## 📖 문서

- **[SETUP.md](SETUP.md)**: 개발 환경 설정 가이드
- **[TESTING.md](TESTING.md)**: 테스트 코드 가이드
- **[CHANGELOG.md](CHANGELOG.md)**: 변경 이력

## 🚀 빠른 시작

### 1. 필수 프로그램 설치
- JDK 17 이상
- MariaDB 10.x
- ElasticSearch 8.x

### 2. 데이터베이스 생성
```sql
CREATE DATABASE book_hub CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 3. 설정 파일 생성
```bash
cp src/main/resources/application-secret.yaml.example src/main/resources/application-secret.yaml
# application-secret.yaml 파일을 편집하여 DB 비밀번호, API 키 등 설정
```

### 4. 애플리케이션 실행
```bash
./gradlew bootRun
```

### 5. 브라우저에서 접속
```
http://localhost:8080
```

**자세한 설정 방법은 [SETUP.md](SETUP.md)를 참고하세요.**

## 🧪 테스트

```bash
# 전체 테스트 실행
./gradlew test

# 테스트 리포트 확인
open build/reports/tests/test/index.html
```

**테스트 작성 가이드는 [TESTING.md](TESTING.md)를 참고하세요.**

## 📁 프로젝트 구조

```
book-hub/
├── src/
│   ├── main/
│   │   ├── java/com/onbok/book_hub/
│   │   │   ├── book/              # 도서 관리
│   │   │   ├── cart/              # 장바구니
│   │   │   ├── order/             # 주문 관리
│   │   │   ├── payment/           # 결제 (Toss Payments)
│   │   │   ├── delivery/          # 배송지 관리
│   │   │   ├── review/            # 리뷰
│   │   │   ├── recommendation/    # 추천 알고리즘
│   │   │   ├── user/              # 사용자 관리
│   │   │   └── common/            # 공통 (Security, Exception 등)
│   │   └── resources/
│   │       ├── templates/         # Thymeleaf 템플릿
│   │       ├── static/            # CSS, JS, 이미지
│   │       └── application*.yaml  # 설정 파일
│   └── test/                      # 테스트 코드
├── SETUP.md                       # 설정 가이드
├── TESTING.md                     # 테스트 가이드
├── CHANGELOG.md                   # 변경 이력
└── README.md                      # 프로젝트 소개 (이 파일)
```

## 🎯 주요 아키텍처 패턴

### 1. CQS (Command-Query Separation)
- `CommandService`: 데이터 변경 (Create, Update, Delete)
- `QueryService`: 데이터 조회 (Read)

### 2. 도메인 주도 설계 (DDD)
```
domain/
├── model/         # 엔티티 및 도메인 로직
└── repository/    # 리포지토리 인터페이스

application/       # 서비스 레이어
presentation/      # 컨트롤러 레이어
```

### 3. 낙관적 락 (Optimistic Locking)
- `@Version` 어노테이션을 사용한 동시성 제어
- 재고 관리에서 활용

## 🔐 보안

- **Spring Security**: 인증 및 인가
- **JWT**: Stateless 인증
- **OAuth2**: 소셜 로그인 (Google, Naver, GitHub)
- **CSRF 보호**: 폼 기반 공격 방어
- **BCrypt**: 비밀번호 암호화

## 📊 API 문서

애플리케이션 실행 후:
- Swagger UI: http://localhost:8080/swagger-ui.html
- API Docs: http://localhost:8080/v3/api-docs

## 🌟 주요 기능 상세

### 1. ElasticSearch 검색
- Nori 형태소 분석기를 활용한 한글 검색
- 제목, 저자, 출판사 검색
- 자동완성 지원

### 2. 재고 관리
- 낙관적 락을 사용한 동시성 제어
- 주문 시 자동 재고 감소
- 주문 취소/환불 시 재고 복구

### 3. 주문 상태 관리
```
PENDING → PAYMENT_COMPLETED → PREPARING → SHIPPED → DELIVERED
         ↓                                              ↓
      CANCELLED                                    REFUNDED
```

### 4. 추천 시스템
- 협업 필터링 알고리즘
- 유사 사용자 기반 추천
- 리뷰 데이터 활용

### 5. 배송지 관리
- 여러 배송지 등록 및 관리
- 별칭(집, 회사 등) 설정
- 기본 배송지 선택

## 🤝 기여 방법

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📝 라이센스

이 프로젝트는 MIT 라이센스를 따릅니다.

## 👥 개발자

- **OnBok Team**

## 📞 문의

- GitHub Issues: [이슈 등록](https://github.com/your-repo/book-hub/issues)
- Email: your-email@example.com

---

**마지막 업데이트**: 2026년 1월 22일
