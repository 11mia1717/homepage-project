package com.trustee.backend.auth.dto;

public class AuthInitRequest {
    private String clientData;
    private String name;

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
}
