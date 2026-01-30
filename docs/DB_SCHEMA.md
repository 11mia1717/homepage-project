# Continue Bank - 데이터베이스 명세서

## 개요

Continue Bank 프로젝트는 두 개의 데이터베이스로 구성됩니다:
- **entrusting_db**: 위탁사 (Continue Bank) 데이터
- **trustee_db**: 수탁사 (SSAP 본인인증) 데이터

---

## 1. entrusting_db (위탁사)

### site_users 테이블

사용자 정보 및 약관 동의 내역을 저장합니다.

| 컬럼명 | 타입 | 설명 | 비고 |
|--------|------|------|------|
| id | BIGINT | PK, 자동 증가 | |
| name | VARCHAR(500) | 이름 | AES-256 암호화 |
| username | VARCHAR(255) | 로그인 ID | |
| password | VARCHAR(255) | 비밀번호 | BCrypt 해시 |
| phone_number | VARCHAR(500) | 휴대폰 번호 | AES-256 암호화 |
| ci | VARCHAR(120) | 연계정보 | UNIQUE, 금융권 공통 식별자 |
| di | VARCHAR(90) | 중복가입확인정보 | 사이트별 고유 |
| is_verified | BOOLEAN | 본인인증 완료 여부 | |
| privacy_agreed_at | DATETIME | 개인정보 동의 일시 | |
| data_expire_at | DATETIME | 데이터 보관 만료일 | 금융거래 5년 |

#### 약관 동의 필드 (필수)

| 컬럼명 | 설명 | 비고 |
|--------|------|------|
| terms_agreed | 이용약관 | |
| privacy_agreed | 개인정보 수집·이용 | |
| unique_id_agreed | 고유식별정보 처리 | |
| credit_info_agreed | 신용정보 조회·제공 | |
| carrier_auth_agreed | SSAP 본인확인서비스 | |
| electronic_finance_agreed | 전자금융거래 기본약관 | |
| monitoring_agreed | 금융거래 모니터링/AML | |

#### 선택 약관 및 마케팅 동의 필드

| 컬럼명 | 설명 | 비고 |
|--------|------|------|
| ssap_provision_agreed | 제휴 TM 센터(Continue Call) 제공 | 2026.01.30 타겟 관리 적용 |
| third_party_provision_agreed | 제3자 정보 제공 동의 | |
| marketing_agreed | 마케팅 정보 수신 동의 | |
| marketing_sms | SMS 마케팅 | |
| marketing_email | 이메일 마케팅 | |
| marketing_push | 푸시 알림 마케팅 | |
| marketing_personal_agreed | 개인맞춤형 상품 추천 | |
| terms_agreed_at | 약관 동의 일시 | |

---

## 2. trustee_db (수탁사)

### auth_token 테이블

본인인증 토큰 정보를 저장합니다.

| 컬럼명 | 타입 | 설명 | 비고 |
|--------|------|------|------|
| token_id | BINARY(16) | PK, UUID | JWT jti |
| auth_request_id | VARCHAR(255) | 위탁사 요청 ID | 거래 매핑용 |
| client_data | VARCHAR(500) | 전화번호 | AES-256 암호화 |
| name | VARCHAR(500) | 이름 | AES-256 암호화 |
| carrier | VARCHAR(50) | 통신사 | SKT/KT/LGU+ |
| otp | VARCHAR(100) | OTP 코드 | 해시 저장 |
| ci | VARCHAR(120) | 연계정보 | 인증 완료 시 생성 |
| di | VARCHAR(90) | 중복가입확인정보 | 인증 완료 시 생성 |
| status | ENUM | 토큰 상태 | PENDING/OTP_SENT/CONFIRMED/USED/EXPIRED |
| created_at | DATETIME | 생성 일시 | |

### carrier_user 테이블

가상 통신사 사용자 Mock 데이터입니다.

| 컬럼명 | 타입 | 설명 | 비고 |
|--------|------|------|------|
| id | BIGINT | PK, 자동 증가 | |
| name | VARCHAR(255) | 이름 | NOT NULL |
| phone_number | VARCHAR(20) | 휴대폰 번호 | UNIQUE |
| carrier | VARCHAR(50) | 통신사 | NOT NULL |

---

## 3. 현재 테스트 데이터

| 이름 | 휴대폰 번호 | 통신사 |
|------|------------|--------|
| 홍길동 | 010-1234-5678 | SKT |
| 김철수 | 010-8765-4321 | KT |
| 이영희 | 010-1111-2222 | LGU+ |
| 홍길순 | 010-3333-4444 | SKT |
| 고길동 | 010-5555-6666 | KT |
| 차은우 | 010-7777-8888 | LGU+ |

---

## 4. 연결 정보

### 개발 환경 (H2 인메모리)
```
spring.datasource.url=jdbc:h2:mem:entrusting_db
spring.datasource.username=sa
spring.datasource.password=
```

### 프로덕션 환경 (MySQL Docker)
```
spring.datasource.url=jdbc:mysql://localhost:3306/entrusting_db
spring.datasource.username=continue
spring.datasource.password=continue12!
```

---

## 5. 보안 고려사항

1. **개인정보 암호화**: name, phone_number, client_data 필드는 AES-256으로 암호화
2. **비밀번호 해시**: BCrypt 알고리즘 사용 (cost factor 10)
3. **CI/DI**: 금융권 표준 본인인증 식별자
4. **데이터 보관 기간**: 금융거래 기록 5년 (개인정보보호법)
5. **OTP 보안**: 해시로 저장, 3분 유효
