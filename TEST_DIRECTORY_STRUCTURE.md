# 테스트 디렉토리 구조 (최종)

## 디렉토리 구조

```
src/
├── test/                      # 공통 테스트 리소스
│   ├── java/                  # ❌ 비어있음 (삭제 가능)
│   └── resources/             # ✅ 공유 설정 파일
│       └── application.yaml   # 단위/통합 테스트 공통 설정
│
├── test-unit/                 # 단위 테스트 (빠름, ~5초)
│   └── java/
│       └── com/onbok/book_hub/
│           ├── BookStockTest.java
│           ├── CartTest.java
│           ├── CartRepositoryTest.java
│           ├── CartCalculationServiceTest.java
│           ├── DeliveryAddressTest.java
│           ├── DeliveryAddressRepositoryTest.java
│           ├── ImageRepositoryTest.java
│           ├── OrderRepositoryTest.java
│           ├── RecommendationServiceTest.java
│           ├── ReviewRepositoryTest.java
│           ├── TossPaymentTest.java
│           ├── BookEsServiceTest.java
│           └── OAuth2UserServiceTest.java
│
└── test-integration/          # 통합 테스트 (느림, ~20초)
    └── java/
        └── com/onbok/book_hub/
            ├── DeliveryAddressIntegrationTest.java
            ├── ElasticsearchIntegrationTest.java
            ├── ImageLocalUploadTest.java
            ├── ImageS3ServiceTest.java
            ├── OrderIntegrationTest.java
            ├── ReviewIntegrationTest.java
            └── TransactionStabilityTest.java
```

---

## 핵심 포인트

### 1. **src/test/java는 비어있음**
- ✅ **삭제 가능**합니다
- 모든 테스트는 `test-unit` 또는 `test-integration`으로 이동됨

### 2. **src/test/resources는 공유**
- ✅ **유지 필요**
- `application.yaml`을 단위/통합 테스트가 **공유**합니다
- 중복 제거로 관리 편의성 향상

### 3. **Gradle 설정**
```gradle
sourceSets {
    unitTest {
        java { srcDir 'src/test-unit/java' }
        resources { srcDir 'src/test/resources' }  // 공유!
    }

    integrationTest {
        java { srcDir 'src/test-integration/java' }
        resources { srcDir 'src/test/resources' }  // 공유!
    }
}
```

---

## 자주 묻는 질문 (FAQ)

### Q1. src/test/java를 삭제해도 되나요?
**A.** 네, 삭제해도 됩니다. 비어있습니다.

```bash
rm -rf src/test/java
```

### Q2. application.yaml을 따로 관리해야 하나요?
**A.** 아니요, **src/test/resources/application.yaml 하나만** 사용합니다.
- 단위 테스트와 통합 테스트가 동일한 설정을 공유
- 필요시 프로파일로 분리 가능:
  - `application-unit.yaml`
  - `application-integration.yaml`

### Q3. 특정 테스트만 다른 설정을 사용하려면?
**A.** Spring Profile을 사용하세요:

**src/test/resources/application-integration.yaml 생성**:
```yaml
spring:
  # 통합 테스트 전용 설정
  datasource:
    url: jdbc:h2:mem:integrationdb
```

**테스트에서 사용**:
```java
@SpringBootTest
@ActiveProfiles("integration")
class OrderIntegrationTest {
    // ...
}
```

### Q4. 왜 resources를 공유하나요?
**A.** 중복 제거와 관리 편의성:
- ✅ 설정 파일 하나만 관리
- ✅ 변경 시 한 곳만 수정
- ✅ 일관성 유지

---

## 테스트 실행

### 단위 테스트만 (빠름)
```bash
./gradlew unitTest
```

### 통합 테스트만
```bash
./gradlew integrationTest
```

### 전체 테스트
```bash
./gradlew test
```

---

## 정리 명령어 (선택사항)

### src/test/java 삭제 (비어있음)
```bash
rm -rf src/test/java
```

### .DS_Store 파일 정리 (macOS)
```bash
find . -name ".DS_Store" -delete
```

---

## 요약

| 디렉토리 | 용도 | 상태 | 비고 |
|---------|------|------|------|
| `src/test/java` | (없음) | ❌ 삭제 가능 | 비어있음 |
| `src/test/resources` | 공통 설정 | ✅ 유지 필요 | application.yaml 공유 |
| `src/test-unit/java` | 단위 테스트 | ✅ 사용 중 | 15개 테스트 |
| `src/test-integration/java` | 통합 테스트 | ✅ 사용 중 | 7개 테스트 |

**결론**: `src/test/resources`만 남기고, `src/test/java`는 삭제해도 됩니다!
