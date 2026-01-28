# Continue Bank (위탁사) - 기술 문서

## 📋 목차
- [서비스 개요](#-서비스-개요)
- [데이터베이스 구조](#-데이터베이스-구조)
- [개인정보 처리 흐름](#-개인정보-처리-흐름)
- [API 엔드포인트](#-api-엔드포인트)
- [주요 기능](#-주요-기능)
- [보안 구현](#-보안-구현)

## 🎯 서비스 개요

Continue Bank는 V-PASS 본인인증을 활용한 디지털 뱅킹 서비스의 위탁사입니다.

### 핵심 역할
- 사용자 회원가입 및 인증 관리
- 금융 서비스 제공 (계좌 개설, 거래 등)
- 금융 컴플라이언스 준수 (약관 동의 관리)
- V-PASS 및 TM 센터와의 연동

### 서비스 포트
- **Backend**: 8080
- **Frontend**: 5175

## 🗄 데이터베이스 구조

### ERD (Entity Relationship Diagram)

```
┌─────────────────────────────────────┐
│            users 테이블              │
├─────────────────────────────────────┤
│ id (PK)                    BIGINT   │
│ username                   VARCHAR  │ ← 로그인 ID
│ password                   VARCHAR  │ ← 암호화된 비밀번호
│ name                       VARCHAR  │
│ phone_number               VARCHAR  │
│ ci                         VARCHAR  │ ← V-PASS에서 받은 CI
│ created_at                 DATETIME │
│                                     │
│ ─── 약관 동의 정보 (9개 필수) ───    │
│ age_agreed                 BOOLEAN  │ ← 만 14세 이상
│ terms_agreed               BOOLEAN  │ ← 서비스 이용약관
│ privacy_agreed             BOOLEAN  │ ← 개인정보 수집·이용
│ unique_id_agreed           BOOLEAN  │ ← 고유식별정보 처리
│ credit_info_agreed         BOOLEAN  │ ← 신용정보 조회·제공
│ carrier_auth_agreed        BOOLEAN  │ ← V-pass 본인인증 이용
│ vpass_provision_agreed     BOOLEAN  │ ← V-pass 데이터 제공
│ electronic_finance_agreed  BOOLEAN  │ ← 전자금융거래 약관
│ monitoring_agreed          BOOLEAN  │ ← 거래 모니터링/AML
│                                     │
│ ─── 약관 동의 정보 (2개 선택) ───    │
│ marketing_personal_agreed  BOOLEAN  │ ← 개인맞춤형 추천
│ marketing_agreed           BOOLEAN  │ ← 혜택/이벤트 알림
│                                     │
│ marketing_sms_agreed       BOOLEAN  │ ← SMS 수신 동의
│ agreed_at                  DATETIME │ ← 약관 동의 시각
└─────────────────────────────────────┘
                │
                │ 1:N
                ▼
┌─────────────────────────────────────┐
│          accounts 테이블             │
├─────────────────────────────────────┤
│ id (PK)                    BIGINT   │
│ user_id (FK)               BIGINT   │
│ account_number             VARCHAR  │
│ balance                    DECIMAL  │
│ account_type               VARCHAR  │
│ status                     VARCHAR  │
│ created_at                 DATETIME │
└─────────────────────────────────────┘
```

### 주요 테이블 설명

#### 1. users 테이블
사용자의 기본 정보와 약관 동의 내역을 저장합니다.

**주요 컬럼:**
- `ci`: V-PASS에서 생성한 Connecting Information (중복 가입 방지)
- `age_agreed` ~ `monitoring_agreed`: 9개 필수 약관 동의 여부
- `marketing_personal_agreed`, `marketing_agreed`: 2개 선택 약관 동의 여부
- `agreed_at`: 약관 동의 시각 (법적 증거)

#### 2. accounts 테이블
사용자의 계좌 정보를 저장합니다.

**주요 컬럼:**
- `user_id`: users 테이블과의 외래키
- `account_number`: 계좌번호 (자동 생성)
- `balance`: 잔액
- `status`: 계좌 상태 (ACTIVE, SUSPENDED, CLOSED)

## 🔄 개인정보 처리 흐름

### 1. 회원가입 플로우

```
[사용자] → [약관 동의 페이지]
              │
              ▼
        9개 필수 약관 동의
        2개 선택 약관 동의
              │
              ▼
        [회원가입 페이지]
              │
              ├─ 이름 입력
              ├─ 휴대폰번호 입력
              │
              ▼
        [본인인증 하기] 클릭
              │
              ▼
        ┌─────────────────────┐
        │ V-PASS로 리다이렉트  │
        │ (tokenId 전달)      │
        └─────────────────────┘
              │
              ▼
        V-PASS 본인인증 완료
        (CI 생성 및 JWT 발급)
              │
              ▼
        Continue Bank로 복귀
        (tokenId, phoneNumber, name)
              │
              ▼
        [회원가입 페이지]
              │
              ├─ 아이디 입력
              ├─ 비밀번호 입력
              │
              ▼
        [회원가입 완료] 버튼 클릭
              │
              ▼
        POST /api/v1/auth/register
              │
              ├─ CI 기반 중복 체크
              ├─ 비밀번호 암호화
              ├─ 약관 동의 정보 저장
              │
              ▼
        회원가입 완료 → 로그인 페이지
```

### 2. 계좌 개설 플로우

```
[로그인 사용자] → [대시보드]
                      │
                      ▼
                [계좌 개설하기] 클릭
                      │
                      ▼
                [계좌 개설 페이지]
                      │
                      ▼
                [본인인증 하기] 클릭
                      │
                      ▼
                ┌─────────────────────┐
                │ V-PASS로 리다이렉트  │
                │ (재인증)            │
                └─────────────────────┘
                      │
                      ▼
                V-PASS 본인인증 완료
                      │
                      ▼
                Continue Bank로 복귀
                      │
                      ▼
                POST /api/v1/accounts/create
                      │
                      ├─ 계좌번호 자동 생성
                      ├─ 초기 잔액 설정
                      ├─ 계좌 상태: ACTIVE
                      │
                      ▼
                계좌 개설 완료 → 대시보드
```

### 3. 개인정보 전송 (V-PASS)

**전송 시점:**
- 회원가입 시 본인인증
- 계좌 개설 시 본인인증
- 아이디 찾기
- 비밀번호 재설정

**전송 데이터:**
```json
{
  "clientData": "01012345678",  // 휴대폰번호 (암호화 전)
  "name": "홍길동"               // 이름 (암호화 전)
}
```

**수신 데이터:**
```json
{
  "tokenId": "uuid-generated-token",
  "ci": "encrypted-ci-value",
  "jwt": "jwt-token"
}
```

## 📡 API 엔드포인트

### 인증 관련

#### POST /api/v1/auth/register
회원가입

**Request Body:**
```json
{
  "name": "홍길동",
  "username": "user123",
  "password": "Password123!",
  "phoneNumber": "01012345678",
  "tokenId": "uuid-from-vpass",
  "termsAgreement": {
    "agreements": {
      "age": true,
      "terms": true,
      "privacy": true,
      "uniqueId": true,
      "creditInfo": true,
      "carrierAuth": true,
      "vpassProvision": true,
      "electronicFinance": true,
      "monitoring": true,
      "marketingPersonal": false,
      "marketing": true
    }
  }
}
```

**Response:** `200 OK` or `400 Bad Request`

#### POST /api/v1/auth/login
로그인

**Request Body:**
```json
{
  "username": "user123",
  "password": "Password123!"
}
```

**Response:**
```json
{
  "id": 1,
  "username": "user123",
  "name": "홍길동",
  "phoneNumber": "01012345678",
  "joinedAt": "2026-01-28T14:30:00"
}
```

#### GET /api/v1/auth/find-id
아이디 찾기

**Query Parameters:**
- `phoneNumber`: 휴대폰번호
- `name`: 이름

**Response:** `user123` (마스킹: `us***`)

#### POST /api/v1/auth/reset-password
비밀번호 재설정

**Query Parameters:**
- `username`: 아이디
- `newPassword`: 새 비밀번호
- `phoneNumber`: 휴대폰번호
- `name`: 이름

**Response:** `200 OK`

### 계좌 관련

#### POST /api/v1/accounts/create
계좌 개설

**Request Body:**
```json
{
  "userId": 1,
  "accountType": "SAVINGS"
}
```

**Response:**
```json
{
  "id": 1,
  "accountNumber": "110-123-456789",
  "balance": 0,
  "accountType": "SAVINGS",
  "status": "ACTIVE",
  "createdAt": "2026-01-28T15:00:00"
}
```

#### GET /api/v1/accounts/user/{userId}
사용자 계좌 조회

**Response:**
```json
[
  {
    "id": 1,
    "accountNumber": "110-123-456789",
    "balance": 1000000,
    "accountType": "SAVINGS",
    "status": "ACTIVE"
  }
]
```

## 🔑 주요 기능

### 1. 약관 동의 관리

**필수 약관 (9개):**
1. 만 14세 이상 확인
2. 서비스 이용약관
3. 개인정보 수집 및 이용
4. 고유식별정보 처리
5. 신용정보 조회 및 제공
6. V-pass 본인인증 이용
7. 개인정보의 V-pass 제공
8. 전자금융거래 기본약관
9. 금융거래 정보 모니터링

**선택 약관 (2개):**
1. 개인맞춤형 금융상품 추천
2. 혜택 및 이벤트 소식

**동의 내역 관리 페이지:**
- 필수/선택 약관 구분 표시
- 동의일시 기록
- 선택 약관 철회 기능 (48시간 재동의 제한)
- 본인인증 기록 조회

### 2. V-PASS 연동

**연동 시나리오:**
1. 위탁사에서 `/trustee-api/v1/auth/init` 호출
2. V-PASS에서 `tokenId` 생성 및 반환
3. V-PASS 인증 페이지로 리다이렉트 (tokenId, name, phoneNumber 전달)
4. 사용자 본인인증 완료
5. 위탁사로 리다이렉트 (verified=true, tokenId, phoneNumber, name)
6. 위탁사에서 회원가입/계좌개설 처리

### 3. 보안 기능

**비밀번호 정책:**
- 최소 8자 이상
- 영문, 숫자, 특수문자 조합 권장

**세션 관리:**
- 로그인 시 사용자 정보 sessionStorage 저장
- 로그아웃 시 세션 정보 삭제

**중복 가입 방지:**
- CI (Connecting Information) 기반 중복 체크
- 동일 CI로 재가입 불가

## 🔐 보안 구현

### 1. 비밀번호 암호화
- **알고리즘**: BCrypt
- **처리**: Spring Security PasswordEncoder 사용

### 2. CORS 설정
```java
@Configuration
public class WebConfig {
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                    .allowedOrigins("http://localhost:5175", "http://localhost:5176")
                    .allowedMethods("GET", "POST", "PUT", "DELETE")
                    .allowCredentials(true);
            }
        };
    }
}
```

### 3. 프록시 설정
V-PASS API 호출을 위한 프록시 설정 (`vite.config.js`):
```javascript
export default defineConfig({
  server: {
    proxy: {
      '/trustee-api': {
        target: 'http://localhost:8086',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/trustee-api/, '')
      }
    }
  }
})
```

## 📝 환경 변수

### Backend (`application.properties`)
```properties
# Database
spring.datasource.url=jdbc:mysql://localhost:3306/entrusting_db
spring.datasource.username=root
spring.datasource.password=your_password

# Server
server.port=8080

# JPA
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

### Frontend (`.env`)
```env
VITE_TRUSTEE_FRONTEND_URL=http://localhost:5176
VITE_API_BASE_URL=http://localhost:8080
```

## 🧪 테스트

### 회원가입 테스트 시나리오
1. http://localhost:5175/signup 접속
2. 약관 동의 (필수 9개 체크)
3. 이름, 휴대폰번호 입력
4. 본인인증 → V-PASS 페이지 이동
5. OTP 인증 완료
6. 회원가입 페이지로 복귀
7. 아이디, 비밀번호 입력
8. 회원가입 완료

### 계좌 개설 테스트 시나리오
1. 로그인
2. 대시보드에서 "계좌 개설하기" 클릭
3. 본인인증 → V-PASS 페이지 이동
4. OTP 인증 완료
5. 계좌 개설 완료
6. 대시보드에서 계좌 확인

---

**Continue Bank** - 안전하고 편리한 디지털 뱅킹 서비스 🏦
