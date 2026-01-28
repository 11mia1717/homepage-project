# V-PASS 본인인증 (수탁사) - 기술 문서

## 📋 목차
- [서비스 개요](#-서비스-개요)
- [데이터베이스 구조](#-데이터베이스-구조)
- [인증 프로세스](#-인증-프로세스)
- [API 엔드포인트](#-api-엔드포인트)
- [보안 구현](#-보안-구현)
- [데이터 관리](#-데이터-관리)

## 🎯 서비스 개요

V-PASS는 휴대폰 본인인증을 제공하는 수탁사입니다. 위탁사(Continue Bank, TM 센터)의 요청에 따라 사용자 본인인증을 수행하고 CI(Connecting Information)를 생성합니다.

### 핵심 역할
- 휴대폰 번호 기반 OTP 인증
- CI (Connecting Information) 생성 및 관리
- 개인정보 암호화 및 보안 관리
- TTL 기반 인증 데이터 자동 삭제

### 서비스 포트
- **Backend**: 8086
- **Frontend**: 5176

## 🗄 데이터베이스 구조

### ERD (Entity Relationship Diagram)

```
┌─────────────────────────────────────┐
│        auth_tokens 테이블            │
├─────────────────────────────────────┤
│ id (PK)                    BIGINT   │
│ token_id                   VARCHAR  │ ← UUID 토큰
│ encrypted_name             VARCHAR  │ ← AES-256 암호화
│ encrypted_phone_number     VARCHAR  │ ← AES-256 암호화
│ ci                         VARCHAR  │ ← 생성된 CI
│ jwt_token                  TEXT     │ ← 발급된 JWT
│ ip_address                 VARCHAR  │ ← 요청 IP
│ verified                   BOOLEAN  │ ← 인증 완료 여부
│ created_at                 DATETIME │
│ expires_at                 DATETIME │ ← TTL (3분)
└─────────────────────────────────────┘

┌─────────────────────────────────────┐
│      carrier_users 테이블            │
├─────────────────────────────────────┤
│ id (PK)                    BIGINT   │
│ name                       VARCHAR  │
│ resident_id                VARCHAR  │ ← 주민등록번호 (앞 6자리)
│ phone_number               VARCHAR  │
│ carrier                    VARCHAR  │ ← SKT/KT/LGU+
│ created_at                 DATETIME │
└─────────────────────────────────────┘
```

### 주요 테이블 설명

#### 1. auth_tokens 테이블
본인인증 요청 및 결과를 저장합니다.

**주요 컬럼:**
- `token_id`: 위탁사와 공유하는 고유 토큰 (UUID)
- `encrypted_name`: AES-256으로 암호화된 이름
- `encrypted_phone_number`: AES-256으로 암호화된 휴대폰번호
- `ci`: 생성된 Connecting Information (SHA-256 해시)
- `jwt_token`: 인증 완료 시 발급되는 JWT
- `expires_at`: 만료 시각 (생성 후 3분)

**TTL (Time To Live):**
- 생성 후 3분이 지나면 자동 삭제
- 스케줄러가 1분마다 만료된 데이터 삭제

#### 2. carrier_users 테이블
가상 통신사 데이터베이스 (테스트용 더미 데이터)

**주요 컬럼:**
- `name`: 사용자 이름
- `resident_id`: 주민등록번호 앞 6자리
- `phone_number`: 휴대폰번호
- `carrier`: 통신사 (SKT, KT, LGU+)

**더미 데이터 예시:**
```sql
INSERT INTO carrier_users (name, resident_id, phone_number, carrier) VALUES
('홍길동', '900101', '01012345678', 'SKT'),
('김철수', '850315', '01087654321', 'KT'),
('이영희', '920728', '01055556666', 'LGU+');
```

## 🔄 인증 프로세스

### 전체 플로우

```
[위탁사] → POST /api/v1/auth/init
              │
              ├─ clientData: "01012345678"
              ├─ name: "홍길동"
              │
              ▼
        ┌─────────────────────┐
        │ 1. tokenId 생성      │
        │ 2. 데이터 암호화     │
        │ 3. DB 저장          │
        └─────────────────────┘
              │
              ▼
        Response: { tokenId: "uuid" }
              │
              ▼
[위탁사] → V-PASS 페이지로 리다이렉트
        (tokenId, name, phoneNumber 전달)
              │
              ▼
[V-PASS 페이지]
              │
              ├─ 이름 (자동 입력)
              ├─ 주민등록번호 앞 6자리
              ├─ 통신사 선택
              ├─ 휴대폰번호 (자동 입력)
              │
              ▼
        [인증번호 발송] 클릭
              │
              ▼
        POST /api/v1/auth/request-otp
              │
              ├─ 가상 통신사 DB 조회
              ├─ 정보 일치 확인
              ├─ OTP 생성 (6자리)
              │
              ▼
        OTP 발송 (콘솔 출력)
              │
              ▼
[사용자] → OTP 입력
              │
              ▼
        POST /api/v1/auth/confirm
              │
              ├─ OTP 검증
              ├─ CI 생성 (SHA-256)
              ├─ JWT 발급
              ├─ verified = true
              │
              ▼
        인증 완료 → 위탁사로 리다이렉트
        (verified=true, tokenId, phoneNumber, name)
```

### CI (Connecting Information) 생성

CI는 사용자를 고유하게 식별하는 값으로, 중복 가입 방지에 사용됩니다.

**생성 알고리즘:**
```java
String rawData = name + phoneNumber + residentId;
String ci = DigestUtils.sha256Hex(rawData);
```

**예시:**
```
Input: "홍길동" + "01012345678" + "900101"
Output: "a1b2c3d4e5f6..." (64자 해시)
```

## 📡 API 엔드포인트

### 1. POST /api/v1/auth/init
본인인증 초기화 (위탁사 → V-PASS)

**Request Body:**
```json
{
  "clientData": "01012345678",
  "name": "홍길동"
}
```

**Response:**
```json
{
  "tokenId": "550e8400-e29b-41d4-a716-446655440000"
}
```

**처리 과정:**
1. UUID 기반 tokenId 생성
2. 이름, 휴대폰번호 AES-256 암호화
3. auth_tokens 테이블에 저장
4. expires_at = 현재시각 + 3분

### 2. POST /api/v1/auth/request-otp
OTP 발송 요청

**Request Body:**
```json
{
  "tokenId": "550e8400-e29b-41d4-a716-446655440000",
  "name": "홍길동",
  "residentId": "900101",
  "carrier": "SKT",
  "phoneNumber": "01012345678"
}
```

**Response:**
```json
{
  "success": true,
  "message": "인증번호가 발송되었습니다",
  "otp": "123456"
}
```

**처리 과정:**
1. tokenId로 auth_tokens 조회
2. 암호화된 데이터 복호화
3. carrier_users 테이블에서 정보 일치 확인
4. 6자리 OTP 생성 (Random)
5. OTP를 auth_tokens에 저장
6. 콘솔에 OTP 출력 (실제 환경에서는 SMS 발송)

### 3. POST /api/v1/auth/confirm
OTP 검증 및 인증 완료

**Request Body:**
```json
{
  "tokenId": "550e8400-e29b-41d4-a716-446655440000",
  "otp": "123456"
}
```

**Response:**
```json
{
  "success": true,
  "ci": "a1b2c3d4e5f6...",
  "jwt": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**처리 과정:**
1. tokenId로 auth_tokens 조회
2. OTP 일치 확인
3. CI 생성 (name + phoneNumber + residentId → SHA-256)
4. JWT 토큰 발급
5. verified = true 업데이트
6. CI, JWT 저장

### 4. GET /api/v1/auth/verify/{tokenId}
인증 상태 확인 (위탁사 → V-PASS)

**Response:**
```json
{
  "verified": true,
  "ci": "a1b2c3d4e5f6...",
  "jwt": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "name": "홍길동",
  "phoneNumber": "01012345678"
}
```

## 🔐 보안 구현

### 1. AES-256 암호화

**암호화 대상:**
- 이름 (name)
- 휴대폰번호 (phoneNumber)

**암호화 알고리즘:**
```java
@Service
public class EncryptionService {
    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final String SECRET_KEY = "your-32-byte-secret-key-here!!";
    
    public String encrypt(String data) {
        // AES-256-CBC 암호화
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        SecretKeySpec keySpec = new SecretKeySpec(SECRET_KEY.getBytes(), "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(IV.getBytes());
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
        byte[] encrypted = cipher.doFinal(data.getBytes());
        return Base64.getEncoder().encodeToString(encrypted);
    }
    
    public String decrypt(String encryptedData) {
        // AES-256-CBC 복호화
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        SecretKeySpec keySpec = new SecretKeySpec(SECRET_KEY.getBytes(), "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(IV.getBytes());
        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
        byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(encryptedData));
        return new String(decrypted);
    }
}
```

### 2. CI 생성 (SHA-256)

**목적:**
- 사용자 고유 식별
- 중복 가입 방지
- 개인정보 비식별화

**생성 방법:**
```java
String rawData = name + phoneNumber + residentId;
String ci = DigestUtils.sha256Hex(rawData);
```

### 3. JWT 토큰 발급

**Payload:**
```json
{
  "sub": "tokenId",
  "ci": "a1b2c3d4e5f6...",
  "name": "홍길동",
  "phoneNumber": "01012345678",
  "iat": 1706500000,
  "exp": 1706503600
}
```

**만료 시간:** 1시간

## 🗑 데이터 관리

### TTL (Time To Live) 구현

**목적:**
- 개인정보 최소 보관 원칙 준수
- 불필요한 데이터 자동 삭제

**구현:**
```java
@Service
public class AuthTokenCleanupService {
    
    @Scheduled(fixedRate = 60000) // 1분마다 실행
    public void cleanupExpiredTokens() {
        LocalDateTime now = LocalDateTime.now();
        List<AuthToken> expiredTokens = authTokenRepository
            .findByExpiresAtBefore(now);
        
        authTokenRepository.deleteAll(expiredTokens);
        
        log.info("Deleted {} expired auth tokens", expiredTokens.size());
    }
}
```

**만료 기준:**
- 생성 시각 + 3분
- `expires_at` 컬럼으로 관리

### IP 주소 기록

**목적:**
- 보안 감시
- 의심 거래 탐지

**저장 위치:**
- `auth_tokens.ip_address`

**수집 방법:**
```java
@PostMapping("/init")
public ResponseEntity<?> init(@RequestBody InitRequest request, 
                               HttpServletRequest httpRequest) {
    String ipAddress = httpRequest.getRemoteAddr();
    // IP 주소 저장
}
```

## 📝 환경 변수

### Backend (`application.properties`)
```properties
# Database
spring.datasource.url=jdbc:mysql://localhost:3306/vpass_db
spring.datasource.username=root
spring.datasource.password=your_password

# Server
server.port=8086

# JPA
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# Encryption
encryption.secret.key=your-32-byte-secret-key-here!!
encryption.iv=your-16-byte-iv!!

# JWT
jwt.secret=your-jwt-secret-key
jwt.expiration=3600000
```

### Frontend (`.env`)
```env
VITE_API_BASE_URL=http://localhost:8086
```

## 🧪 테스트

### 본인인증 테스트 시나리오

**준비:**
1. 가상 통신사 DB에 테스트 데이터 등록
```sql
INSERT INTO carrier_users (name, resident_id, phone_number, carrier) 
VALUES ('홍길동', '900101', '01012345678', 'SKT');
```

**테스트 플로우:**
1. 위탁사에서 `/api/v1/auth/init` 호출
2. tokenId 받기
3. V-PASS 페이지 접속 (tokenId 포함)
4. 이름: 홍길동
5. 주민등록번호: 900101
6. 통신사: SKT
7. 휴대폰번호: 01012345678
8. 인증번호 발송 → 콘솔에서 OTP 확인
9. OTP 입력 및 인증 완료
10. 위탁사로 리다이렉트 확인

### OTP 확인 방법

**개발 모드:**
- 백엔드 콘솔에 OTP 출력
```
[V-PASS] OTP for 01012345678: 123456
```

**프로덕션 모드:**
- 실제 SMS 발송 API 연동 필요

## 🔍 모니터링

### 로그 확인

**인증 요청 로그:**
```
[INFO] Auth init request from IP: 127.0.0.1, tokenId: 550e8400...
```

**OTP 발송 로그:**
```
[INFO] OTP sent to 010****5678: 123456
```

**인증 완료 로그:**
```
[INFO] Auth confirmed for tokenId: 550e8400..., CI: a1b2c3d4...
```

**TTL 삭제 로그:**
```
[INFO] Deleted 5 expired auth tokens
```

---

**V-PASS** - 안전하고 신뢰할 수 있는 본인인증 서비스 🔐
