package com.entrusting.backend.s2s.controller;

import com.entrusting.backend.user.entity.User;
import com.entrusting.backend.user.entity.AccessLog;
import com.entrusting.backend.user.repository.UserRepository;
import com.entrusting.backend.user.repository.AccessLogRepository;
import com.entrusting.backend.util.EncryptionUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * S2S (Server-to-Server) API for Call Center integration.
 * 
 * [COMPLIANCE] This API exposes customer data to the trustee (call center).
 * - Only masked data is returned
 * - Requires valid S2S service token
 * - All access is logged for audit (DB)
 */
@RestController
@RequestMapping("/api/s2s")
public class S2SCustomerController {

    private final UserRepository userRepository;
    private final AccessLogRepository accessLogRepository;

    // S2S token validation (simplified for demo)
    private static final String VALID_S2S_TOKEN = "callcenter-service-token-2026";

    public S2SCustomerController(UserRepository userRepository, AccessLogRepository accessLogRepository) {
        this.userRepository = userRepository;
        this.accessLogRepository = accessLogRepository;
    }

    /**
     * Search customer by phone number.
     * Returns masked data only for compliance.
     */
    @PostMapping("/customer/search")
    public ResponseEntity<?> searchCustomer(
            @RequestHeader(value = "X-Service-Token", required = false) String serviceToken,
            @RequestBody Map<String, String> request) {

        // [COMPLIANCE] S2S Token validation
        if (serviceToken == null || !VALID_S2S_TOKEN.equals(serviceToken)) {
            return ResponseEntity.status(403).body(Map.of(
                "error", "INVALID_SERVICE_TOKEN",
                "message", "유효하지 않은 서비스 토큰입니다."
            ));
        }

        String phone = request.get("phone");
        if (phone == null || phone.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "INVALID_PHONE",
                "message", "전화번호를 입력해 주세요."
            ));
        }

        // Normalize phone number
        String normalizedPhone = phone.replaceAll("\\D", "");
        
        // Encrypt phone to search in DB (stored encrypted)
        String encryptedPhone = EncryptionUtils.encrypt(normalizedPhone);

        Optional<User> userOpt = userRepository.findByPhoneNumber(encryptedPhone);

        if (userOpt.isEmpty()) {
            return ResponseEntity.ok(Map.of(
                "candidateCount", 0,
                "candidates", Collections.emptyList()
            ));
        }

        User user = userOpt.get();

        // [COMPLIANCE] Decrypt and mask data
        String decryptedName = EncryptionUtils.decrypt(user.getName());
        String decryptedPhone = EncryptionUtils.decrypt(user.getPhoneNumber());

        String maskedName = maskName(decryptedName);
        String maskedPhone = maskPhone(decryptedPhone);

        // [COMPLIANCE] Log access to DB for audit
        accessLogRepository.save(new AccessLog(
            user.getId(),
            "CALLCENTER",
            "callcenter-was",
            "SEARCH",
            "고객 조회 - 전화번호: " + maskedPhone,
            "S2S-API"
        ));

        Map<String, Object> candidate = new HashMap<>();
        candidate.put("customerRef", "CU" + user.getId()); // Reference ID
        candidate.put("maskedName", maskedName);
        candidate.put("maskedPhone", maskedPhone);
        candidate.put("verified", user.isVerified());

        return ResponseEntity.ok(Map.of(
            "candidateCount", 1,
            "candidates", List.of(candidate)
        ));
    }

    /**
     * Mask name: 홍길동 → 홍*동
     */
    private String maskName(String name) {
        if (name == null || name.length() < 2) return "***";
        if (name.length() == 2) {
            return name.charAt(0) + "*";
        }
        return name.charAt(0) + "*".repeat(name.length() - 2) + name.charAt(name.length() - 1);
    }

    /**
     * Mask phone: 01012345678 → 010-****-5678
     */
    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 10) return "***-****-****";
        String normalized = phone.replaceAll("\\D", "");
        if (normalized.length() == 11) {
            return normalized.substring(0, 3) + "-****-" + normalized.substring(7);
        } else if (normalized.length() == 10) {
            return normalized.substring(0, 3) + "-***-" + normalized.substring(6);
        }
        return "***-****-****";
    }
    /**
     * Get marketing consented users for Outbound (TM).
     * [COMPLIANCE] S2S only.
     */
    @PostMapping("/marketing-consented")
    public ResponseEntity<?> getMarketingConsentedUsers(
            @RequestHeader(value = "X-Service-Token", required = false) String serviceToken) {

        if (serviceToken == null || !VALID_S2S_TOKEN.equals(serviceToken)) {
            return ResponseEntity.status(403).body(Map.of("error", "INVALID_TOKEN"));
        }

        List<Map<String, Object>> candidates = userRepository.findByMarketingAgreedTrue().stream()
                .map(user -> {
                    String decryptedName = EncryptionUtils.decrypt(user.getName());
                    String decryptedPhone = EncryptionUtils.decrypt(user.getPhoneNumber());

                    Map<String, Object> map = new HashMap<>();
                    map.put("customerRef", "CU" + user.getId());
                    map.put("name", maskName(decryptedName));
                    map.put("phone", maskPhone(decryptedPhone));
                    map.put("marketingAgreedAt", user.getTermsAgreedAt()); // roughly
                    return map;
                })
                .collect(Collectors.toList());

        accessLogRepository.save(new AccessLog(
                0L, // System
                "CALLCENTER",
                "callcenter-was",
                "BATCH_RETRIEVE",
                "마케팅 동의 고객 목록 조회 (Outbound)",
                "S2S-API"
        ));

        return ResponseEntity.ok(Map.of(
            "count", candidates.size(),
            "candidates", candidates
        ));
    }
}
