package com.entrusting.backend.user.controller;

import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/compliance")
public class ComplianceController {


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

    private final com.entrusting.backend.user.repository.UserRepository userRepository;

    @org.springframework.beans.factory.annotation.Autowired
    public ComplianceController(com.entrusting.backend.user.repository.UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/marketing-consented-users")
    public java.util.List<Map<String, Object>> getMarketingConsentedUsers() {
        return userRepository.findByMarketingAgreedTrue().stream()
            .map(user -> Map.<String, Object>of(
                "id", user.getId(),
                "name", user.getName(),
                "phone", user.getPhoneNumber(),
                "type", "신용카드 권유",
                "purpose", "상품 소개 및 권유",
                "retentionUntil", LocalDate.now().plusMonths(3).toString()
            ))
            .collect(java.util.stream.Collectors.toList());
    }

}
