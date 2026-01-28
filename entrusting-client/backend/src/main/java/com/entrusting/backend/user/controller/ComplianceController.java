package com.entrusting.backend.user.controller;

import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/compliance")
public class ComplianceController {

    public ComplianceController() {
    }

    @PostMapping("/marketing-consent")
    public Map<String, Object> registerMarketingConsent(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String productName = request.get("productName");
        
        UUID requestId = UUID.randomUUID();
        LocalDate retentionUntil = LocalDate.now().plusMonths(3);
        
        // Simulation: In a real system, this would be saved to a database table like 'marketing_requests'.
        System.out.println("[COMPLIANCE] Marketing Consent Received: User=" + username + ", Product=" + productName);
        
        return Map.of(
            "status", "SUCCESS",
            "requestId", requestId.toString(),
            "retentionUntil", retentionUntil.toString(),
            "message", "마케팅 활용 동의가 성공적으로 기록되었습니다. (보유기한: 3개월)"
        );
    }
}
