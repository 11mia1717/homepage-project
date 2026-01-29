package com.entrusting.backend.user.controller;

import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/compliance")
public class ComplianceController {

    private final com.entrusting.backend.user.repository.UserRepository userRepository;
    private final com.entrusting.backend.user.repository.AccessLogRepository accessLogRepository;

    public ComplianceController(com.entrusting.backend.user.repository.UserRepository userRepository,
                                com.entrusting.backend.user.repository.AccessLogRepository accessLogRepository) {
        this.userRepository = userRepository;
        this.accessLogRepository = accessLogRepository;
    }

    @PostMapping("/marketing-consent")
    public Map<String, Object> registerMarketingConsent(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String productName = request.get("productName");
        
        com.entrusting.backend.user.entity.User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));

        // Update Consent
        user.setMarketingAgreed(true);
        user.setMarketingSms(true); // Default to SMS for outbound
        user.setSsapProvisionAgreed(true); // 3rd party provision
        user.setMarketingPersonalAgreed(true);
        userRepository.save(user);

        // Audit Log
        accessLogRepository.save(new com.entrusting.backend.user.entity.AccessLog(
                user.getId(),
                "SELF",
                user.getUsername(),
                "MARKETING_CONSENT",
                "이벤트 참여 동의 (Product: " + productName + ")",
                "WEB"
        ));
        
        UUID requestId = UUID.randomUUID();
        LocalDate retentionUntil = LocalDate.now().plusMonths(3);
        
        return Map.of(
            "status", "SUCCESS",
            "requestId", requestId.toString(),
            "retentionUntil", retentionUntil.toString(),
            "message", "마케팅 활용 동의가 성공적으로 기록되었습니다. (보유기한: 3개월)"
        );
    }

    @GetMapping("/marketing-consented-users")
    public java.util.List<Map<String, Object>> getMarketingConsentedUsers() {
        return userRepository.findByMarketingAgreedTrue().stream()
            .map(user -> Map.<String, Object>of(
                "id", user.getId(),
                "name", com.entrusting.backend.util.EncryptionUtils.decrypt(user.getName()), // Decrypt for display
                "phone", com.entrusting.backend.util.EncryptionUtils.decrypt(user.getPhoneNumber()),
                "type", "신용카드 권유",
                "purpose", "상품 소개 및 권유",
                "retentionUntil", LocalDate.now().plusMonths(3).toString()
            ))
            .collect(java.util.stream.Collectors.toList());
    }
}
