package com.tossbank.tmcenter.dto;

import com.tossbank.tmcenter.entity.TmTarget;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * TM 타겟 정보 DTO (상담사 화면용)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TmTargetDto {
    
    private Long id;
    private String externalRef;
    
    // 고객 정보 (마스킹 여부에 따라 다름)
    private String customerName;
    private String phone;
    
    // 상담 정보
    private String productName;
    private String consentPurpose;
    
    // 보유 기한
    private LocalDate retentionUntil;
    
    // 상태
    private String status;
    private Integer priority;
    
    // 배정 정보
    private String assignedAgentName;
    private LocalDateTime assignedAt;
    
    // 파기 여부
    private boolean destroyed;
    
    // 경고 메시지 (목적 외 사용 제한 안내)
    private String warningMessage;
    
    public static TmTargetDto from(TmTarget target) {
        return TmTargetDto.builder()
                .id(target.getId())
                .externalRef(target.getExternalRef())
                .customerName(target.getCustomerName())
                .phone(target.getPhone())
                .productName(target.getProductName())
                .consentPurpose(target.getConsentPurpose())
                .retentionUntil(target.getRetentionUntil())
                .status(target.getStatus().name())
                .priority(target.getPriority())
                .assignedAgentName(target.getAssignedAgent() != null ? 
                        target.getAssignedAgent().getName() : null)
                .assignedAt(target.getAssignedAt())
                .destroyed("Y".equals(target.getDestroyedYn()))
                .warningMessage(generateWarningMessage(target))
                .build();
    }
    
    private static String generateWarningMessage(TmTarget target) {
        return String.format(
            "⚠️ 본 정보는 \"%s\" 목적으로만 사용 가능합니다. 보유 기한: %s까지",
            target.getConsentPurpose(),
            target.getRetentionUntil()
        );
    }
}
