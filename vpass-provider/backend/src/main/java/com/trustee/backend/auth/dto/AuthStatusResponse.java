package com.trustee.backend.auth.dto;

import com.trustee.backend.auth.entity.AuthStatus;
import java.util.UUID;

public class AuthStatusResponse {
    private UUID tokenId;
    private AuthStatus status;
    private String name;
    // [New] JWT Token needed for verification by Client
    private String accessToken;

    public AuthStatusResponse(UUID tokenId, AuthStatus status, String name, String accessToken) {
        this.tokenId = tokenId;
        this.status = status;
        this.name = name;
        this.accessToken = accessToken;
    }

    public UUID getTokenId() {
        return tokenId;
    }

    public void setTokenId(UUID tokenId) {
        this.tokenId = tokenId;
    }

    public AuthStatus getStatus() {
        return status;
    }

    public void setStatus(AuthStatus status) {
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
}
