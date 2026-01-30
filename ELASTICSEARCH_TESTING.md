# ElasticSearch 테스트 가이드

## 현재 상태

ElasticSearch 통합 테스트(`ElasticsearchIntegrationTest`)는 현재 **비활성화** 상태입니다.

```java
@Disabled("ElasticSearch 서버가 실행 중일 때만 활성화. Testcontainers 사용 권장")
```

## 비활성화 이유

1. **ElasticSearch 서버 부재**: 테스트 환경에 ElasticSearch 서버가 실행되지 않음
2. **테스트 구성 충돌**: `src/test/resources/application.yaml`에서 ElasticSearch 자동 구성을 제외함
   ```yaml
   spring:
     autoconfigure:
       exclude:
         - org.springframework.boot.autoconfigure.data.elasticsearch.ElasticsearchDataAutoConfiguration
         - org.springframework.boot.autoconfigure.elasticsearch.ElasticsearchRestClientAutoConfiguration
   ```
3. **CI/CD 호환성**: 모든 환경에서 테스트가 실행 가능하도록 ElasticSearch 없이도 빌드 가능

---

## ElasticSearch 테스트 활성화 방법

### 방법 1: Testcontainers 사용 (권장)

Testcontainers를 사용하면 Docker 컨테이너로 ElasticSearch를 자동으로 시작/종료합니다.

#### 1.1. 의존성 추가

`build.gradle`에 추가:

```gradle
dependencies {
    // Testcontainers
    testImplementation 'org.testcontainers:testcontainers:1.19.3'
    testImplementation 'org.testcontainers:elasticsearch:1.19.3'
    testImplementation 'org.testcontainers:junit-jupiter:1.19.3'
}
```

#### 1.2. 테스트 클래스 수정

```java
package com.onbok.book_hub.elasticsearch;

import com.onbok.book_hub.book.application.service.bookEs.BookEsService;
import com.onbok.book_hub.book.domain.model.bookEs.BookEs;
import com.onbok.book_hub.book.domain.repository.bookEs.BookEsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest(properties = {
        "spring.data.elasticsearch.repositories.enabled=true"
})
@DisplayName("ElasticSearch 통합 테스트 (Testcontainers)")
class ElasticsearchIntegrationTest {

    @Container
    static ElasticsearchContainer elasticsearchContainer = new ElasticsearchContainer(
            "docker.elastic.co/elasticsearch/elasticsearch:8.11.0"
    ).withEnv("xpack.security.enabled", "false")
     .withEnv("discovery.type", "single-node");

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.elasticsearch.uris", elasticsearchContainer::getHttpHostAddress);
        registry.add("spring.autoconfigure.exclude", () -> ""); // Override exclusions
    }

    @Autowired
    private BookEsService bookEsService;

    @Autowired
    private BookEsRepository bookEsRepository;

    @BeforeEach
    void setUp() {
        bookEsRepository.deleteAll();
    }

    // ... 테스트 메서드들 ...
}
```

#### 1.3. @Disabled 제거

테스트 클래스에서 `@Disabled` 어노테이션을 제거합니다.

---

### 방법 2: 로컬 ElasticSearch 사용

로컬에 ElasticSearch가 이미 실행 중인 경우 사용합니다.

#### 2.1. ElasticSearch 실행

Docker로 ElasticSearch 실행:

```bash
docker run -d \
  --name elasticsearch \
  -p 9200:9200 \
  -e "discovery.type=single-node" \
  -e "xpack.security.enabled=false" \
  docker.elastic.co/elasticsearch/elasticsearch:8.11.0
```

#### 2.2. 테스트 프로파일 생성

`src/test/resources/application-elasticsearch.yaml` 생성:

```yaml
spring:
  data:
    elasticsearch:
      repositories:
        enabled: true
  elasticsearch:
    uris: http://localhost:9200

  # ElasticSearch 자동 구성 제외를 비활성화
  autoconfigure:
    exclude: []  # 빈 리스트로 override
```

#### 2.3. 테스트 클래스 수정

```java
@ActiveProfiles("elasticsearch")
@SpringBootTest
@DisplayName("ElasticSearch 통합 테스트 (로컬 서버)")
class ElasticsearchIntegrationTest {
    // ... 테스트 코드 ...
}
```

#### 2.4. @Disabled 제거 및 테스트 실행

```bash
# 특정 테스트만 실행
./gradlew test --tests ElasticsearchIntegrationTest

# 또는 특정 프로파일로 실행
./gradlew test -Dspring.profiles.active=elasticsearch
```

---

### 방법 3: CI/CD에서 조건부 실행

GitHub Actions 등에서 ElasticSearch 서비스를 추가:

```yaml
name: Test with ElasticSearch

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest

    services:
      elasticsearch:
        image: docker.elastic.co/elasticsearch/elasticsearch:8.11.0
        ports:
          - 9200:9200
        env:
          discovery.type: single-node
          xpack.security.enabled: false

    steps:
      - uses: actions/checkout@v3
      - name: Set up JDK 17
        uses: actions/setup-java@v3
        with:
          java-version: '17'
      - name: Run tests
        run: ./gradlew test -Dspring.profiles.active=elasticsearch
```

---

## 테스트 활성화 체크리스트

- [ ] Testcontainers 또는 로컬 ElasticSearch 준비
- [ ] `ElasticsearchIntegrationTest`에서 `@Disabled` 제거
- [ ] 테스트 설정 수정 (위 방법 중 하나 선택)
- [ ] Docker Desktop 실행 (Testcontainers 사용 시)
- [ ] 테스트 실행 및 검증

---

## 문제 해결

### 에러: "No bean named 'elasticsearchTemplate' available"

**원인**: `application.yaml`에서 ElasticSearch 자동 구성이 제외됨

**해결**:
1. `@DynamicPropertySource`를 사용해 `spring.autoconfigure.exclude`를 빈 문자열로 override
2. 또는 별도의 테스트 프로파일 사용

### 에러: "Connection refused to localhost:9200"

**원인**: ElasticSearch 서버가 실행되지 않음

**해결**:
1. Docker로 ElasticSearch 실행
2. 또는 Testcontainers 사용

### 에러: Docker not found (Testcontainers)

**원인**: Docker Desktop이 설치되지 않았거나 실행되지 않음

**해결**:
1. Docker Desktop 설치: https://www.docker.com/products/docker-desktop
2. Docker Desktop 실행
3. 테스트 재실행

---

## 참고 자료

- [Testcontainers 공식 문서](https://www.testcontainers.org/)
- [Spring Data Elasticsearch 문서](https://docs.spring.io/spring-data/elasticsearch/docs/current/reference/html/)
- [ElasticSearch Docker 이미지](https://www.docker.elastic.co/)
