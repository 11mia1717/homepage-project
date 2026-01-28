package com.tossbank.tmcenter.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 마케팅 동의 요청 DTO
 * 고객이 마케팅 동의를 할 때 사용
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsentRequest {
    
    // @NotBlank(message = "고객명은 필수입니다") -> 본인인증으로 대체
    private String customerName;
    
    // @NotBlank(message = "연락처는 필수입니다")
    // @Pattern(regexp = "^01[0-9]-?[0-9]{4}-?[0-9]{4}$", message = "올바른 연락처 형식이 아닙니다")
    private String customerPhone;

    // 본인인증 토큰 (필수)
    private java.util.UUID tokenId;
    
    @NotBlank(message = "상품명은 필수입니다")
    private String productName;
    
    // 동의 항목들
    @Builder.Default
    private Boolean agreeThirdPartyProvision = false;  // 제3자 제공 동의
    
    @Builder.Default
    private Boolean agreeMarketing = false;  // 마케팅 활용 동의
}
