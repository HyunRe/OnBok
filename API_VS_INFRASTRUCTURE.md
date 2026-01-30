# API vs Infrastructure 테스트 분류 가이드

## 핵심 차이점

### 📡 api/ - API 계층 테스트
**"HTTP 요청/응답"을 테스트합니다**

- **사용 어노테이션**: `@WebMvcTest`, `@AutoConfigureMockMvc`
- **테스트 도구**: `MockMvc`, `@MockBean`
- **테스트 대상**: **Controller** (API 엔드포인트)
- **검증 내용**: HTTP 상태 코드, JSON 응답, 요청 매핑, 파라미터 검증

### 🏗️ infrastructure/ - Infrastructure 계층 테스트
**"비즈니스 플로우 + 외부 시스템 연동"을 테스트합니다**

- **사용 어노테이션**: `@SpringBootTest`, `@Transactional`
- **테스트 도구**: 실제 Service, Repository 빈
- **테스트 대상**: **Service + Repository + 외부 시스템**
- **검증 내용**: 비즈니스 로직, 트랜잭션, 데이터베이스, 파일 시스템, S3, ElasticSearch

---

## 현재 infrastructure/ 테스트들이 api/에 들어갈 수 없는 이유

### 1. ImageLocalUploadTest.java ❌ api/

**실제 코드**:
```java
@SpringBootTest
@Transactional
class ImageLocalUploadTest {
    @Autowired
    private ImageService imageService;  // ← Service 직접 테스트

    @Test
    void uploadLocalImage_success() {
        // MockMultipartFile 생성
        MockMultipartFile file = new MockMultipartFile(...);

        // Service 직접 호출
        Image image = imageService.uploadImage(file);

        // 파일 시스템 검증
        assertThat(new File(uploadPath).exists()).isTrue();
    }
}
```

**왜 api/가 아닌가?**
- ❌ Controller를 테스트하지 않음
- ❌ HTTP 요청을 보내지 않음 (MockMvc 없음)
- ✅ Service 계층 직접 테스트
- ✅ 파일 시스템(infrastructure) 연동 검증

**api/에 들어가려면 이렇게 해야 함**:
```java
@WebMvcTest(ImageController.class)  // Controller 테스트
class ImageControllerTest {
    @Autowired
    private MockMvc mockMvc;  // HTTP 요청 테스트

    @MockBean
    private ImageService imageService;  // Service는 Mock으로

    @Test
    void uploadImage_success() throws Exception {
        // HTTP 요청 테스트
        mockMvc.perform(multipart("/api/images")
                .file(mockFile))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.imageUrl").exists());
    }
}
```

---

### 2. OrderIntegrationTest.java ❌ api/

**실제 코드**:
```java
@SpringBootTest
@Transactional
class OrderIntegrationTest {
    @Autowired
    private OrderCommandService orderCommandService;  // ← Service 직접 테스트

    @Autowired
    private BookRepository bookRepository;

    @Test
    void createOrder_decreasesStock() {
        // Service 직접 호출
        Order order = orderCommandService.createOrder(userId, cartItems);

        // DB 트랜잭션 검증
        Book book = bookRepository.findById(bookId).get();
        assertThat(book.getStock()).isEqualTo(7);  // 재고 감소 확인
    }
}
```

**왜 api/가 아닌가?**
- ❌ Controller를 테스트하지 않음
- ❌ HTTP 요청을 보내지 않음
- ✅ Service + Repository 통합 테스트
- ✅ 비즈니스 플로우 전체 검증
- ✅ 트랜잭션 안정성 검증

**api/에 들어가려면 이렇게 해야 함**:
```java
@WebMvcTest(OrderController.class)
class OrderControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderCommandService orderCommandService;

    @Test
    void createOrder_success() throws Exception {
        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"userId\":1,\"items\":[...]}"))
               .andExpect(status().isCreated())
               .andExpect(jsonPath("$.orderId").exists());
    }
}
```

---

### 3. TransactionStabilityTest.java ❌ api/

**실제 코드**:
```java
@SpringBootTest
@DisplayName("트랜잭션 안정성 테스트")
class TransactionStabilityTest {
    @Test
    void multipleThreads_concurrency() {
        // 동시성 테스트
        // 트랜잭션 격리 수준 테스트
        // 데드락 테스트
    }
}
```

**왜 api/가 아닌가?**
- ❌ Controller와 무관
- ❌ HTTP와 무관
- ✅ 데이터베이스 트랜잭션 안정성 테스트
- ✅ Infrastructure 계층 테스트

---

## 비교표

| 테스트 | 현재 위치 | api/에 적합? | 이유 |
|--------|----------|-------------|------|
| ImageLocalUploadTest | infrastructure/ | ❌ | Service + 파일 시스템 테스트 |
| ImageS3ServiceTest | infrastructure/ | ❌ | Service + S3 연동 테스트 |
| OrderIntegrationTest | infrastructure/ | ❌ | Service + 비즈니스 플로우 테스트 |
| ReviewIntegrationTest | infrastructure/ | ❌ | Service + 트랜잭션 테스트 |
| DeliveryAddressIntegrationTest | infrastructure/ | ❌ | Service + DB 통합 테스트 |
| TransactionStabilityTest | infrastructure/ | ❌ | 트랜잭션 안정성 테스트 |
| ElasticsearchIntegrationTest | infrastructure/ | ❌ | ElasticSearch 연동 테스트 |

---

## api/에 들어가야 하는 테스트 예시 (추후 작성)

### 1. BookControllerTest ✅ api/

```java
@WebMvcTest(BookController.class)
class BookControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookService bookService;

    @Test
    @DisplayName("GET /api/books - 도서 목록 조회")
    void getBooks_success() throws Exception {
        // given
        List<BookDto> books = List.of(new BookDto(...));
        when(bookService.findAll()).thenReturn(books);

        // when & then
        mockMvc.perform(get("/api/books"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$[0].title").value("자바의 정석"));
    }

    @Test
    @DisplayName("POST /api/books - 도서 등록 (검증 실패)")
    void createBook_validationFail() throws Exception {
        mockMvc.perform(post("/api/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"\"}"))  // 빈 제목
               .andExpect(status().isBadRequest())
               .andExpect(jsonPath("$.errors[0].field").value("title"));
    }
}
```

### 2. OrderControllerTest ✅ api/

```java
@WebMvcTest(OrderController.class)
class OrderControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService orderService;

    @Test
    @DisplayName("POST /api/orders - 주문 생성")
    void createOrder_success() throws Exception {
        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"userId\":1,\"items\":[{\"bookId\":1,\"quantity\":2}]}"))
               .andExpect(status().isCreated())
               .andExpect(header().exists("Location"))
               .andExpect(jsonPath("$.orderId").exists());
    }

    @Test
    @DisplayName("GET /api/orders/{id} - 주문 상세 조회")
    void getOrder_success() throws Exception {
        mockMvc.perform(get("/api/orders/1"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.orderId").value(1))
               .andExpect(jsonPath("$.status").value("PENDING"));
    }
}
```

### 3. UserControllerTest ✅ api/

```java
@WebMvcTest(UserController.class)
class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Test
    @DisplayName("POST /api/users/register - 회원가입")
    void register_success() throws Exception {
        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"test@test.com\",\"password\":\"1234\"}"))
               .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("POST /api/users/login - 로그인 실패 (잘못된 비밀번호)")
    void login_wrongPassword() throws Exception {
        mockMvc.perform(post("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"test@test.com\",\"password\":\"wrong\"}"))
               .andExpect(status().isUnauthorized());
    }
}
```

---

## 요약: 분류 기준

### ✅ api/ 에 들어가는 테스트
- Controller를 테스트
- MockMvc를 사용
- HTTP 요청/응답 검증
- @WebMvcTest 사용
- Service는 @MockBean으로 Mock 처리

**핵심**: "HTTP 인터페이스 테스트"

### ✅ infrastructure/ 에 들어가는 테스트
- Service를 직접 테스트
- 실제 빈을 사용
- 비즈니스 플로우 전체 검증
- @SpringBootTest 사용
- 외부 시스템 연동 (파일, DB, S3, ElasticSearch)

**핵심**: "비즈니스 로직 + 외부 연동 테스트"

---

## 결론

현재 `infrastructure/`에 있는 테스트들은:
- ❌ Controller를 테스트하지 않음
- ❌ MockMvc를 사용하지 않음
- ❌ HTTP 요청/응답을 검증하지 않음
- ✅ Service 계층을 직접 테스트
- ✅ 외부 시스템(파일, DB, S3, ES) 연동 테스트
- ✅ 비즈니스 플로우 전체 검증

따라서 **infrastructure/**가 올바른 위치입니다!

**api/** 디렉토리는 추후 Controller 테스트를 작성할 때 사용됩니다.
