package com.entrusting.backend.user.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import java.util.Map;

@Service
public class S2SAuthService {

    private final RestTemplate restTemplate;

    @Value("${trustee.api.base-url}")
    private String trusteeBaseUrl;

    public S2SAuthService() {
        this.restTemplate = new RestTemplate();
    }

    /**
     * 수탁사 서버에 직접 토큰 유효성을 확인하고 인증된 정보를 가져옴
     */
    public Map<String, Object> verifyTokenWithTrustee(String tokenId) {
        String url = trusteeBaseUrl + "/api/v1/auth/verify/" + tokenId;
        try {
            @SuppressWarnings("unchecked")
            ResponseEntity<Map<String, Object>> response = restTemplate.getForEntity(url,
                    (Class<Map<String, Object>>) (Class<?>) Map.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                return response.getBody();
            }
        } catch (Exception e) {
            System.err.println("[ENTRUSTING-S2S] Failed to verify token: " + e.getMessage());
        }
        return null;
    }
}
