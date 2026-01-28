package com.tossbank.tmcenter.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 동의 철회 요청 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WithdrawalRequest {
    
    @NotBlank(message = "요청 ID는 필수입니다")
    private String requestId;
    
    private String reason;
}
