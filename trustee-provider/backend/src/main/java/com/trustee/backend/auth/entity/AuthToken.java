package com.trustee.backend.auth.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
public class AuthToken {

    @Id
    private UUID tokenId;

    private String clientData;
    private String name;
    private String carrier;
    private String otp;

    @Enumerated(EnumType.STRING)
    private AuthStatus status;

    private LocalDateTime createdAt;

    public AuthToken() {
    }

    public AuthToken(UUID tokenId, String clientData, String name, String carrier, String otp, AuthStatus status,
            LocalDateTime createdAt) {
        this.tokenId = tokenId;
        this.clientData = clientData;
        this.name = name;
        this.carrier = carrier;
        this.otp = otp;
        this.status = status;
        this.createdAt = createdAt;
    }

    public UUID getTokenId() {
        return tokenId;
    }

    public void setTokenId(UUID tokenId) {
        this.tokenId = tokenId;
    }

    public String getClientData() {
        return clientData;
    }

    public void setClientData(String clientData) {
        this.clientData = clientData;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCarrier() {
        return carrier;
    }

    public void setCarrier(String carrier) {
        this.carrier = carrier;
    }

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }

    public AuthStatus getStatus() {
        return status;
    }

    public void setStatus(AuthStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
