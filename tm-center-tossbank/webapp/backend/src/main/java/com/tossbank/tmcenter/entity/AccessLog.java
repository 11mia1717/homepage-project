package com.tossbank.tmcenter.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "access_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccessLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Long agentId;
    
    @Column(nullable = false, length = 100)
    private String agentName;
    
    @Column(nullable = false)
    private Long targetId;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TargetType targetType;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccessType accessType;
    
    @Column(nullable = false, length = 200)
    private String accessPurpose;
    
    @Column(nullable = false, length = 50)
    private String ipAddress;
    
    @Column(length = 500)
    private String userAgent;
    
    @Builder.Default
    private LocalDateTime accessedAt = LocalDateTime.now();
    
    public enum TargetType {
        MARKETING_REQUEST,
        TM_TARGET,
        CALL_RESULT
    }
    
    public enum AccessType {
        VIEW,
        CREATE,
        UPDATE,
        DELETE,
        EXPORT
    }
    
    // 정적 팩토리 메서드
    public static AccessLog createViewLog(Long agentId, String agentName, 
                                          Long targetId, TargetType targetType,
                                          String purpose, String ipAddress, String userAgent) {
        return AccessLog.builder()
                .agentId(agentId)
                .agentName(agentName)
                .targetId(targetId)
                .targetType(targetType)
                .accessType(AccessType.VIEW)
                .accessPurpose(purpose)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .build();
    }
}
