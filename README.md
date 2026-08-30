# ONBOK (검색 품질 & 결제 신뢰성 중심 고성능 온라인 서점)

Elasticsearch 기반의 한글 정밀 검색과 Toss Payments 및 동시성 제어로 결제 정합성을 보장하는 이커머스 백엔드

## 1. 프로젝트 개요

### 1-1. 기획 배경

* **한글 검색의 한계**: 기존 RDBMS LIKE 검색은 데이터 증가 시 성능이 급격히 저하되며, 조사/어미가 발달한 한글 특성상 형태소 분석 없이 정밀한 검색 결과를 제공하기 어렵습니다.
* **이커머스 결제/재고 정합성 이슈**: 대규모 동시 주문 시 발생할 수 있는 재고 수량 불일치(Race Condition) 및 클라이언트 네트워크 불안정으로 인한 결제 상태 오차 리스크를 구조적으로 차단하고자 했습니다.
* **해결 방향**: Elasticsearch + Nori 분석기 기반의 가중치 랭킹 검색 아키텍처, 낙관적 락(@Version)을 활용한 재고 동시성 제어, 그리고 Webhook 기반의 Toss Payments 결제 상태 머신을 도입하여 대용량 처리 환경에서도 안정적인 서점 플랫폼을 구현했습니다.

### 1-2. 프로젝트 목표

* Nori 형태소 분석기 및 Multi-field 가중치 검색으로 검색 결과 최상단 노출 정확도 향상
* 낙관적 락 적용으로 성능 저하 없이 동시 주문 시 재고 정합성 100% 보장
* Toss Payments Webhook 및 Enum 상태 머신 설계를 통한 결제 불일치 0건 달성
* 외부 API 호출을 트랜잭션 밖으로 격리하여 DB 커넥션 점유 최소화 (PaymentOrderFacade 패턴)
* Elasticsearch Near-Real-Time(NRT) 지연 특성 제어로 통합 테스트 성공 신뢰성 100% 확보

### 1-3. 기술 스택

* **Core**: Java 17, Spring Boot 3.x, Spring Data JPA
* **Security & Auth**: Spring Security, JWT, OAuth2(Google, Naver, GitHub)
* **Payment & Engine**: Toss Payments API, Elasticsearch (Nori Analyzer)
* **Database**: MariaDB
* **Infrastructure**: Docker
* **Test**: JUnit5, Mockito, AssertJ, Testcontainers
* **Docs**: Swagger 

### 1-4. 시스템 아키텍처 및 CI/CD 파이프라인

**[시스템 아키텍처]**

```text
  [ Users & Clients ]
         │ (HTTPS / REST API)
         ▼
    [ Spring Boot Backend Server ]
   ┌────────────────────────────────────────────────────────┐
   │  - Core API / Auth / Business Logic                    │
   │  - PaymentOrderFacade (External Transaction Boundary)  │
   └──────┬──────────────────┬────────────────────┬─────────┘
          │                  │                    │
          ▼                  ▼                    ▼
    [ MariaDB ]       [ Elasticsearch ]    [ Toss Payments API ]
  (Primary RDBMS)    (Nori Tokenizer)      (Payment Approval)
```

## 2. 도메인 및 구조 설계

### 2-1. 패키지 및 프로젝트 구조

```text
com.onbok.book_hub
├── common             # Security, Elasticsearch, TossPayments 설정 및 Global Error
└── domain
    ├── user           # 회원 및 OAuth2 로그인
    ├── book           # 도서 조회 및 Elasticsearch Nori 매핑
    ├── order          # 주문, 재고 관리 (@Version) 및 상태 머신
    ├── payment        # PaymentOrderFacade, Toss Payments Webhook 처리
    ├── cart           # 장바구니 도메인
    ├── delivery       # 배송 상태 및 정보 관리
    ├── image          # 도서 이미지 관리
    ├── recommendation # 협업 필터링 기반 도서 추천
    └── review         # 도서 리뷰 및 평점 관리
```

### 2-2. 핵심 상태 관리 (OrderStatus Enum 기반 상태 머신)

```text
[PENDING] (주문 생성)
   │
   ├──► [PAYMENT_COMPLETED] (결제 완료)
   │          │
   │          └──► [PREPARING] (상품 준비 중)
   │                    │
   │                    └──► [SHIPPED]
   │                              │
   │                              └──► [DELIVERED]
   │
   └──► [CANCELLED] ◄──────────────┴──► [REFUNDED] (결제 취소 & 재고 자동 복구)
```

## 3. 핵심 기능 및 담당 도메인

### 3-1. 핵심 기능 요약

* **Elasticsearch 정밀 검색**: Nori 형태소 분석, 필드별 가중치(제목 > 저자 > 요약), 오타 보정 Fuzzy Query
* **낙관적 락 재고 제어**: @Version 기반 동시성 제어로 동시 주문 시 정합성 유지
* **외부 결제 연동 & Webhook**: Toss Payments 승인 및 Webhook 연동을 통한 결제 상태 자동 동기화
* **협업 필터링 추천**: 유저 구매 이력 기반 맞춤형 도서 개인화 추천


| **구분**     | **설명**              | **점수 계산 / 처리 방식** |
| ---------- | ------------------- | ----------------- |
| **구매 기반**  | 구매 이력의 동일 저자/출판사 도서 | 저자 +3, 출판사 +1     |
| **리뷰 기반**  | 4점 이상 평가한 도서의 동일 저자 | +5점               |
| **협업 필터링** | 유사 사용자(공통 구매) 기반 추천 | 유사도 × 가중치         |
| **인기 도서**  | 전체 주문 수량 기준         | 주문량 순             |
| **고평점 도서** | 평균 평점 기준 (최소 3개 리뷰) | 평점 순              |

### 3-2. 핵심 비즈니스 로직

| **구분**        | **비즈니스 규칙 및 지표**                      | **처리 방식**                                                                  |
| ------------- | ------------------------------------- | -------------------------------------------------------------------------- |
| **검색 랭킹**     | 키워드 검색 시 단순 본문 매칭보다 제목/저자 매칭 우선 노출    | Multi-field 매핑 기반 가중치 랭킹: 제목(2.0), 저자(1.5), 요약(1.0) 설정                     |
| **재고 정합성**    | 동시에 동일 도서 주문 시 재고 음수 처리 방지            | 엔티티 내 @Version 낙관적 락 적용 및 충돌 시 예외 재시도 안내                                 |
| **결제 불일치 방지** | 네트워크 장애로 결제창 종료 후 유저 이탈 시 주문 상태 멈춤 차단 | Toss Webhook 체계로 외부 결제 상태 변화를 수신하여 PAYMENT_COMPLETED / CANCELLED 자동 전환 |

### 3-3. 담당 영역 및 역할

* **개인 프로젝트 (100% 기여**

  * **도메인 설계 및 검색 구축**: Elasticsearch Nori 분석기 튜닝 및 가중치 랭킹 알고리즘 구현
  * **트랜잭션 및 결제 아키텍처**: PaymentOrderFacade 패턴과 TransactionTemplate으로 외부 API 호출을 트랜잭션 밖으로 분리하고 Webhook 상태 머신 구축
  * **통합 테스트 환경 자동화**: Testcontainers 환경에 analysis-nori 동적 주입 및 백엔드 전 과정 테스트 자동화

## 4. 엔지니어링 문제 해결 및 회고

### 4-1. 성능 개선 및 구조 최적화

| **개선 항목**        | **개선 전**                                      | **개선 후**                            | **정성적 / 정량적 효과**                     |
| ---------------- | --------------------------------------------- | ----------------------------------- | ------------------------------------ |
| **한글 검색 정확도**    | RDBMS LIKE 단순 검색 (조사 포함, 오타 미보정)              | Nori 형태소 분석 + Multi-field 가중치 튜닝    | 검색 결과 최상단 노출 정확도 대폭 개선, 오타 보정 유연성 확보 |
| **동시 주문 재고 정합성** | 별도 락 제어 없음 (Race Condition 발생 시 재고 음수)        | 엔티티 @Version 낙관적 락 적용             | 별도 DB Lock 점유 없이 재고 정합성 100% 보장      |
| **결제 상태 불일치**    | 유저 브라우저 응답에 의존 ('결제 대기' 멈춤 리스크)               | Toss Webhook + Enum 상태 머신 설계        | 외부 API 연동 불확실성 제거, 결제 불일치 0건 달성      |
| **통합 테스트 안정성**   | NRT 특성으로 인한 Thread.sleep 임시 대기 (Flaky Test) | Elasticsearch _refresh API 명시적 호출 | 테스트 신뢰성 100% 확보 및 전체 빌드 시간 대폭 단축     |

### 4-2. 기술 트러블슈팅

#### 1) PaymentOrderFacade 도입을 통한 외부 API 트랜잭션 분리

* **현상 (Problem)**

  * Toss Payments 결제 승인 API 연동 중 외부 네트워크 지연이 발생하면, 백엔드 서버의 DB 커넥션 풀(HikariCP)이 고갈되면서 다른 모든 API 요청까지 마비되는 시스템 전체 장애 현상을 확인했습니다.
* **원인 (Root Cause)**

  * 기존 구조에서는 @Transactional로 묶인 단일 트랜잭션 범위 안에서 외부 결제 승인 API(HTTP 요청)를 직접 호출하고 있었습니다.
  * 외부 API의 응답을 기다리는 동안 DB 커넥션을 반납하지 못하고 잡고 있게 되어, 네트워크 지연 시간이 그대로 DB 커넥션 점유 시간으로 이어지는 구조적 문제가 원인이었습니다.
* **해결 (Solution)**

  * PaymentOrderFacade 패턴과 TransactionTemplate을 도입하여 **트랜잭션의 경계를 물리적으로 분리**했습니다.
  * **[1단계]** DB에 PENDING 상태의 주문 생성 (짧은 DB 트랜잭션) ➔ **[2단계]** external Toss API 호출 (트랜잭션 밖에서 수행하여 DB 커넥션 미점유) ➔ **[3단계]** 결제 결과 DB 반영 (짧은 DB 트랜잭션) 형태로 구조를 바꿨습니다.
  * 만약 2단계 승인 성공 후 3단계 DB 반영 중 예외가 발생할 경우, DB 롤백만으로는 이미 승인된 결제를 되돌릴 수 없으므로 Toss 결제 취소 API를 호출하는 보상 트랜잭션compensate())을 구현하여 데이터 일관성을 유지했습니다.
* **결과 (Impact)**

  * 외부 API 요청 지연이 발생하더라도 DB 커넥션을 점유하지 않도록 완벽히 격리하여, 시스템 전체의 처리량(Throughput)과 안정성을 확보했습니다.

#### 2) Testcontainers 환경 내 Elasticsearch Nori 플러그인 누락 해결

* **현상 (Problem)**

  * 로컬 및 CI/CD 환경에서 독립적인 테스트를 수행하기 위해 Testcontainers로 Elasticsearch 컨테이너를 구동해 통합 테스트를 실행하면, Unknown tokenizer type [nori_tokenizer] 예외가 발생하며 한글 검색 관련 모든 테스트가 실패했습니다.
* **원인 (Root Cause)**

  * Docker Hub에서 제공하는 공식 Elasticsearch 기본 이미지에는 한국어 형태소 분석기인 analysis-nori 플러그인이 포함되어 있지 않기 때문이었습니다.
* **해결 (Solution)**

  * 별도의 커스텀 Docker Image를 매번 만들어 올리는 번거로움을 피하기 위해, Testcontainers 구동 시 **컨테이너 실행 명령(Command)을 동적으로 주입하는 설정 클래스**를 구현했습니다.
  * withCommand("sh", "-c", "bin/elasticsearch-plugin install analysis-nori && bin/elasticsearch") 명령을 주입하여, 컨테이너 셋업 시점에 Nori 플러그인을 자동으로 다운로드 및 설치한 후 ES 엔진이 켜지도록 환경을 구현했습니다.
* **결과 (Impact)**

  * 별도의 외부 커스텀 이미지 관리 없이, 표준 Docker 환경만 갖춰져 있다면 CI/CD 및 로컬 어디서나 운영 환경과 100% 동일한 Nori 형태소 분석기 기반의 통합 테스트를 100% 자동화하는 데 성공했습니다.

#### 3) Elasticsearch Near-Real-Time(NRT) 지연과 _refresh API 기반 테스트 Flaky 해결

* **현상 (Problem)**

  * Elasticsearch에 도서 데이터를 색인(Index/Update)한 직후 곧바로 조회하는 통합 테스트를 실행할 때, 데이터가 즉시 검색되지 않아 간헐적으로 성공/실패를 반복하는 Flaky Test 현상이 발생했습니다.
  * 이를 임시로 회피하기 위해 테스트 코드 곳곳에 Thread.sleep(1000) 대기 로직을 작성했으나, 이로 인해 전체 테스트 빌드 시간이 크게 증가하는 문제가 생겼습니다.
* **원인 (Root Cause)**

  * Elasticsearch는 데이터를 색인할 때 메모리 버퍼에 임시 저장한 후, 기본 1초 주기의 Refresh 작업을 거쳐 Lucene Segment 디스크 캐시로 넘어가야만 실제 검색 가능(Searchable) 상태가 되는 Near-Real-Time(NRT) 아키텍처 특성을 가집니다.
* **해결 (Solution)**

  * 테스트 코드 내 불필요하고 불확실한 대기 시간(Thread.sleep)을 전면 제거하고, ES의 인덱싱 동작을 명시적으로 제어하도록 변경했습니다.
  * 테스트 전용 Helper 메서드를 작성하여, 테스트 데이터를 생성/수정하는 즉시 Elasticsearch RestClient를 통해 해당 인덱스에 **POST /{index}/_refresh** API를 명시적으로 호출하여 Segment를 강제로 생성시켰습니다.
* **결과 (Impact)**

  * 데이터 색인 직후 100% 즉시 검색이 가능한 상태로 확정 지음으로써 **통합 테스트의 성공 신뢰성을 100% 확보**했고, 불필요한 대기 로직을 없애 **전체 테스트 빌드 수행 시간을 대폭 단축**했습니다.

### 4-3. 프로젝트 회고 및 성장 포인트

* **트랜잭션 경계와 외부 의존성의 명확한 분리**

  * @Transactional 안에 외부 API 호출을 무심코 작성했을 때 DB 커넥션 풀이 어떻게 고갈되는지 직접 경험하며, 백엔드 아키텍처에서 '외부 시스템 연동 시 트랜잭션 경계를 어디까지 잡아야 하는가'에 대한 명확한 기준을 세웠습니다. Facade 패턴과 TransactionTemplate을 결합해 보상 트랜잭션까지 고려하는 분산 처리 기본기를 다졌습니다.
* **데이터 무결성을 최우선으로 하는 시스템 설계**

  * 이커머스에서 가장 중요한 재고 및 결제 정합성을 지키기 위해, @Version 기반 낙관적 락과 Toss Webhook 체계를 결합했습니다. 클라이언트의 브라우저 이탈이나 네트워크 불안정 등 다양한 예외 상황에서도 단 1건의 결제 불일치나 재고 오차가 발생하지 않도록 정교하게 설계했습니다.
* **기술의 내부 동작 원리(Internal Mechanics)에 기반한 문제 해결**

  * Elasticsearch 통합 테스트 실패 문제를 해결하는 과정에서 단순히 Thread.sleep으로 때우는 임시방편이 아닌, ES의 Near-Real-Time(NRT) 인덱싱 및 Segment Refresh 메커니즘을 파악했습니다. 문제의 근본 원인을 이해하고 엔진 명시적 API (_refresh)로 원인을 제거함으로써 신뢰할 수 있는 테스트 환경을 완성했습니다.

## 5. 테스트 전략

* **Testcontainers 통합 테스트**: Nori 플러그인이 동적 설치된 Elasticsearch Docker 컨테이너를 테스트 환경에서 동적 구동.
* **검증 범위**: @Version 낙관적 락 충돌 시 예외 처리 검증, Toss Webhook 처리 시 상태 전환 검증, Nori 가중치 랭킹 쿼리 검증, _refresh API를 활용한 색인 직후 검색 정확도 검증.

## 6. 실행 방법 (Local Run)

```bash
# 1. Repository Clone
$ git clone <https://github.com/HyunRe/ONBOK.git>
$ cd ONBOK

# 2. Infra Containers Run (MariaDB, Elasticsearch with Nori)
$ docker-compose up -d

# 3. Application Run
$ ./gradlew bootRun
```
