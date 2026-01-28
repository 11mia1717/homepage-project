# TM 센터 (수탁사) - 기술 문서

## 📋 목차
- [서비스 개요](#-서비스-개요)
- [위탁사 연동 구조](#-위탁사-연동-구조)
- [데이터베이스 구조](#-데이터베이스-구조)
- [상담 예약 프로세스](#-상담-예약-프로세스)
- [API 엔드포인트](#-api-엔드포인트)
- [V-PASS 연동](#-v-pass-연동)
- [마케팅 동의 관리](#-마케팅-동의-관리)

## 🎯 서비스 개요

TM 센터는 Continue Bank의 고객 상담을 담당하는 콜센터 서비스입니다. **위탁사(Continue Bank)와 직접 연동**되어 상담 예약을 받아 관리하며, V-PASS 본인인증을 통해 고객 정보를 안전하게 수집합니다.

### 핵심 역할
- 상담 예약 접수 및 관리
- **위탁사(Continue Bank)와 연동**
- V-PASS 본인인증 연동
- 마케팅 동의 정보 관리
- 고객 정보 조회 및 상담 이력 관리

### 서비스 포트
- **Backend**: 8082
- **Frontend**: 5178

## 🔗 위탁사 연동 구조

TM 센터는 독립적인 서비스가 아니라 **위탁사(Continue Bank)와 연동**되어 동작합니다.

```
┌──────────────────────────────────────────────────────────────┐
│                    Continue Bank (위탁사)                    │
│                      http://localhost:5175                   │
├──────────────────────────────────────────────────────────────┤
│                                                              │
│   [상품 페이지] ──► [상담 신청 버튼] ──► [본인인증]          │
│        │                                     │               │
│        └───────────────┬─────────────────────┘               │
│                        ▼                                     │
└────────────────────────┼─────────────────────────────────────┘
                         │
                         │ 상담 예약 요청
                         │ (고객정보 + 마케팅동의)
                         ▼
┌──────────────────────────────────────────────────────────────┐
│                    TM 센터 (수탁사)                          │
│                      http://localhost:5178                   │
├──────────────────────────────────────────────────────────────┤
│                                                              │
│   [상담 예약 접수] ──► [상담원 배정] ──► [상담 진행]         │
│                                                              │
└──────────────────────────────────────────────────────────────┘
```

### 연동 포인트

| 구분 | 위탁사 (Continue Bank) | TM 센터 |
|------|------------------------|--------|
| 역할 | 상담 신청 UI 제공 | 상담 예약 관리 |
| 본인인증 | V-PASS 호출 | 인증 결과 수신 |
| 마케팅 동의 | 동의 UI 제공 | 동의 정보 저장 |
| 고객 정보 | 정보 수집 | 정보 활용 |

### API 연동 방식

위탁사에서 TM 센터로 상담 예약을 전송합니다:

```javascript
// Continue Bank (위탁사) 프론트엔드
const response = await fetch('http://localhost:8082/api/v1/consultations/request', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    customerName: '홍길동',
    phoneNumber: '01012345678',
    productInterest: '적금',
    preferredTime: '오후',
    tokenId: 'vpass-token-id',
    marketingAgreed: true,
    marketingSmsAgreed: true
  })
});
```

## 🗄 데이터베이스 구조

### ERD (Entity Relationship Diagram)

```
┌─────────────────────────────────────┐
│      consultations 테이블            │
├─────────────────────────────────────┤
│ id (PK)                    BIGINT   │
│ customer_name              VARCHAR  │
│ phone_number               VARCHAR  │
│ product_interest           VARCHAR  │ ← 관심 상품
│ preferred_time             VARCHAR  │ ← 희망 상담 시간
│ status                     VARCHAR  │ ← PENDING/COMPLETED/CANCELLED
│ notes                      TEXT     │ ← 상담 메모
│                                     │
│ ─── 마케팅 동의 정보 ───            │
│ marketing_agreed           BOOLEAN  │ ← 마케팅 수신 동의
│ marketing_sms_agreed       BOOLEAN  │ ← SMS 수신 동의
│ marketing_email_agreed     BOOLEAN  │ ← 이메일 수신 동의
│ marketing_push_agreed      BOOLEAN  │ ← 푸시 알림 동의
│                                     │
│ ─── V-PASS 연동 정보 ───            │
│ token_id                   VARCHAR  │ ← V-PASS tokenId
│ verified                   BOOLEAN  │ ← 본인인증 완료 여부
│ verified_at                DATETIME │ ← 인증 완료 시각
│                                     │
│ created_at                 DATETIME │
│ updated_at                 DATETIME │
└─────────────────────────────────────┘
```

### 주요 테이블 설명

#### consultations 테이블
상담 예약 정보를 저장합니다.

**주요 컬럼:**
- `customer_name`: 고객 이름
- `phone_number`: 휴대폰번호
- `product_interest`: 관심 상품 (예: 적금, 대출, 카드 등)
- `preferred_time`: 희망 상담 시간 (예: 오전, 오후, 저녁)
- `status`: 상담 상태
  - `PENDING`: 대기 중
  - `COMPLETED`: 완료
  - `CANCELLED`: 취소
- `marketing_agreed`: 마케팅 수신 전체 동의
- `marketing_sms_agreed`: SMS 수신 동의
- `marketing_email_agreed`: 이메일 수신 동의
- `marketing_push_agreed`: 푸시 알림 수신 동의
- `token_id`: V-PASS 인증 토큰
- `verified`: 본인인증 완료 여부

## 🔄 상담 예약 프로세스

### 전체 플로우

```
[위탁사] → 상품 페이지에서 "상담 신청" 클릭
              │
              ▼
        [상담 신청 페이지]
              │
              ├─ 이름 입력
              ├─ 휴대폰번호 입력
              ├─ 관심 상품 선택
              ├─ 희망 상담 시간 선택
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
              │
              ▼
        위탁사로 복귀
              │
              ▼
        [마케팅 동의 페이지]
              │
              ├─ 마케팅 수신 동의 (선택)
              ├─ SMS 수신 동의
              ├─ 이메일 수신 동의
              ├─ 푸시 알림 수신 동의
              │
              ▼
        POST /api/v1/consultations/request
              │
              ├─ 상담 예약 정보 저장
              ├─ 마케팅 동의 정보 저장
              ├─ status = PENDING
              │
              ▼
        상담 예약 완료 → 대시보드
```

### TM 센터 관리자 플로우

```
[TM 센터 관리자] → 로그인
                      │
                      ▼
                [상담 예약 목록]
                      │
                      ├─ 대기 중 예약 조회
                      ├─ 완료된 예약 조회
                      ├─ 취소된 예약 조회
                      │
                      ▼
                [예약 상세 보기]
                      │
                      ├─ 고객 정보 확인
                      ├─ 마케팅 동의 정보 확인
                      ├─ 상담 메모 작성
                      ├─ 상태 변경 (PENDING → COMPLETED)
                      │
                      ▼
                상담 완료 처리
```

## 📡 API 엔드포인트

### 1. POST /api/v1/consultations/request
상담 예약 요청 (위탁사 → TM 센터)

**Request Body:**
```json
{
  "customerName": "홍길동",
  "phoneNumber": "01012345678",
  "productInterest": "적금",
  "preferredTime": "오후",
  "tokenId": "550e8400-e29b-41d4-a716-446655440000",
  "marketingAgreed": true,
  "marketingSmsAgreed": true,
  "marketingEmailAgreed": false,
  "marketingPushAgreed": true
}
```

**Response:**
```json
{
  "id": 1,
  "status": "PENDING",
  "message": "상담 예약이 접수되었습니다."
}
```

**처리 과정:**
1. V-PASS tokenId 검증
2. 상담 예약 정보 저장
3. 마케팅 동의 정보 저장
4. status = PENDING 설정

### 2. GET /api/v1/consultations
상담 예약 목록 조회 (TM 센터 관리자)

**Query Parameters:**
- `status`: PENDING | COMPLETED | CANCELLED (선택)

**Response:**
```json
[
  {
    "id": 1,
    "customerName": "홍길동",
    "phoneNumber": "010****5678",
    "productInterest": "적금",
    "preferredTime": "오후",
    "status": "PENDING",
    "marketingAgreed": true,
    "createdAt": "2026-01-28T15:00:00"
  }
]
```

### 3. GET /api/v1/consultations/{id}
상담 예약 상세 조회

**Response:**
```json
{
  "id": 1,
  "customerName": "홍길동",
  "phoneNumber": "01012345678",
  "productInterest": "적금",
  "preferredTime": "오후",
  "status": "PENDING",
  "notes": "",
  "marketingAgreed": true,
  "marketingSmsAgreed": true,
  "marketingEmailAgreed": false,
  "marketingPushAgreed": true,
  "verifiedAt": "2026-01-28T14:55:00",
  "createdAt": "2026-01-28T15:00:00"
}
```

### 4. PUT /api/v1/consultations/{id}
상담 예약 정보 수정 (상태 변경, 메모 추가)

**Request Body:**
```json
{
  "status": "COMPLETED",
  "notes": "상담 완료. 적금 상품 가입 안내 완료."
}
```

**Response:**
```json
{
  "id": 1,
  "status": "COMPLETED",
  "message": "상담 정보가 업데이트되었습니다."
}
```

### 5. DELETE /api/v1/consultations/{id}
상담 예약 취소

**Response:**
```json
{
  "message": "상담 예약이 취소되었습니다."
}
```

## 🔗 V-PASS 연동

### 본인인증 플로우

TM 센터는 위탁사를 통해 V-PASS 본인인증을 수행합니다.

```
[위탁사] → POST /trustee-api/v1/auth/init
              │
              ├─ clientData: "01012345678"
              ├─ name: "홍길동"
              │
              ▼
        V-PASS에서 tokenId 생성
              │
              ▼
        V-PASS 인증 페이지로 리다이렉트
              │
              ▼
        사용자 본인인증 완료
              │
              ▼
        위탁사로 복귀
        (verified=true, tokenId)
              │
              ▼
        TM 센터로 상담 예약 전송
        (tokenId 포함)
```

### tokenId 검증

TM 센터는 받은 tokenId를 V-PASS에 검증 요청합니다.

```java
@Service
public class ConsultationService {
    
    public void verifyToken(String tokenId) {
        // V-PASS API 호출
        RestTemplate restTemplate = new RestTemplate();
        String url = "http://localhost:8086/api/v1/auth/verify/" + tokenId;
        
        ResponseEntity<VerifyResponse> response = 
            restTemplate.getForEntity(url, VerifyResponse.class);
        
        if (!response.getBody().isVerified()) {
            throw new RuntimeException("본인인증이 완료되지 않았습니다.");
        }
    }
}
```

## 📧 마케팅 동의 관리

### 동의 항목

TM 센터에서 수집하는 마케팅 동의 정보:

1. **마케팅 수신 전체 동의** (`marketing_agreed`)
   - 전체 마케팅 수신 동의 여부

2. **SMS 수신 동의** (`marketing_sms_agreed`)
   - 문자 메시지를 통한 마케팅 정보 수신

3. **이메일 수신 동의** (`marketing_email_agreed`)
   - 이메일을 통한 마케팅 정보 수신

4. **푸시 알림 수신 동의** (`marketing_push_agreed`)
   - 앱 푸시 알림을 통한 마케팅 정보 수신

### 동의 철회

사용자는 위탁사(Continue Bank)의 "동의 내역 관리" 페이지에서 마케팅 동의를 철회할 수 있습니다.

**철회 시 TM 센터 처리:**
1. 위탁사에서 TM 센터로 철회 요청 전송
2. TM 센터에서 해당 사용자의 마케팅 동의 정보 업데이트
3. 마케팅 발송 대상에서 제외

## 📝 환경 변수

### Backend (`application.properties`)
```properties
# Database
spring.datasource.url=jdbc:mysql://localhost:3306/tm_center_db
spring.datasource.username=root
spring.datasource.password=your_password

# Server
server.port=8082

# JPA
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# V-PASS API
vpass.api.url=http://localhost:8086
```

### Frontend (`.env`)
```env
VITE_API_BASE_URL=http://localhost:8082
VITE_VPASS_URL=http://localhost:8086
```

## 🎨 주요 기능

### 1. 상담 예약 관리

**대시보드:**
- 대기 중 예약 수
- 완료된 예약 수
- 취소된 예약 수
- 최근 예약 목록

**예약 목록:**
- 필터링 (상태별, 날짜별)
- 검색 (고객명, 휴대폰번호)
- 정렬 (최신순, 오래된순)

**예약 상세:**
- 고객 정보 표시
- 마케팅 동의 정보 표시
- 상담 메모 작성
- 상태 변경 (PENDING → COMPLETED)

### 2. 마케팅 동의 관리

**동의 정보 조회:**
- 고객별 마케팅 동의 현황
- 채널별 동의 현황 (SMS, 이메일, 푸시)

**마케팅 발송 대상 관리:**
- 동의한 고객만 발송 대상에 포함
- 철회한 고객 자동 제외

## 🧪 테스트

### 상담 예약 테스트 시나리오

**준비:**
1. 위탁사 (Continue Bank) 로그인
2. 상품 페이지 접속

**테스트 플로우:**
1. "상담 신청" 버튼 클릭
2. 이름, 휴대폰번호 입력
3. 관심 상품 선택 (예: 적금)
4. 희망 상담 시간 선택 (예: 오후)
5. 본인인증 → V-PASS 페이지 이동
6. OTP 인증 완료
7. 위탁사로 복귀
8. 마케팅 동의 선택
9. 상담 예약 완료
10. TM 센터에서 예약 확인

### TM 센터 관리자 테스트

1. TM 센터 로그인 (http://localhost:5178)
2. 상담 예약 목록 확인
3. 대기 중 예약 클릭
4. 고객 정보 및 마케팅 동의 정보 확인
5. 상담 메모 작성
6. 상태를 "완료"로 변경
7. 저장

## 📊 통계 및 리포트

### 상담 통계

**일별 통계:**
- 접수된 예약 수
- 완료된 상담 수
- 취소된 예약 수

**상품별 통계:**
- 상품별 관심도 (적금, 대출, 카드 등)

**시간대별 통계:**
- 희망 상담 시간대 분포

### 마케팅 동의 통계

**채널별 동의율:**
- SMS 동의율
- 이메일 동의율
- 푸시 알림 동의율

## 🔐 보안 고려사항

### 개인정보 보호

**휴대폰번호 마스킹:**
- 목록 화면: `010****5678`
- 상세 화면: 전체 번호 표시 (권한 필요)

**접근 권한 관리:**
- 관리자만 상담 예약 정보 조회 가능
- 일반 직원은 자신이 담당한 상담만 조회

### 데이터 보관 정책

**상담 기록:**
- 상담 완료 후 3년간 보관
- 법적 요구사항 준수

**마케팅 동의 정보:**
- 철회 시 즉시 반영
- 철회 기록 보관 (법적 증거)

---

**TM 센터** - 고객과의 신뢰를 최우선으로 하는 상담 서비스 📞
