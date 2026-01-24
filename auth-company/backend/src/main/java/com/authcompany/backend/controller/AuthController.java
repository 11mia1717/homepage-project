package com.authcompany.backend.controller;

import com.authcompany.backend.dto.AuthRequest;
import com.authcompany.backend.dto.AuthResponse;
import com.authcompany.backend.dto.CallbackRequest;
import com.authcompany.backend.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * @class AuthController
 * @description 본인인증 관련 REST API 엔드포인트를 제공하는 컨트롤러입니다.
 * 클라이언트의 본인인증 요청을 처리하고, 위탁사로의 콜백을 수신하며, 시스템 헬스 체크 기능을 제공합니다.
 * 의도된 보안 취약점: 콜백 API 인증 없음, Rate Limit 없음 (서비스 계층에서 설명).
 */
@RestController
@RequestMapping("/api/auth") // 모든 엔드포인트에 /api/auth prefix 적용
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * @method requestAuth
     * @description 본인인증 요청을 처리하는 엔드포인트입니다.
     * 프론트엔드에서 이름과 휴대폰 번호를 받아 인증 서비스로 전달합니다.
     * 시나리오 2: 수탁사 본인인증 페이지에서 최소 정보(이름, 휴대폰번호) 입력 후 인증 요청 버튼 누름.
     * 시나리오 4: 인증 요청을 백엔드 API로 전달 (POST /api/auth/request).
     * @param request - AuthRequest DTO (이름, 휴대폰 번호)
     * @return AuthResponse DTO (인증 성공/실패 여부, 메시지, Mock 인증 토큰)
     */
    @PostMapping("/request")
    public ResponseEntity<AuthResponse> requestAuth(@RequestBody AuthRequest request) {
        logger.info("[API] 본인인증 요청 수신: {}", request.toString());

        // [보안 취약점] Rate Limit 없음:
        // 이 엔드포인트에 대한 Rate Limit이 없으면 무차별 대입 공격(Brute-force attack) 또는 DoS 공격에 취약할 수 있습니다.
        // 실제 시스템에서는 IP 기반, 사용자 기반 등으로 요청 빈도를 제한하는 로직이 필요합니다.

        AuthResponse response = authService.requestAuth(request);

        // 시나리오 5: 인증 성공/실패 결과를 콜백 API 형태로 위탁사에 전달 (AuthService 내부에서 처리).
        return ResponseEntity.ok(response);
    }

    /**
     * @method receiveCallback
     * @description 위탁사로부터 본인인증 결과를 수신하는 가상의 콜백 엔드포인트입니다.
     * (교육용 목적이며, 실제 시나리오 5에서는 수탁사가 위탁사로 콜백을 보내는 구조이므로,
     * 이 엔드포인트는 실제 콜백 수신이 아니라 콜백 수신 로직을 모의하기 위해 존재합니다.)
     * @param callbackRequest - CallbackRequest DTO (성공 여부, 메시지, 트랜잭션 ID)
     * @return String "Callback received"
     */
    @PostMapping("/callback")
    public ResponseEntity<String> receiveCallback(@RequestBody CallbackRequest callbackRequest) {
        // [보안 취약점] 콜백 API 인증 없음:
        // 이 콜백 엔드포인트에 대한 인증 메커니즘이 없으므로, 아무나 콜백을 보낼 수 있습니다.
        // 이는 데이터 조작, 서비스 거부 등의 공격에 취약합니다.
        // 실제 금융권에서는 상호 인증 (예: SSL Client Certificate), IP 화이트리스트,
        // 메시지 서명(HMAC-SHA256) 등 강력한 보안 메커니즘이 필수적입니다.
        logger.info("[API] 위탁사로부터 콜백 수신 (Mock): {}", callbackRequest.toString());
        // 실제로는 여기에서 콜백 데이터를 처리하고, 필요한 비즈니스 로직을 수행합니다.
        return ResponseEntity.ok("Callback received successfully");
    }

    /**
     * @method healthCheck
     * @description 백엔드 서비스의 헬스 체크 엔드포인트입니다.
     * @return String "OK"
     */
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        logger.info("[API] 헬스 체크 요청 수신.");
        return ResponseEntity.ok(authService.healthCheck());
    }

    // [보안 취약점] 이 구조에서 발생 가능한 보안 취약점 목록 (백엔드 관련) 주석 정리:
    // 1. 인증 토큰 서명 검증 미흡: `AuthService`의 `generateMockAuthToken`에서 서명 검증 로직이 없음.
    // 2. 콜백 API 인증 없음: `/api/auth/callback` 엔드포인트에 대한 인증/인가 메커니즘 부재.
    // 3. Rate Limit 없음: `/api/auth/request` 및 `/api/auth/callback`에 대한 Rate Limit이 없어 DoS 공격에 취약.
    // 4. 입력값 검증 미흡: DTO(`AuthRequest`)에 대한 서버 측 유효성 검증 로직 부재.
    // 5. 민감 정보 로깅: 로그에 민감 정보가 포함될 수 있으며, 로깅 시스템의 보안 취약.
    // 6. CORS 설정 미흡: `SecurityConfig.java`에서 허용 범위를 너무 넓게 설정하거나, 안전하지 않게 설정할 경우.
    // 7. HTTPS 미적용(가정): 실제 배포 시 HTTPS 강제 필요. 현재는 통신이 HTTPS라고 가정함.
    // 8. 에러 메시지 노출: 클라이언트에 상세한 백엔드 에러 메시지를 노출할 경우 정보 유출 가능성.

    // k3s/컨테이너 전제 설명 주석
    // 이 Spring Boot 백엔드 애플리케이션은 컨테이너화되어 k3s 환경에서 동작합니다.
    // 빌드 시 Dockerfile을 통해 이미지가 생성되고,
    // Kubernetes Deployment에 의해 파드가 배포됩니다.
    // Service를 통해 내부적으로 노출되며,
    // Ingress 또는 NodePort를 통해 외부에서 접근 가능하도록 설정될 예정입니다.
}