package com.trustee.backend.auth.dto;

import com.trustee.backend.auth.entity.AuthStatus;

public class AuthVerificationResponse {
    private AuthStatus status;
    private String name;
    private String phoneNumber;

    public AuthVerificationResponse() {
    }

    public AuthVerificationResponse(AuthStatus status, String name, String phoneNumber) {
        this.status = status;
        this.name = name;
        this.phoneNumber = phoneNumber;
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

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}
