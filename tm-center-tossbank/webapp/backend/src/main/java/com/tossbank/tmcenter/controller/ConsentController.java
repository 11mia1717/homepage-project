package com.tossbank.tmcenter.controller;

import com.tossbank.tmcenter.dto.*;
import com.tossbank.tmcenter.service.ConsentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 마케팅 동의 API 컨트롤러
 * 고객의 마케팅 동의 처리 및 동의 철회 관련 기능
 */
@RestController
@RequestMapping("/consent")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "마케팅 동의 API", description = "마케팅 동의/철회 관련 API")
public class ConsentController {
    
    private final ConsentService consentService;
    
    /**
     * 마케팅 동의 처리
     * POST /api/consent
     */
    @PostMapping
    @Operation(summary = "마케팅 동의", description = "고객의 마케팅 목적 개인정보 제3자 제공 동의 처리")
    public ResponseEntity<ApiResponse<ConsentResponse>> processConsent(
            @Valid @RequestBody ConsentRequest request) {
        
        log.info("마케팅 동의 요청: customerName={}, productName={}", 
                request.getCustomerName(), request.getProductName());
        
        try {
            ConsentResponse response = consentService.processConsent(request);
            return ResponseEntity.ok(ApiResponse.success(response, "마케팅 동의가 완료되었습니다."));
        } catch (IllegalArgumentException e) {
            log.warn("마케팅 동의 실패: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("CONSENT_ERROR", e.getMessage()));
        }
    }
    
    /**
     * 동의 철회 처리
     * POST /api/consent/withdraw
     */
    @PostMapping("/withdraw")
    @Operation(summary = "동의 철회", description = "마케팅 동의 철회 처리 (개인정보 즉시 파기)")
    public ResponseEntity<ApiResponse<Void>> withdrawConsent(
            @Valid @RequestBody WithdrawalRequest request) {
        
        log.info("동의 철회 요청: requestId={}", request.getRequestId());
        
        try {
            consentService.withdrawConsent(request.getRequestId(), request.getReason());
            return ResponseEntity.ok(ApiResponse.success("동의가 철회되었습니다. 개인정보가 즉시 파기됩니다."));
        } catch (IllegalArgumentException | IllegalStateException e) {
            log.warn("동의 철회 실패: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("WITHDRAWAL_ERROR", e.getMessage()));
        }
    }
    
    /**
     * 마케팅 동의 내역 조회
     * GET /api/consent/history?phone={phone}
     */
    @GetMapping("/history")
    @Operation(summary = "동의 내역 조회", description = "고객의 마케팅 동의 내역 조회")
    public ResponseEntity<ApiResponse<List<ConsentResponse>>> getConsentHistory(
            @RequestParam String phone) {
        
        log.info("동의 내역 조회 요청: phone={}", phone);
        
        List<ConsentResponse> history = consentService.getConsentHistory(phone);
        return ResponseEntity.ok(ApiResponse.success(history));
    }
    
    /**
     * 개인정보 제3자 제공 동의서 내용 조회
     * GET /api/consent/agreement/{productName}
     */
    @GetMapping("/agreement/{productName}")
    @Operation(summary = "동의서 내용 조회", description = "제3자 제공 동의서 내용 조회")
    public ResponseEntity<ApiResponse<AgreementContent>> getAgreementContent(
            @PathVariable String productName) {
        
        AgreementContent content = AgreementContent.builder()
                .productName(productName)
                .recipient("○○TM센터")
                .purpose(productName + " 상담")
                .items("이름, 연락처")
                .retentionPeriod("상담 완료 후 3개월")
                .refusalInfo("동의를 거부할 수 있으며, 거부 시 상담 서비스를 받으실 수 없습니다.")
                .build();
        
        return ResponseEntity.ok(ApiResponse.success(content));
    }
    
    /**
     * 동의서 내용 DTO
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class AgreementContent {
        private String productName;
        private String recipient;
        private String purpose;
        private String items;
        private String retentionPeriod;
        private String refusalInfo;
    }
}
