package com.entrusting.backend.user.controller;

import com.entrusting.backend.user.dto.AuthCallbackRequest;
import com.entrusting.backend.user.dto.LoginRequest;
import com.entrusting.backend.user.dto.RegisterRequest;
import com.entrusting.backend.user.entity.User;
import com.entrusting.backend.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequest request) {
        try {
            userService.registerUser(request);
            return ResponseEntity.ok("User registered successfully");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequest request) {
        try {
            User user = userService.loginUser(request);
            // 실제 로그인 시 JWT 토큰 등을 반환해야 하지만, 여기서는 간단하게 성공 메시지 반환
            return ResponseEntity.ok("Login successful for user: " + user.getUsername());
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
            // [보안 강화] 수탁사(8082) 서버에 직접 토큰 상태 확인
            String trusteeUrl = "http://127.0.0.1:8082/api/v1/auth/status/" + request.getTokenId();
            org.springframework.web.client.RestTemplate restTemplate = new org.springframework.web.client.RestTemplate();

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
                throw new Exception("수탁사 인증 서버(8082) 연결 실패: " + e.getMessage());
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
