package com.trustee.backend.auth.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.security.Key;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtProvider {

private Key key;

    @PostConstruct
    public void init() {
        // JWT 서명 키를 동적으로 생성 (HS256 알고리즘 사용)
        this.key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    }

    private final long expiration = 3600000; // 1 hour

    public String generateToken(UUID jti, UUID authRequestId, String name, String ci) {
        return Jwts.builder()
                .setId(jti.toString()) // jti = Authentication Provider's Session ID
                .claim("auth_request_id", authRequestId != null ? authRequestId.toString() : null) // Custom claim
                .claim("name", name)
                .claim("ci", ci) // [Compliance] Add CI to claims
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(key)
                .compact();
    }
}
