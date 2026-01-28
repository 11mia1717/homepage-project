package com.tossbank.tmcenter.dto;

import com.tossbank.tmcenter.entity.MarketingRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 마케팅 동의 응답 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsentResponse {
    
    private String requestId;
    private String productName;
    private String consentPurpose;
    private String consentRecipient;
    private LocalDateTime consentedAt;
    private LocalDate retentionUntil;
    private String status;
    private String message;
    
    public static ConsentResponse from(MarketingRequest request) {
        return ConsentResponse.builder()
                .requestId(request.getRequestId())
                .productName(request.getProductName())
                .consentPurpose(request.getConsentPurpose())
                .consentRecipient(request.getConsentRecipient())
                .consentedAt(request.getConsentedAt())
                .retentionUntil(request.getRetentionUntil())
                .status(request.getStatus().name())
                .message("동의가 완료되었습니다. 개인정보는 " + request.getRetentionUntil() + "까지 보유 후 자동 파기됩니다.")
                .build();
    }
}
