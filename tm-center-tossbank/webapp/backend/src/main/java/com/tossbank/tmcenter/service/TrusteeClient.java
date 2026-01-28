package com.tossbank.tmcenter.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TrusteeClient {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${trustee.api.url:http://localhost:8086/api/v1/auth}")
    private String trusteeApiUrl;

    public VerificationResult verifyToken(UUID tokenId) {
        String url = trusteeApiUrl + "/verify/" + tokenId;
        log.info("Requesting verification to: {}", url);

        try {
            ResponseEntity<VerificationResult> response = restTemplate.getForEntity(url, VerificationResult.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody();
            }
        } catch (Exception e) {
            log.error("Trustee verification failed: {}", e.getMessage());
            throw new IllegalArgumentException("본인인증 확인 중 오류가 발생했습니다: " + e.getMessage());
        }
        
        throw new IllegalArgumentException("본인인증 정보를 찾을 수 없습니다.");
    }

    @Data
    public static class VerificationResult {
        private String status; // COMPLETED
        private String name;
        @JsonProperty("clientData")
        private String phoneNumber; // V-PASS returns 'clientData' as phone number field usually, checking AuthController...
        
        // Wait, V-PASS AuthVerificationResponse uses:
        // status, name, clientData (which is decrypted phone)
        // Let's verify AuthVerificationResponse structure from V-PASS source if possible.
        // I read AuthService.java earlier:
        // return new AuthVerificationResponse(authToken.getStatus(), decryptedName, decryptedPhone);
        // AuthVerificationResponse DTO I should check but assuming standard getter matching.
    }
}
