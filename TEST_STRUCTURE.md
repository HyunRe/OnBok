# 테스트 구조 가이드

## 테스트 분리 전략

테스트를 **단위 테스트(Unit Test)**와 **통합 테스트(Integration Test)**로 분리하여 관리합니다.

```
src/
├── test-unit/              # 단위 테스트 (빠른 실행)
│   ├── java/
│   │   └── com/onbok/book_hub/
│   │       ├── BookStockTest.java
│   │       ├── CartTest.java
│   │       ├── CartRepositoryTest.java
│   │       ├── CartCalculationServiceTest.java
│   │       ├── DeliveryAddressTest.java
│   │       ├── DeliveryAddressRepositoryTest.java
│   │       ├── ImageRepositoryTest.java
│   │       ├── OrderRepositoryTest.java
│   │       ├── RecommendationServiceTest.java
│   │       ├── ReviewRepositoryTest.java
│   │       ├── TossPaymentTest.java
│   │       ├── BookEsServiceTest.java
│   │       └── OAuth2UserServiceTest.java
│   └── resources/
│       └── application.yaml
│
└── test-integration/       # 통합 테스트 (느린 실행)
    ├── java/
    │   └── com/onbok/book_hub/
    │       ├── DeliveryAddressIntegrationTest.java
    │       ├── ElasticsearchIntegrationTest.java
    │       ├── ImageLocalUploadTest.java
    │       ├── ImageS3ServiceTest.java
    │       ├── OrderIntegrationTest.java
    │       ├── ReviewIntegrationTest.java
    │       └── TransactionStabilityTest.java
    └── resources/
        └── application.yaml
```

---

## 테스트 종류별 특징

### 1. 단위 테스트 (Unit Test)

**위치**: `src/test-unit/java`

**특징**:
- **빠른 실행 속도** (~3-5배 빠름)
- **격리된 테스트**: Mock, Stub 활용
- **의존성 최소화**: 데이터베이스만 사용 (H2)

**어노테이션**:
- `@DataJpaTest` - Repository 계층 테스트
- `@ExtendWith(MockitoExtension.class)` - Service 계층 테스트 (Mock 기반)
- 일반 JUnit 테스트 - Domain 객체 테스트

**실행 명령어**:
```bash
./gradlew unitTest
```

**포함된 테스트**:
- Repository 테스트 (JPA 쿼리 검증)
- Domain 객체 테스트 (비즈니스 로직 검증)
- Service 테스트 (Mock 기반 단위 테스트)

**예시**:
```java
@DataJpaTest
@DisplayName("Cart Repository 계층 테스트")
class CartRepositoryTest {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void saveCart_success() {
        // Repository 계층만 테스트
    }
}
```

---

### 2. 통합 테스트 (Integration Test)

**위치**: `src/test-integration/java`

**특징**:
- **느린 실행 속도**: 전체 Spring Context 로드
- **실제 환경 시뮬레이션**: 모든 빈 로드
- **E2E 테스트**: 여러 계층이 통합된 시나리오 검증

**어노테이션**:
- `@SpringBootTest` - 전체 애플리케이션 컨텍스트
- `@Transactional` - 테스트 후 자동 롤백
- `@Testcontainers` - Docker 컨테이너 사용 (ElasticSearch 등)

**실행 명령어**:
```bash
./gradlew integrationTest
```

**포함된 테스트**:
- 여러 계층이 통합된 시나리오 테스트
- 트랜잭션 안정성 테스트
- ElasticSearch 통합 테스트
- 파일 업로드 통합 테스트

**예시**:
```java
@SpringBootTest
@Transactional
@DisplayName("Order 통합 테스트")
class OrderIntegrationTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private CartService cartService;

    @Test
    void createOrder_fromCart_success() {
        // 여러 서비스가 통합된 시나리오 테스트
    }
}
```

---

## Gradle 태스크

### 단위 테스트만 실행
```bash
./gradlew unitTest
```

### 통합 테스트만 실행
```bash
./gradlew integrationTest
```

### 모든 테스트 실행 (단위 + 통합)
```bash
./gradlew test
```

### 빌드 (테스트 포함)
```bash
./gradlew build
```

### 테스트 없이 빌드
```bash
./gradlew build -x test
```

---

## 테스트 작성 가이드

### 단위 테스트 작성 시 체크리스트

- [ ] 테스트 파일 위치: `src/test-unit/java/com/onbok/book_hub/`
- [ ] 외부 의존성 최소화 (Mock 활용)
- [ ] `@DataJpaTest` 또는 `@ExtendWith(MockitoExtension.class)` 사용
- [ ] 테스트 메서드명: `메서드명_조건_예상결과` 형식
- [ ] `@DisplayName`으로 한글 설명 추가

**예시 - Repository 단위 테스트**:
```java
@DataJpaTest
@DisplayName("User Repository 계층 테스트")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("이메일로 사용자 조회 - 성공")
    void findByEmail_success() {
        // given
        User user = User.builder()
            .email("test@example.com")
            .build();
        userRepository.save(user);

        // when
        Optional<User> found = userRepository.findByEmail("test@example.com");

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("test@example.com");
    }
}
```

**예시 - Service 단위 테스트 (Mock)**:
```java
@ExtendWith(MockitoExtension.class)
@DisplayName("User Service 단위 테스트")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("사용자 등록 - 성공")
    void registerUser_success() {
        // given
        User user = User.builder().email("test@example.com").build();
        when(userRepository.save(any(User.class))).thenReturn(user);

        // when
        User registered = userService.register(user);

        // then
        assertThat(registered.getEmail()).isEqualTo("test@example.com");
        verify(userRepository, times(1)).save(any(User.class));
    }
}
```

---

### 통합 테스트 작성 시 체크리스트

- [ ] 테스트 파일 위치: `src/test-integration/java/com/onbok/book_hub/`
- [ ] `@SpringBootTest` 사용
- [ ] `@Transactional` 추가 (자동 롤백)
- [ ] 여러 계층이 통합된 시나리오 검증
- [ ] `@DisplayName`으로 한글 설명 추가

**예시 - 통합 테스트**:
```java
@SpringBootTest
@Transactional
@DisplayName("주문 통합 테스트")
class OrderIntegrationTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookRepository bookRepository;

    @Test
    @DisplayName("주문 생성 - 재고 감소 확인")
    void createOrder_decreasesStock() {
        // given: 사용자와 책 준비
        User user = userRepository.save(
            User.builder().email("test@example.com").build()
        );
        Book book = bookRepository.save(
            Book.builder().title("테스트 책").stock(10).build()
        );

        // when: 주문 생성
        Order order = orderService.createOrder(user.getId(), book.getId(), 3);

        // then: 재고 감소 확인
        Book updatedBook = bookRepository.findById(book.getId()).get();
        assertThat(updatedBook.getStock()).isEqualTo(7);
    }
}
```

---

## 테스트 속도 최적화

### 단위 테스트 우선 실행
개발 중에는 단위 테스트만 먼저 실행하여 빠른 피드백을 받습니다:
```bash
./gradlew unitTest
```

### CI/CD 파이프라인
```yaml
# GitHub Actions 예시
jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3

      # 단위 테스트 먼저 실행 (빠름)
      - name: Run Unit Tests
        run: ./gradlew unitTest

      # 단위 테스트 통과 후 통합 테스트 실행
      - name: Run Integration Tests
        run: ./gradlew integrationTest
```

---

## 성능 비교

| 테스트 종류 | 실행 시간 | Context 로드 | 사용 사례 |
|------------|----------|--------------|----------|
| 단위 테스트 | ~5초 | ❌ 없음 | 개발 중 빠른 피드백 |
| 통합 테스트 | ~20초 | ✅ 전체 | 배포 전 최종 검증 |

---

## 테스트 커버리지 확인

### JaCoCo 플러그인 사용 (추후 추가 가능)
```gradle
plugins {
    id 'jacoco'
}

test {
    finalizedBy jacocoTestReport
}

jacocoTestReport {
    dependsOn test
    reports {
        html.required = true
        xml.required = true
    }
}
```

실행:
```bash
./gradlew test jacocoTestReport
open build/reports/jacoco/test/html/index.html
```

---

## 참고 자료

- [Spring Boot Testing Best Practices](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.testing)
- [JUnit 5 User Guide](https://junit.org/junit5/docs/current/user-guide/)
- [Mockito Documentation](https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html)
- [AssertJ Documentation](https://assertj.github.io/doc/)
