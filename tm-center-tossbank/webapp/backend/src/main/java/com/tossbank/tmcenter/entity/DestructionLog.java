package com.tossbank.tmcenter.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "destruction_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DestructionLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 50)
    private String targetTable;
    
    @Column(nullable = false)
    private Long targetId;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DestructionType destructionType;
    
    @Column(nullable = false, length = 500)
    private String destructionReason;
    
    @Column(nullable = false, length = 500)
    private String destroyedFields;
    
    @Column(length = 100)
    private String executedBy;
    
    @Builder.Default
    private LocalDateTime executedAt = LocalDateTime.now();
    
    public enum DestructionType {
        AUTO,       // 자동 파기 (배치)
        MANUAL,     // 수동 파기
        WITHDRAWAL  // 동의 철회로 인한 파기
    }
    
    // 자동 파기 로그 생성
    public static DestructionLog createAutoDestructionLog(
            String table, Long targetId, String destroyedFields) {
        return DestructionLog.builder()
                .targetTable(table)
                .targetId(targetId)
                .destructionType(DestructionType.AUTO)
                .destructionReason("보유기간 만료로 인한 자동 파기")
                .destroyedFields(destroyedFields)
                .executedBy("SYSTEM")
                .build();
    }
    
    // 동의 철회 파기 로그 생성
    public static DestructionLog createWithdrawalDestructionLog(
            String table, Long targetId, String destroyedFields, String executedBy) {
        return DestructionLog.builder()
                .targetTable(table)
                .targetId(targetId)
                .destructionType(DestructionType.WITHDRAWAL)
                .destructionReason("고객 동의 철회로 인한 파기")
                .destroyedFields(destroyedFields)
                .executedBy(executedBy)
                .build();
    }
}
