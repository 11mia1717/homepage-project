package com.trustee.backend.auth.controller;

import com.trustee.backend.auth.dto.AuthConfirmRequest;
import com.trustee.backend.auth.dto.AuthInitRequest;
import com.trustee.backend.auth.dto.AuthInitResponse;
import com.trustee.backend.auth.dto.AuthOtpRequest;
import com.trustee.backend.auth.dto.AuthStatusResponse;
import com.trustee.backend.auth.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/init")
    public ResponseEntity<AuthInitResponse> initAuth(@RequestBody AuthInitRequest request) {
        AuthInitResponse response = authService.initAuth(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/status/{tokenId}")
    public ResponseEntity<AuthStatusResponse> getAuthStatus(@PathVariable UUID tokenId) {
        AuthStatusResponse response = authService.getAuthStatus(tokenId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/request-otp")
    public ResponseEntity<?> requestOtp(@RequestBody AuthOtpRequest request) {
        try {
            AuthInitResponse response = authService.requestOtp(request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PatchMapping("/confirm")
    public ResponseEntity<Void> confirmAuth(@RequestBody AuthConfirmRequest request) {
        authService.confirmAuth(request);
        return ResponseEntity.noContent().build();
    }
}
