package com.trustee.backend.auth.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtProvider {

    // Ideally, this should be in application.properties/yml
    private final Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
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
