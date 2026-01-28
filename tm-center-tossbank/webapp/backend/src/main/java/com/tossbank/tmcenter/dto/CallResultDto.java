package com.tossbank.tmcenter.dto;

import com.tossbank.tmcenter.entity.CallResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 상담 결과 응답 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CallResultDto {
    
    private Long id;
    private Long tmTargetId;
    private String customerName;
    
    // 상담사 정보
    private Long agentId;
    private String agentName;
    
    // 통화 정보
    private LocalDateTime callStartedAt;
    private LocalDateTime callEndedAt;
    private Integer callDuration;
    private String formattedDuration;
    
    // 녹취 정보
    private boolean recordingAgreed;
    private boolean recorded;
    private String recordingFileUrl;
    
    // 상담 결과
    private String resultCode;
    private String resultCodeLabel;
    private String resultDetail;
    
    // 상품 정보
    private String consultedProduct;
    private String productResult;
    private String productResultLabel;
    
    // 재통화 정보
    private LocalDateTime callbackScheduledAt;
    private boolean retryAgreed;
    
    // 메모
    private String memo;
    
    private LocalDateTime createdAt;
    
    public static CallResultDto from(CallResult result) {
        return CallResultDto.builder()
                .id(result.getId())
                .tmTargetId(result.getTmTarget().getId())
                .customerName(result.getTmTarget().getCustomerName())
                .agentId(result.getAgent().getId())
                .agentName(result.getAgent().getName())
                .callStartedAt(result.getCallStartedAt())
                .callEndedAt(result.getCallEndedAt())
                .callDuration(result.getCallDuration())
                .formattedDuration(formatDuration(result.getCallDuration()))
                .recordingAgreed("Y".equals(result.getRecordingAgreedYn()))
                .recorded("Y".equals(result.getRecordedYn()))
                .recordingFileUrl(result.getRecordingFileUrl())
                .resultCode(result.getResultCode().name())
                .resultCodeLabel(getResultCodeLabel(result.getResultCode()))
                .resultDetail(result.getResultDetail())
                .consultedProduct(result.getConsultedProduct())
                .productResult(result.getProductResult() != null ? 
                        result.getProductResult().name() : null)
                .productResultLabel(result.getProductResult() != null ? 
                        getProductResultLabel(result.getProductResult()) : null)
                .callbackScheduledAt(result.getCallbackScheduledAt())
                .retryAgreed("Y".equals(result.getRetryAgreedYn()))
                .memo(result.getMemo())
                .createdAt(result.getCreatedAt())
                .build();
    }
    
    private static String formatDuration(Integer seconds) {
        if (seconds == null || seconds == 0) return "0초";
        int min = seconds / 60;
        int sec = seconds % 60;
        if (min > 0) {
            return min + "분 " + sec + "초";
        }
        return sec + "초";
    }
    
    private static String getResultCodeLabel(CallResult.ResultCode code) {
        return switch (code) {
            case SUCCESS -> "상담 성공";
            case NO_ANSWER -> "부재중";
            case BUSY -> "통화중";
            case CALLBACK -> "재통화 요청";
            case REFUSED -> "상담 거부";
            case WRONG_NUMBER -> "잘못된 번호";
            case OTHER -> "기타";
        };
    }
    
    private static String getProductResultLabel(CallResult.ProductResult result) {
        return switch (result) {
            case AGREED -> "가입 동의";
            case PENDING -> "검토중";
            case REFUSED -> "거절";
        };
    }
}
