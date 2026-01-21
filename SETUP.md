# OnBok Book-Hub 개발 환경 설정 가이드

## 📋 목차
- [필수 프로그램 설치](#필수-프로그램-설치)
- [데이터베이스 설정](#데이터베이스-설정)
- [ElasticSearch 설정](#elasticsearch-설정)
- [애플리케이션 설정](#애플리케이션-설정)
- [Toss Payments 설정](#toss-payments-설정)
- [실행 방법](#실행-방법)

---

## 📦 필수 프로그램 설치

### 1. JDK 17 이상
```bash
# Mac (Homebrew)
brew install openjdk@17

# 환경변수 설정
export JAVA_HOME=/Library/Java/JavaVirtualMachines/openjdk-17.jdk/Contents/Home
```

### 2. MariaDB 10.x
```bash
# Mac
brew install mariadb
brew services start mariadb

# Linux
sudo apt-get install mariadb-server

# Windows
# MariaDB 공식 사이트에서 설치 파일 다운로드
# https://mariadb.org/download/
```

### 3. ElasticSearch 8.x
```bash
# Mac
brew install elasticsearch
brew services start elasticsearch

# Docker로 실행 (추천)
docker run -d \
  --name elasticsearch \
  -p 9200:9200 \
  -p 9300:9300 \
  -e "discovery.type=single-node" \
  -e "xpack.security.enabled=false" \
  docker.elastic.co/elasticsearch/elasticsearch:8.11.0
```

---

## 🗄️ 데이터베이스 설정

### MySQL 데이터베이스 생성
```sql
-- MySQL 접속
mysql -u root -p

-- 데이터베이스 생성
CREATE DATABASE book_hub CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 사용자 생성 (선택사항)
CREATE USER 'bookadmin'@'localhost' IDENTIFIED BY 'your_password';
GRANT ALL PRIVILEGES ON book_hub.* TO 'bookadmin'@'localhost';
FLUSH PRIVILEGES;
```

---

## 🔍 ElasticSearch 설정

### 1. ElasticSearch 실행 확인
```bash
curl http://localhost:9200
```

### 2. 인덱스 설정 파일 위치
```
src/main/resources/elasticsearch/book-settings.json
```

### 3. 애플리케이션 실행 시 자동 인덱스 생성
- 첫 실행 시 자동으로 `books` 인덱스가 생성됩니다
- Nori 형태소 분석기가 자동 설정됩니다

---

## ⚙️ 애플리케이션 설정

### 1. application-secret.yaml 파일 생성

**중요: 이 파일은 Git에 커밋되지 않습니다!**

```bash
cp src/main/resources/application-secret.yaml.example src/main/resources/application-secret.yaml
```

### 2. application-secret.yaml 수정

```yaml
spring:
  datasource:
    password: your_mysql_password  # MySQL 비밀번호

  elasticsearch:
    password: your_es_password     # ElasticSearch 비밀번호 (없으면 생략)

toss:
  payment:
    secret:
      key: test_sk_xxxxxxxxxx      # Toss Payments Secret Key
    client:
      key: test_ck_xxxxxxxxxx      # Toss Payments Client Key
```

### 3. application-local.yaml 확인

기본 설정은 이미 작성되어 있습니다:
- 데이터베이스: `jdbc:mysql://localhost:3306/book_hub`
- ElasticSearch: `http://localhost:9200`
- 서버 포트: `8080`

변경이 필요하면 `application-local.yaml` 파일을 수정하세요.

---

## 💳 Toss Payments 설정

### 1. Toss Payments 개발자 계정 생성
1. [Toss Payments 개발자센터](https://developers.tosspayments.com/) 접속
2. 회원가입 및 로그인
3. 내 애플리케이션 > 새 애플리케이션 만들기

### 2. API 키 발급
1. 애플리케이션 선택
2. **테스트 API 키** 확인
   - 클라이언트 키: `test_ck_...`
   - 시크릿 키: `test_sk_...`

### 3. application-secret.yaml에 설정
```yaml
toss:
  payment:
    secret:
      key: test_sk_xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
    client:
      key: test_ck_xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
```

### 4. Webhook URL 설정 (선택)
- URL: `https://your-domain.com/api/payments/webhook`
- Method: POST
- 이벤트: 결제 상태 변경

---

## 🔐 OAuth2 소셜 로그인 설정

애플리케이션은 **Google, Naver, GitHub** 3가지 OAuth2 로그인을 지원합니다.

### 1. Google OAuth2

#### 1-1. Google Cloud Console 설정
1. [Google Cloud Console](https://console.cloud.google.com/) 접속
2. 프로젝트 생성 또는 선택
3. **API 및 서비스 > 사용자 인증 정보** 이동
4. **사용자 인증 정보 만들기 > OAuth 클라이언트 ID** 선택
5. 애플리케이션 유형: **웹 애플리케이션**
6. 승인된 리디렉션 URI 추가:
   ```
   http://localhost:8080/login/oauth2/code/google
   ```
7. 클라이언트 ID와 클라이언트 보안 비밀번호 복사

#### 1-2. application-secret.yaml 설정
```yaml
spring.security.oauth2.client:
  registration:
    google:
      client-id: your_google_client_id
      client-secret: your_google_client_secret
```

---

### 2. Naver OAuth2

#### 2-1. Naver Developers 설정
1. [Naver Developers](https://developers.naver.com/) 접속
2. **Application > 애플리케이션 등록** 선택
3. 사용 API: **네아로 (네이버 아이디로 로그인)** 선택
4. 제공 정보 선택:
   - 회원이름 (필수)
   - 이메일 주소 (필수)
   - 프로필 이미지 (선택)
5. 서비스 환경: **PC 웹** 추가
6. 서비스 URL: `http://localhost:8080`
7. Callback URL:
   ```
   http://localhost:8080/login/oauth2/code/naver
   ```
8. Client ID와 Client Secret 복사

#### 2-2. application-secret.yaml 설정
```yaml
spring.security.oauth2.client:
  registration:
    naver:
      client-id: your_naver_client_id
      client-secret: your_naver_client_secret
```

---

### 3. GitHub OAuth2

#### 3-1. GitHub OAuth Apps 설정
1. [GitHub Settings](https://github.com/settings/developers) 접속
2. **OAuth Apps > New OAuth App** 선택
3. 정보 입력:
   - Application name: `OnBok Book-Hub`
   - Homepage URL: `http://localhost:8080`
   - Authorization callback URL:
     ```
     http://localhost:8080/login/oauth2/code/github
     ```
4. Register application 클릭
5. Client ID 확인 및 **Generate a new client secret** 클릭
6. Client Secret 복사

#### 3-2. application-secret.yaml 설정
```yaml
spring.security.oauth2.client:
  registration:
    github:
      client-id: your_github_client_id
      client-secret: your_github_client_secret
```

---

### 4. OAuth2 전체 설정 예시

```yaml
# application-secret.yaml
spring.security.oauth2.client:
  registration:
    google:
      client-id: 123456789-abc.apps.googleusercontent.com
      client-secret: GOCSPX-xxxxxxxxxxxx

    naver:
      client-id: AbCdEfGhIj
      client-secret: XxXxXxXxXx

    github:
      client-id: Iv1.1234567890abcdef
      client-secret: 0123456789abcdef0123456789abcdef01234567
```

---

## 🚀 실행 방법

### 1. Gradle을 사용한 실행
```bash
# 프로젝트 루트 디렉토리에서
./gradlew bootRun
```

### 2. IDE에서 실행
- IntelliJ IDEA: `BookHubApplication` 파일을 열고 실행 버튼 클릭
- Eclipse: 프로젝트 우클릭 > Run As > Spring Boot App

### 3. JAR 파일로 실행
```bash
# 빌드
./gradlew build

# 실행
java -jar build/libs/book-hub-0.0.1-SNAPSHOT.jar
```

---

## 🔗 접속 URL

애플리케이션이 정상적으로 실행되면:

### 웹 페이지
- 메인 페이지: http://localhost:8080
- 도서 목록: http://localhost:8080/view/books/list
- ElasticSearch 검색: http://localhost:8080/view/bookEs/list
- 통계 대시보드: http://localhost:8080/view/orders/charts (ADMIN)

### API 문서
- Swagger UI: http://localhost:8080/swagger-ui.html
- API Docs: http://localhost:8080/api-docs

### 데이터베이스
- MySQL: localhost:3306
- ElasticSearch: http://localhost:9200

---

## 🧪 테스트 데이터 초기화

### 1. CSV 파일로 도서 데이터 가져오기
```
GET /view/books/yes24
```

### 2. ElasticSearch 인덱스에 데이터 동기화
- 애플리케이션 실행 후 자동으로 인덱스가 생성됩니다
- 수동 동기화가 필요한 경우 BookEsService의 메서드를 호출하세요

---

## ⚠️ 문제 해결

### MySQL 연결 오류
```
Error: Access denied for user 'root'@'localhost'
```
**해결**: application-secret.yaml의 비밀번호 확인

### ElasticSearch 연결 오류
```
Connection refused: localhost:9200
```
**해결**: ElasticSearch가 실행 중인지 확인
```bash
# Mac
brew services list

# Docker
docker ps
```

### Toss Payments 오류
```
Error: Invalid secret key
```
**해결**:
1. Toss Payments 개발자센터에서 키 재확인
2. application-secret.yaml에 올바른 키 설정
3. 테스트 키(`test_sk_...`)와 운영 키(`live_sk_...`) 구분

---

## 📞 지원

문제가 해결되지 않으면:
1. GitHub Issues에 문의
2. 프로젝트 Wiki 확인
3. CHANGELOG.md에서 최신 변경사항 확인

---

**마지막 업데이트**: 2026년 1월 13일
