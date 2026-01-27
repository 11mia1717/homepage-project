# 본인인증 시스템 인증 흐름 및 구조 설명 (수탁사 & 위탁사)

이 문서는 **수탁사(Trustee, V-PASS Provider)**와 **위탁사(Entruster, Entrusting Client)** 간의 본인인증 처리 흐름과 상호 연관성을 설명합니다.

## 1. 주요 역할 및 용어
- **수탁사 (Trustee/Provider - vpass-provider)**: 본인인증 서비스를 실질적으로 제공하는 주체입니다. 인증번호를 생성하고, 통신사 정보와 대조하여 본인인지 확인합니다.
- **위탁사 (Entruster/Client - entrusting-client)**: 본인인증 서비스를 이용하는 고객사(쇼핑몰, 금융 앱 등)입니다. 수탁사에게 인증을 맡기고 그 결과만 전달받습니다.
- **인증번호 (OTP)**: 수탁사가 생성하며, 사용자의 휴대폰으로 전달되는 6자리 숫자입니다.

## 2. 전체 인증 흐름도 (Sequence Diagram) - JWT Flow

```mermaid
sequenceDiagram
    autonumber
    actor User as 사용자
    participant Client as 위탁사 (Entrusting Client)
    participant Trustee as 수탁사 (V-PASS Provider)
    participant Carrier as 통신사 (Mock DB)

    Note over User, Carrier: [1단계: 인증 초기화]
    User->>Client: 본인인증 요청
    Client->>Client: auth_request_id (UUID) 생성
    Client->>Trustee: 인증 세션 생성 요청 (auth_request_id 포함)
    Trustee-->>Trustee: 내부 토큰(jti) 및 OTP 생성, auth_request_id 저장
    Trustee-->>Client: tokenId 및 OTP 반환
    
    Note over User, Carrier: [2단계: 인증 수행]
    User->>Client: 인증번호 및 정보 입력
    Client->>Trustee: 인증번호 확인 요청
    Trustee->>Carrier: 실명 대조
    
    alt 검증 성공
        Trustee-->>Trustee: 상태 'COMPLETED' 변경
        Trustee-->>Client: 성공 응답
    else 검증 실패
        Trustee-->>Client: 실패 응답
    end

    Note over User, Carrier: [3단계: JWT 발급 및 서명 검증]
    Client->>Trustee: 최종 상태 확인 (GET /status)
    Trustee-->>Trustee: 상태가 COMPLETED면 JWT 생성 (JTI=tokenId, Claim=auth_request_id)
    Trustee-->>Client: JWT 토큰(accessToken) 포함 응답
    Client->>Client: JWT 서명 검증 & auth_request_id 대조
    Client->>User: "인증 완료"
```

## 3. 핵심 변경 사항 (JWT 적용)

### Q1. JWT는 언제 발급되나요?
인증 절차가 모두 완료되어 상태가 `COMPLETED`가 되었을 때, 수탁사(`AuthService.getAuthStatus`)에서 발급합니다.
이 토큰에는 다음 정보가 포함됩니다:
- **jti (JWT ID)**: 수탁사 내부의 고유 토큰 ID
- **claim (auth_request_id)**: 위탁사가 처음에 보낸 요청 ID (매핑용)

### Q2. 위탁사는 무엇을 검증하나요?
위탁사는 수탁사로부터 받은 JWT의 **서명(Signature)**을 검증하여 위변조 여부를 확인하고, JWT 내부의 `auth_request_id`가 자신이 보낸 것과 일치하는지 확인하여 거래를 확정합니다.

## 4. 구조적 특징
- **Token 방식**: 수탁사는 인증 완료 후 `tokenId`를 통해 상태를 관리하므로, 위탁사는 실제 사용자의 민감 정보를 직접 들고 있지 않아도 인증 여부를 판단할 수 있습니다.
- **보안 강화**: `entrusting-client/backend`의 `UserController`는 수탁사의 API를 호출하여 토큰 상태가 `COMPLETED`인지 반드시 확인한 후에만 후속 절차(회원가입 등)를 진행합니다.
