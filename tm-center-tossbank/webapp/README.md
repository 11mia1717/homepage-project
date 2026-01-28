# TM Center - 토스뱅크 콜센터 수탁사 관리 시스템

토스뱅크 스타일의 콜센터 수탁사 관리 시스템입니다. 개인정보보호법 준수를 위한 마케팅 동의, 제3자 제공 동의, 자동 파기 등의 기능을 포함합니다.

## 🎯 프로젝트 개요

- **목적**: 금융권 콜센터 수탁사의 개인정보 처리 및 TM 상담 관리
- **디자인**: 토스뱅크 디자인 시스템 적용
- **기술 스택**: Spring Boot 3.2 + React 18 + TypeScript

## ✅ 구현된 기능

### 1️⃣ 마케팅 동의 시스템
- 제3자 제공 동의서 별도 작성
- 보유기간 명시 (상담 완료 후 3개월)
- 동의 철회 기능
- 고객 알림 (SMS)

### 2️⃣ TM 상담사 시스템
- 다음 고객 불러오기 (자동 배정)
- 고객 정보 조회 (목적 외 사용 제한 경고)
- 상담 결과 저장
- 녹취 동의 확인
- 재통화 관리

### 3️⃣ 관리자 시스템
- 대시보드 통계
- 개인정보 접근 로그 조회
- 자동/수동 파기 관리

### 4️⃣ 개인정보 보호
- CI 대신 request_id 사용 (최소화)
- 자동 파기 배치 (매일 자정)
- 접근 로그 기록
- 목적 외 사용 제한

## 📁 프로젝트 구조

```
webapp/
├── backend/                    # Spring Boot 백엔드
│   ├── src/main/java/com/tossbank/tmcenter/
│   │   ├── config/            # 설정 (Security, Web)
│   │   ├── controller/        # REST API 컨트롤러
│   │   ├── dto/               # 요청/응답 DTO
│   │   ├── entity/            # JPA 엔티티
│   │   ├── repository/        # JPA Repository
│   │   ├── service/           # 비즈니스 로직
│   │   └── scheduler/         # 배치 작업 (파기)
│   └── src/main/resources/
│       ├── application.yml    # 애플리케이션 설정
│       └── schema.sql         # 데이터베이스 스키마
│
└── frontend/                   # React 프론트엔드
    ├── src/
    │   ├── components/        # UI 컴포넌트
    │   │   ├── layout/        # 레이아웃
    │   │   ├── common/        # 공통 컴포넌트
    │   │   └── ...
    │   ├── pages/             # 페이지 컴포넌트
    │   │   ├── agent/         # 상담사 페이지
    │   │   └── admin/         # 관리자 페이지
    │   ├── services/          # API 서비스
    │   ├── stores/            # 상태 관리 (Zustand)
    │   └── types/             # TypeScript 타입
    └── tailwind.config.js     # 토스 디자인 설정
```

## 🚀 실행 방법

### 백엔드 (Spring Boot)

```bash
cd backend

# 개발 모드 (H2 인메모리 DB)
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# 프로덕션 모드 (MySQL)
./mvnw spring-boot:run -Dspring-boot.run.profiles=prod
```

### 프론트엔드 (React)

```bash
cd frontend

# 의존성 설치
npm install

# 개발 서버 실행
npm run dev

# 빌드
npm run build
```

## 📡 API 명세

### 마케팅 동의 API

| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/api/consent` | 마케팅 동의 처리 |
| POST | `/api/consent/withdraw` | 동의 철회 |
| GET | `/api/consent/history?phone={phone}` | 동의 내역 조회 |
| GET | `/api/consent/agreement/{productName}` | 동의서 내용 조회 |

### TM 상담 API

| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/api/tm/next` | 다음 고객 불러오기 |
| GET | `/api/tm/targets` | 배정된 고객 목록 |
| GET | `/api/tm/targets/{id}` | 고객 상세 조회 |
| POST | `/api/tm/targets/{id}/start` | 상담 시작 |
| POST | `/api/tm/results` | 상담 결과 저장 |
| GET | `/api/tm/callbacks` | 재통화 대상 조회 |

### 관리자 API

| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/admin/dashboard` | 대시보드 통계 |
| GET | `/api/admin/access-logs` | 접근 로그 조회 |
| POST | `/api/admin/destroy` | 수동 파기 실행 |

## 💾 데이터베이스 스키마

### 핵심 테이블

- **users**: 사용자/상담사 정보
- **marketing_requests**: 마케팅 동의 요청
- **tm_targets**: TM 상담 대상 (수탁사 전달용)
- **call_results**: 상담 결과
- **access_logs**: 개인정보 접근 로그
- **customer_notifications**: 고객 알림 이력
- **destruction_logs**: 파기 이력

### 개인정보 보호 컬럼

```sql
-- 보유기간 관련
retention_until DATE        -- 보유 기한
destroyed_yn CHAR(1)        -- 파기 여부
destroyed_at DATETIME       -- 파기 일시

-- 녹취 동의 관련
recording_agreed_yn CHAR(1) -- 녹취 사전 동의
recorded_yn CHAR(1)         -- 실제 녹음 여부

-- 재통화 동의 관련
retry_agreed_yn CHAR(1)     -- 재통화 동의
retry_agreed_at DATETIME    -- 재동의 일시
```

## 🔐 개인정보보호 준수 사항

### ✅ 구현됨

- [x] 제3자 제공 동의서 별도 작성
- [x] 보유기간 명시 (3개월)
- [x] 자동 파기 배치 작업
- [x] 목적 외 사용 제한
- [x] 녹취 사전 안내 및 동의
- [x] CI 전송 최소화 (request_id로 대체)
- [x] 동의 철회 기능
- [x] 상담 결과 고객 통지
- [x] 접근 로그 기록
- [x] 재통화 시 재동의

### 📋 시스템 외 준비사항

- [ ] 위수탁 계약서 체결
- [ ] 개인정보 처리방침 업데이트
- [ ] TM 상담사 교육 (법령 준수)

## 🎨 토스 디자인 시스템

### 컬러

```javascript
colors: {
  toss: {
    blue: { 500: '#3182F6' },    // Primary
    gray: { 900: '#191F28' },    // Text
    green: { 500: '#30C85E' },   // Success
    red: { 500: '#F04452' },     // Error
    yellow: { 500: '#FFAA00' },  // Warning
  }
}
```

### 컴포넌트

- `btn-primary`: 주요 버튼
- `btn-secondary`: 보조 버튼
- `card`: 카드 컨테이너
- `input-toss`: 입력 필드
- `badge-*`: 상태 뱃지

## 🛠 기술 스택

### Backend
- Java 17
- Spring Boot 3.2
- Spring Security
- Spring Data JPA
- Spring Batch
- MySQL / H2
- AWS S3 (녹취 파일)

### Frontend
- React 18
- TypeScript
- Vite
- TailwindCSS
- React Router
- TanStack Query
- Zustand
- Framer Motion
- Recharts

## 📝 환경 변수

### Backend (application.yml)

```yaml
# Database
DB_USERNAME: root
DB_PASSWORD: password

# JWT
JWT_SECRET: your-secret-key

# AWS S3
AWS_ACCESS_KEY: your-access-key
AWS_SECRET_KEY: your-secret-key
AWS_S3_BUCKET: tm-center-recordings
```

### Frontend (.env)

```
VITE_API_URL=http://localhost:8080/api
```

## 🔄 개발 상태

- **Backend**: ✅ 완료 (Spring Boot 코드 작성)
- **Frontend**: ✅ 완료 (React + 토스 디자인)
- **Database**: ✅ 스키마 설계 완료
- **실행 환경**: ⚠️ 별도 서버 필요 (Cloudflare 미지원)

---

© 2024 TM Center - 토스뱅크 콜센터 수탁사 관리 시스템
