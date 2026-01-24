# 본인인증 수탁사 웹 시스템 (교육용)

## 프로젝트 개요

이 프로젝트는 금융권 정보보안 컨설턴트 출신 풀스택 개발자의 관점에서 교육용 "본인인증 수탁사 웹 시스템"을 설계하고 구현합니다. 실제 금융사의 수탁사 구조를 단순화하여 React (프론트엔드)와 Spring Boot (백엔드) 기술 스택을 사용하며, k3s 기반 컨테이너 환경에서 동작하는 것을 전제로 합니다. UI 디자인보다는 구조와 보안 취약점 학습에 중점을 둡니다.

## 전체 시나리오

1.  **사용자는 위탁사 웹에서 "본인인증" 버튼을 누른다.**
2.  **수탁사 본인인증 페이지로 리다이렉트된다.**
    *   (프론트엔드: AuthPage.jsx)
3.  **수탁사 페이지에서 최소 정보(이름, 휴대폰번호) 입력 후 "인증 요청" 버튼을 누른다.**
    *   (프론트엔드: AuthPage.jsx)
4.  **인증 요청을 백엔드 API로 전달한다.** (`POST /api/auth/request`)
    *   (프론트엔드: api.js, 백엔드: AuthController.java -> AuthService.java)
5.  **백엔드에서 인증 성공/실패 결과를 콜백 API 형태로 위탁사에 전달한다.** (`POST /api/auth/callback` - Mock)
    *   (백엔드: AuthService.java)
6.  **모든 통신은 HTTPS를 가정하며, 실제 인증 연동은 Mock 처리된다.**

## 구성 요소

### 1. 프론트엔드 (React)

*   **기술 스택**: React, Vite, Axios, React Router Dom
*   **주요 기능**:
    *   본인인증 메인 페이지 (`AuthPage.jsx`): 이름, 휴대폰 번호 입력 폼 및 인증 요청 버튼
    *   인증 결과 페이지 (`ResultPage.jsx`): 인증 성공/실패 메시지 표시
*   **컨테이너 전제**: Dockerfile을 통해 컨테이너화되며, k3s Deployment와 Service, Ingress/NodePort를 통해 배포됩니다.
*   **환경 변수**: `VITE_BACKEND_API_URL`을 통해 백엔드 API 엔드포인트를 분리합니다.

### 2. 백엔드 (Spring Boot)

*   **기술 스택**: Spring Boot, Maven, Lombok, Spring Security, Slf4j (로깅)
*   **REST API**:
    *   `POST /api/auth/request`: 본인인증 요청 처리
    *   `POST /api/auth/callback`: 위탁사로부터의 콜백 수신 (Mock 처리)
    *   `GET /api/health`: 서비스 헬스 체크
*   **주요 특징**:
    *   인증 로직은 Mock 처리되어 항상 성공으로 가정합니다.
    *   Request/Response DTO (`AuthRequest`, `AuthResponse`, `CallbackRequest`)를 명확히 분리합니다.
    *   CORS 설정을 명시적으로 관리합니다 (`SecurityConfig.java`).
*   **컨테이너 전제**: Dockerfile을 통해 컨테이너화되며, k3s Deployment와 Service, Ingress/NodePort를 통해 배포됩니다.

## 이 구조에서 발생 가능한 보안 취약점 목록 (교육용)

이 프로젝트는 교육 목적으로 의도적으로 일부 보안 취약점을 포함하고 있으며, 실제 운영 환경에서는 아래 사항들을 반드시 강화해야 합니다.

### 1. 프론트엔드 (React) 관련 취약점

*   **클라이언트 측 입력값 검증 미흡**: `AuthPage.jsx`에서 필수 필드만 검증하고 상세 형식(예: 휴대폰 번호 정규식) 검증이 부족합니다. (클라이언트 측 검증은 사용자 경험 향상을 위한 것이며, 서버 측 검증은 필수입니다.)
*   **민감 정보 클라이언트 로깅**: `console.log`를 통해 민감 정보 (예: API 요청/응답 데이터) 또는 중요한 응답 (예: 인증 토큰)이 브라우저 개발자 도구에 노출될 수 있습니다.
*   **API 엔드포인트 노출**: `.env` 파일을 통해 백엔드 API 엔드포인트가 클라이언트 번들에 포함되어 쉽게 노출될 수 있습니다. 중요한 API 엔드포인트는 서버 측에서 관리하는 것이 더 안전합니다.
*   **토큰 저장 위치의 부적절함**: `localStorage` 또는 `sessionStorage`에 인증 토큰을 저장할 경우 XSS(Cross-Site Scripting) 공격에 취약해질 수 있습니다. `HttpOnly` 및 `Secure` 속성이 설정된 쿠키에 저장하는 것이 훨씬 안전합니다.
*   **HTTPS 미적용 (가정)**: 모든 통신이 HTTPS로 이루어진다고 가정하지만, 실제 배포 시에는 반드시 HTTPS를 강제하고 SSL/TLS 설정을 강화해야 합니다.

### 2. 백엔드 (Spring Boot) 관련 취약점

*   **인증 토큰 서명 검증 미흡**: `AuthService.java`의 `generateMockAuthToken` 메서드에서 생성되는 Mock JWT는 서명(Signature) 검증 로직이 없습니다. 이는 토큰 위변조에 매우 취약합니다. 실제 JWT는 강력한 암호화 알고리즘으로 서명되어야 하며, 서버는 반드시 이 서명을 검증해야 합니다.
*   **콜백 API 인증 없음**: `/api/auth/callback` 엔드포인트에 대한 인증 또는 인가 메커니즘이 없습니다. 이는 위탁사로 가장한 악의적인 공격자가 임의의 콜백을 전송하여 시스템에 혼란을 주거나 데이터를 조작할 수 있게 합니다. 실제 금융권 콜백은 상호 인증 (예: SSL Client Certificate), IP 화이트리스트, 메시지 서명(HMAC-SHA256) 등 강력한 보안이 필수입니다.
*   **Rate Limit 없음**: `/api/auth/request` 및 `/api/auth/callback`과 같은 엔드포인트에 대한 요청 빈도 제한(Rate Limit)이 없습니다. 이는 무차별 대입 공격(Brute-force attack)이나 DoS(Denial of Service) 공격에 취약하게 만듭니다.
*   **입력값 검증 미흡**: DTO(`AuthRequest`, `CallbackRequest`)에 대한 서버 측 유효성 검증 로직이 부족합니다. 클라이언트 측 검증은 우회될 수 있으므로, 서버 측에서 모든 입력값에 대한 엄격한 유효성 검증(예: Spring `@Valid` 어노테이션 및 Bean Validation)을 수행해야 합니다.
*   **민감 정보 로깅**: `application.properties` 설정에 따라 로그에 민감 정보가 포함될 수 있으며, 로깅 시스템 자체가 보안적으로 취약할 경우 정보 유출 위험이 있습니다. 실제 환경에서는 민감 정보 마스킹, 최소한의 로깅 레벨 유지 등의 정책이 필요합니다.
*   **CORS 설정 미흡**: `SecurityConfig.java`의 `allowedOrigins` 설정이 개발/교육 편의를 위해 넓게 허용되어 있습니다. 실제 운영 환경에서는 프론트엔드의 도메인 또는 IP를 명확히 지정하여 XSS 및 CSRF 공격 가능성을 줄여야 합니다.
*   **CSRF 보호 비활성화**: `SecurityConfig.java`에서 CSRF 보호를 비활성화했습니다. 이는 `session` 기반 인증에 취약하며, REST API에서는 토큰 기반 인증 사용 시 고려할 사항이 있지만, 적절한 보호 메커니즘은 항상 중요합니다.
*   **에러 메시지 노출**: API 응답으로 클라이언트에 상세한 백엔드 에러 메시지(스택 트레이스 등)를 직접 노출할 경우, 시스템 내부 구조나 취약점에 대한 정보가 유출될 수 있습니다. 일반적이고 사용자 친화적인 에러 메시지를 제공해야 합니다.

## k3s / 컨테이너 전제

*   React 프론트엔드와 Spring Boot 백엔드 애플리케이션은 각각 Dockerfile을 통해 컨테이너 이미지로 빌드됩니다.
*   빌드된 컨테이너 이미지는 k3s(Kubernetes) 환경에서 Deployment 리소스를 통해 파드(Pod)로 배포됩니다.
*   애플리케이션은 Kubernetes Service를 통해 클러스터 내부에서 네트워크로 노출됩니다.
*   클러스터 외부에서는 Ingress 리소스 또는 Service의 NodePort 유형을 통해 프론트엔드와 백엔드 서비스에 접근할 수 있도록 구성될 예정입니다. (실제 YAML 파일은 이 프로젝트의 범위를 벗어나므로 코드 내 주석으로만 설명합니다.)