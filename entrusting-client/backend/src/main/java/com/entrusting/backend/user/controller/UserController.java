package com.entrusting.backend.user.controller;

import com.entrusting.backend.user.dto.AuthCallbackRequest;
import com.entrusting.backend.user.dto.LoginRequest;
import com.entrusting.backend.user.dto.RegisterRequest;
import com.entrusting.backend.user.entity.User;
import com.entrusting.backend.user.service.UserService;
import com.entrusting.backend.user.service.S2SAuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class UserController {

    private final UserService userService;
    private final S2SAuthService s2sAuthService;
    private final org.springframework.web.client.RestTemplate restTemplate;

    @org.springframework.beans.factory.annotation.Value("${trustee.api.base-url}")
    private String trusteeApiBaseUrl;

    public UserController(UserService userService, S2SAuthService s2sAuthService,
            org.springframework.web.client.RestTemplate restTemplate) {
        this.userService = userService;
        this.s2sAuthService = s2sAuthService;
        this.restTemplate = restTemplate;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequest request) {
        try {
            // [Server-to-Server 검증]
            String tokenId = request.getTokenId();
            if (tokenId == null || tokenId.isEmpty()) {
                throw new IllegalArgumentException("본인인증 토큰이 누락되었습니다. 다시 시도해 주세요.");
            }

            java.util.Map<String, Object> verification = s2sAuthService.verifyTokenWithTrustee(tokenId);
            if (verification == null || !"COMPLETED".equals(verification.get("status"))) {
                throw new IllegalArgumentException("본인인증이 완료되지 않았거나 유효하지 않은 토큰입니다.");
            }

            // [보안 추가] 인증된 실명/번호와 가입 정보가 일치하는지 최종 확인
            String verifiedName = (String) verification.get("name");
            String verifiedCi = (String) verification.get("ci");
            
            if (verifiedName != null && !verifiedName.equals(request.getName())) {
                throw new IllegalArgumentException("본인인증 성명과 가입 성명이 일치하지 않습니다.");
            }

            // [추가] 인증된 CI 정보를 요청 객체에 반영 (UserService에서 중복 체크 시 사용)
            request.setCi(verifiedCi);

            // [수정] 본인인증이 완료되었으므로 isVerified=true 설정하여 가입
            request.setVerified(true);
            userService.registerUser(request);
            return ResponseEntity.ok("User registered successfully");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("서버 처리 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            User user = userService.loginUser(request);
            java.util.Map<String, String> response = new java.util.HashMap<>();
            response.put("status", "success");
            response.put("username", user.getUsername());
            
            // [수정] 성명과 휴대폰번호 복호화 후 전달
            String encryptedName = user.getName();
            String encryptedPhone = user.getPhoneNumber();
            String decryptedName = com.entrusting.backend.common.util.EncryptionUtil.decrypt(encryptedName);
            String decryptedPhone = com.entrusting.backend.common.util.EncryptionUtil.decrypt(encryptedPhone);
            
            System.out.println("[ENTRUSTING-DEBUG] Login Response - Name(Enc): [" + encryptedName + "] -> Dec: [" + decryptedName + "]");
            System.out.println("[ENTRUSTING-DEBUG] Login Response - Phone(Enc): [" + encryptedPhone + "] -> Dec: [" + decryptedPhone + "]");

            response.put("name", decryptedName);
            response.put("phoneNumber", decryptedPhone);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/callback")
    public org.springframework.http.ResponseEntity<?> authCallback(@RequestBody AuthCallbackRequest request) {
        System.out.println("[ENTRUSTING-DEBUG] Callback Request Received - Phone: " + request.getPhoneNumber()
                + ", Token: " + request.getTokenId());
        java.util.Map<String, String> response = new java.util.HashMap<>();
        try {
            // [보안 강화] 수탁사 서버에 직접 토큰 상태 확인 (중앙 설정값 사용)
            String trusteeUrl = trusteeApiBaseUrl + "/api/v1/auth/status/" + request.getTokenId();

            java.util.Map statusResponse = null;
            try {
                statusResponse = restTemplate.getForObject(trusteeUrl, java.util.Map.class);
                System.out.println("[ENTRUSTING-DEBUG] Trustee Response: " + statusResponse);

                if (statusResponse == null || !"COMPLETED".equals(statusResponse.get("status"))) {
                    throw new Exception("인증이 완료되지 않았거나 토큰이 유효하지 않습니다. (Status: " +
                            (statusResponse != null ? statusResponse.get("status") : "null") + ")");
                }
            } catch (Exception e) {
                System.err.println("[ENTRUSTING-DEBUG] Trustee Connection Error: " + e.getMessage());
                throw new Exception("수탁사 인증 서버 연결 실패: " + e.getMessage());
            }

            userService.updateUserVerifiedStatus(request.getPhoneNumber(), true);
            System.out.println("[ENTRUSTING-DEBUG] Callback Success for: " + request.getPhoneNumber());
            response.put("status", "success");
            response.put("message", "verified successfully");

            // [수정] 수탁사에서 받은 실제 이름을 사용 (홍길동 하드코딩 제거)
            String verifiedName = (statusResponse != null) ? (String) statusResponse.get("name") : null;
            response.put("name", verifiedName != null ? verifiedName : "인증완료");

            return org.springframework.http.ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("[ENTRUSTING-DEBUG] Callback Error: " + e.getMessage());
            response.put("status", "error");
            response.put("message", "Verification failed: " + e.getMessage());
            return org.springframework.http.ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/find-id")
    public ResponseEntity<String> findId(@RequestParam String phoneNumber, @RequestParam String name) {
        try {
            String username = userService.findUsernameByPhoneNumber(phoneNumber, name);
            return ResponseEntity.ok(username);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestParam String username, @RequestParam String newPassword,
            @RequestParam String phoneNumber, @RequestParam String name) {
        try {
            userService.resetPassword(username, newPassword, phoneNumber, name);
            return ResponseEntity.ok("Password reset successfully");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
