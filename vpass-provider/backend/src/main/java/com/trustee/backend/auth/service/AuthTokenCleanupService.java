package com.trustee.backend.auth.service;

import com.trustee.backend.auth.repository.AuthTokenRepository;
import com.trustee.backend.auth.entity.AuthToken;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AuthTokenCleanupService {

    private final AuthTokenRepository authTokenRepository;

    public AuthTokenCleanupService(AuthTokenRepository authTokenRepository) {
        this.authTokenRepository = authTokenRepository;
    }

    /**
     * [Compliance] Data TTL (Time-To-Live)
     * Every 1 minute, delete tokens older than 3 minutes.
     * This ensures PII is not kept longer than necessary.
     */
    @Scheduled(fixedRate = 60000) // Run every 1 minute
    @Transactional
    public void cleanupExpiredTokens() {
        LocalDateTime expirationThreshold = LocalDateTime.now().minusMinutes(3); // 3 minutes TTL
        
        // Note: In real production, use a custom query like deleteByCreatedAtBefore(expirationThreshold)
        // Here we iterate for demonstration/logging purposes.
        List<AuthToken> expiredTokens = authTokenRepository.findAll().stream()
                .filter(t -> t.getCreatedAt().isBefore(expirationThreshold))
                .toList();

        if (!expiredTokens.isEmpty()) {
            System.out.println("[TRUSTEE-TTL] Deleting " + expiredTokens.size() + " expired auth sessions.");
            authTokenRepository.deleteAll(expiredTokens);
        }
    }
}
