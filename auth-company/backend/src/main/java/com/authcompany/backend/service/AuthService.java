package com.authcompany.backend.service;

import com.authcompany.backend.dto.AuthRequest;
import com.authcompany.backend.dto.AuthResponse;
import com.authcompany.backend.dto.CallbackRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @class AuthService
 * @description 본인인증 관련 비즈니스 로직을 처리하는 서비스 클래스입니다.
 * 실제 금융사 연동 대신 Mock 인증 로직과 콜백 처리를 구현합니다.
 * 의도된 보안 취약점: 인증 토큰 서명 검증 미흡, Rate Limit 없음.
 */
@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    // 위탁사의 콜백 URL (환경변수 또는 설정 파일에서 로드)
    @Value("${client.callback.url:http://localhost:3000/callback}") // 실제 위탁사 콜백 URL로 변경 필요
    private String clientCallbackUrl;

    private final RestTemplate restTemplate;

    public AuthService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * @method requestAuth
     * @description 본인인증 요청을 처리하는 Mock 로직입니다.
     * 항상 성공 응답을 반환하며, 교육용 Mock 인증 토큰을 생성합니다.
     * @param request - AuthRequest DTO (이름, 휴대폰 번호)
     * @return AuthResponse DTO (성공 여부, 메시지, Mock 인증 토큰)
     */
    public AuthResponse requestAuth(AuthRequest request) {
        logger.info("[본인인증 요청] 사용자: {}, 휴대폰: {}", request.getName(), request.getPhoneNumber());

        // [보안 취약점] 인증 로직 Mock 처리:
        // 실제 금융권에서는 신뢰할 수 있는 본인인증 기관(ISP)과의 연동,
        // 사용자 데이터의 안전한 처리 및 검증 과정이 필수적입니다.
        // 여기서는 항상 성공한다고 가정하고 Mock 토큰을 발급합니다.
        boolean isAuthSuccess = true; // Mock: 항상 성공
        String authToken = generateMockAuthToken(request.getName());

        String message = isAuthSuccess ? "본인인증 요청 성공" : "본인인증 요청 실패";
        logger.info("[본인인증 처리 결과] 사용자: {}, 결과: {}", request.getName(), message);

        // 콜백 API 호출은 비동기로 처리하거나, 별도의 스레드에서 처리하는 것이 좋습니다.
        // 여기서는 예시를 위해 동기적으로 호출합니다.
        sendAuthCallback(isAuthSuccess, message, UUID.randomUUID().toString());

        return new AuthResponse(isAuthSuccess, message, authToken);
    }

    /**
     * @method generateMockAuthToken
     * @description 교육용 Mock 인증 토큰을 생성합니다.
     * 실제 JWT(JSON Web Token)와 유사한 형태를 가지지만, 서명 검증 로직은 의도적으로 단순화합니다.
     * [보안 취약점] 인증 토큰 서명 검증 미흡:
     * 실제 JWT는 서명(Signature)을 통해 토큰의 무결성 및 위변조 여부를 검증합니다.
     * 여기서는 단순히 Base64 인코딩된 문자열을 결합하는 형태로, 서명 검증 로직이 없습니다.
     * 이는 토큰 위변조에 취약합니다.
     * @param username - 사용자 이름
     * @return Mock 인증 토큰 문자열
     */
    private String generateMockAuthToken(String username) {
        String header = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";
        String payload = String.format("{\"sub\":\"%s\",\"exp\":%d,\"iat\":%d}",
                username,
                System.currentTimeMillis() / 1000 + 3600, // 1시간 유효
                System.currentTimeMillis() / 1000);

        String encodedHeader = java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(header.getBytes());
        String encodedPayload = java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(payload.getBytes());

        // [보안 취약점] 서명 로직 누락/미흡:
        // 실제 JWT는 `HMACSHA256(base64UrlEncode(header) + "." + base64UrlEncode(payload), secret)`
        // 와 같은 형태로 서명을 생성합니다. 여기서는 서명 없이 결합합니다.
        String signature = "mock-signature"; // Mock 서명

        return String.format("%s.%s.%s", encodedHeader, encodedPayload, signature);
    }

    /**
     * @method sendAuthCallback
     * @description 위탁사 콜백 URL로 인증 결과를 전송하는 Mock 로직입니다.
     * [보안 취약점] 콜백 API 인증 없음:
     * 실제 금융권 콜백 API는 상호 인증 (예: SSL Client Certificate), IP 화이트리스트,
     * 메시지 서명(HMAC-SHA256) 등 강력한 보안 메커니즘이 필수적입니다.
     * 여기서는 별도의 인증 없이 요청을 수락하고, 간단한 로깅만 수행합니다.
     * @param success - 인증 성공 여부
     * @param message - 결과 메시지
     * @param transactionId - 트랜잭션 ID
     */
    public void sendAuthCallback(boolean success, String message, String transactionId) {
        logger.info("[콜백 전송 준비] 위탁사 URL: {}, 결과: {}, 메시지: {}, 트랜잭션ID: {}",
                clientCallbackUrl, success, message, transactionId);

        CallbackRequest callbackRequest = new CallbackRequest(success, message, transactionId);

        try {
            // [보안 취약점] Rate Limit 없음:
            // 콜백 API 호출에 대한 Rate Limit이 없으면 DoS 공격에 취약할 수 있습니다.
            // 실제 시스템에서는 초당/분당 호출 제한 등의 로직이 필요합니다.
            ResponseEntity<String> response = restTemplate.postForEntity(clientCallbackUrl, callbackRequest, String.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                logger.info("[콜백 전송 성공] 위탁사 응답: {}", response.getBody());
            } else {
                logger.warn("[콜백 전송 실패] 위탁사 응답 코드: {}, 바디: {}", response.getStatusCode(), response.getBody());
            }
        } catch (Exception e) {
            logger.error("[콜백 전송 오류] 위탁사 콜백 중 예외 발생: {}", e.getMessage());
        }
    }

    /**
     * @method healthCheck
     * @description 백엔드 헬스 체크 엔드포인트 로직입니다.
     * @return String "OK"
     */
    public String healthCheck() {
        logger.info("[헬스 체크] 백엔드 서비스 상태 OK.");
        return "OK";
    }
}
