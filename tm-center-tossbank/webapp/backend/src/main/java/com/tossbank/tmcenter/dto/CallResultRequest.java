package com.tossbank.tmcenter.dto;

import com.tossbank.tmcenter.entity.CallResult;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 상담 결과 저장 요청 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CallResultRequest {
    
    @NotNull(message = "TM 타겟 ID는 필수입니다")
    private Long tmTargetId;
    
    // 녹취 동의 여부
    @Builder.Default
    private Boolean recordingAgreed = false;
    
    // 상담 결과
    @NotNull(message = "상담 결과 코드는 필수입니다")
    private CallResult.ResultCode resultCode;
    
    private String resultDetail;
    
    // 상담한 상품 (목적 외 사용 제한 검증용)
    @NotBlank(message = "상담 상품명은 필수입니다")
    private String consultedProduct;
    
    // 상품 상담 결과
    private CallResult.ProductResult productResult;
    
    // 재통화 예정 일시 (결과가 CALLBACK인 경우)
    private LocalDateTime callbackScheduledAt;
    
    // 재통화 시 재동의 여부
    @Builder.Default
    private Boolean retryAgreed = false;
    
    // 메모
    private String memo;
}
