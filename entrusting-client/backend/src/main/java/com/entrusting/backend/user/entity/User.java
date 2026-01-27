package com.entrusting.backend.user.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "site_users") // 'user', 'users'는 H2/SQL 예약어와 충돌할 가능성이 높음
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name; // 사용자 이름
    private String username;
    private String password;
    
    // [Compliance] Encrypted PII
    private String phoneNumber; 
    
    // [Compliance] Connecting Information (Unique per person)
    @Column(unique = true)
    private String ci;
    
    // [Compliance] Duplication Information (Unique per site)
    private String di;
    
    private boolean isVerified;

    public User() {
    }

    public User(String name, String username, String password, String phoneNumber, String ci, String di, boolean isVerified) {
        this.name = name;
        this.username = username;
        this.password = password;
        this.phoneNumber = phoneNumber;
        this.ci = ci;
        this.di = di;
        this.isVerified = isVerified;
    }

    public String getCi() {
        return ci;
    }

    public void setCi(String ci) {
        this.ci = ci;
    }

    public String getDi() {
        return di;
    }

    public void setDi(String di) {
        this.di = di;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public boolean isVerified() {
        return isVerified;
    }

    public void setVerified(boolean verified) {
        isVerified = verified;
    }
}
