# Project Continuity Memo (2026-01-26)

이 파일은 현재까지 진행된 모든 작업 내역과 시스템 설정 상태를 요약하여 다시 방문했을 때 즉각적인 학습이 가능하도록 한 것입니다.

## 1. 최근 작업 요약
- **Server-to-Server (S2S) 인증 완료:** 수탁사(`vpass-provider`)와 위탁사(`entrusting-client`) 서버 간의 직접 토큰 검증 로직 구현 완료. 🛡️ ㅠ    - `AuthController` (/api/v1/auth/verify/{tokenId}) 추가
    - `S2SAuthService` (Entrusting Backend) 추가
    - 계좌 생성 시 최종 검증 강제화
- **비즈니스 로직 고도화:** 가입 축하금(10,000원)을 **첫 번째 계좌**에만 지급하도록 서버/클라이언트 로직 수정.
- **보안 및 UI 개선:** 
    - 본인인증 단계에서 **통신사(Carrier) 대조** 로직 추가.
    - 인증 버튼의 상태(비활성화/로딩) 로직을 실제 검증 단계와 동기화.
    - 대시보드 웰컴 배너 제거.
- **E2E 연동 및 UI 고도화:** 
    - **S2S 회원가입 검증:** 회원가입 시에도 수탁사 토큰을 서버 간 통신으로 검증하여 무단 가입 원천 차단.
    - **Premium 에러 모달:** 모든 주요 페이지(로그인, 가입, 계좌개설)에 '금융권 스타일'의 `AlertModal` 공통 컴포넌트 적용.
- **시스템 견고함(Robustness) 강화:** 
    - **Login Guard:** 로그인 없이 대시보드 주소 직접 진입 시 로그인 페이지로 자동 리다이렉트.
    - **OTP 3분 제한:** 프론트엔드뿐만 아니라 백엔드에서도 3분이 경과한 토큰은 인증 거부.
    - **PIN 5회 오류 제한:** 계좌 비밀번호 5회 연속 오류 시 계좌 상태를 `SUSPENDED`로 변경하고 거래 차단.

## 2. 시스템 환경 및 이슈 해결 내역
- **데이터베이스:** H2 예약어 충돌 이슈 해결을 위해 `User` 테이블 이름을 **`site_users`**로 변경함.
- **포트 구성:**
    - 위탁사 (Continue - Entrusting Client): 8085 (Backend), [http://localhost:5175](http://localhost:5175) (Frontend)
    - 수탁사 (V-PASS Provider): 8086 (Backend), [http://localhost:5176](http://localhost:5176) (Frontend) / vpass-provider ㅠ- **명령어 환경:** 
    - `netstat` 등 시스템 명령어 사용을 위해 `C:\Windows\System32`를 PATH에 임시 등록함.
    - `taskkill` 명령어를 통해 좀비 프로세스(Java)를 정리하여 포트 충돌 방지 로직 구축함.

## 3. 본인인증 테스트용 데이터 리스트 (MockCarrierDatabase)
본인인증 테스트 시 아래 리스트와 **[성명 / 전화번호 / 통신사]**가 모두 일치해야 인증이 성공합니다.

| 성명 | 전화번호 | 통신사 |
| :--- | :--- | :--- |
| **김중수** | 010-9511-9924 | SKT |
| **방수진** | 010-8717-6882 | KT |
| **김은수** | 010-5133-7437 | LG U+ |
| **이광진** | 010-3065-9693 | 알뜰폰 (ALDDLE) |
| **임혜진** | 010-3731-5819 | SKT |
| **전용준** | 010-5047-0664 | KT |
| **김유진** | 010-9287-7379 | LG U+ |
| **장민아** | 010-4932-8977 | SKT |
| **이승원** | 010-9212-8221 | KT |
| **홍길동** | 010-0000-0000 | SKT |

## 4. 남아있는 과제 / 다음 단계
- [ ] 현재 구현된 S2S 로직의 실제 데이터 일치 여부(이름, 번호) 세부 필드 검증 강화.
- [ ] 운영 환경을 위한 실제 DB(PostgreSQL/MySQL) 전환 준비.
- [ ] 위탁사-수탁사 간 CORS 및 세션 공유 정책 최종 검증.

상세 구현 내역은 [task.md](file:///C:/Users/ez/.gemini/antigravity/brain/d3de4917-837d-40e0-ac80-e2ec71586dba/task.md)와 [walkthrough.md](file:///C:/Users/ez/.gemini/antigravity/brain/d3de4917-837d-40e0-ac80-e2ec71586dba/walkthrough.md)에 기록되어 있습니다.
