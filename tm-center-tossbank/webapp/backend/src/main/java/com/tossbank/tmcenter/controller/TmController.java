package com.tossbank.tmcenter.controller;

import com.tossbank.tmcenter.dto.*;
import com.tossbank.tmcenter.service.TmService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * TM 상담 API 컨트롤러
 * 상담사의 고객 조회, 상담 결과 입력 등 TM 업무 관련 기능
 */
@RestController
@RequestMapping("/tm")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "TM 상담 API", description = "TM 상담사 업무 관련 API")
public class TmController {
    
    private final TmService tmService;
    
    /**
     * 다음 고객 불러오기 (상담 대상 배정)
     * POST /api/tm/next
     */
    @PostMapping("/next")
    @Operation(summary = "다음 고객 불러오기", description = "대기중인 고객을 상담사에게 배정")
    public ResponseEntity<ApiResponse<TmTargetDto>> getNextTarget(
            @RequestHeader("X-Agent-Id") Long agentId,
            HttpServletRequest request) {
        
        String ipAddress = getClientIp(request);
        String userAgent = request.getHeader("User-Agent");
        
        log.info("다음 고객 요청: agentId={}", agentId);
        
        TmTargetDto target = tmService.getNextTarget(agentId, ipAddress, userAgent);
        
        if (target == null) {
            return ResponseEntity.ok(ApiResponse.success(null, "대기중인 고객이 없습니다."));
        }
        
        return ResponseEntity.ok(ApiResponse.success(target));
    }
    
    /**
     * 배정된 고객 목록 조회
     * GET /api/tm/targets
     */
    @GetMapping("/targets")
    @Operation(summary = "배정 고객 목록", description = "상담사에게 배정된 고객 목록 조회")
    public ResponseEntity<ApiResponse<Page<TmTargetDto>>> getAssignedTargets(
            @RequestHeader("X-Agent-Id") Long agentId,
            @PageableDefault(size = 10) Pageable pageable,
            HttpServletRequest request) {
        
        String ipAddress = getClientIp(request);
        String userAgent = request.getHeader("User-Agent");
        
        Page<TmTargetDto> targets = tmService.getAssignedTargets(agentId, pageable, ipAddress, userAgent);
        return ResponseEntity.ok(ApiResponse.success(targets));
    }
    
    /**
     * 고객 상세 정보 조회
     * GET /api/tm/targets/{targetId}
     */
    @GetMapping("/targets/{targetId}")
    @Operation(summary = "고객 상세 조회", description = "고객 상세 정보 조회 (접근 로그 기록)")
    public ResponseEntity<ApiResponse<TmTargetDto>> getTargetDetail(
            @PathVariable Long targetId,
            @RequestHeader("X-Agent-Id") Long agentId,
            HttpServletRequest request) {
        
        String ipAddress = getClientIp(request);
        String userAgent = request.getHeader("User-Agent");
        
        log.info("고객 상세 조회: targetId={}, agentId={}", targetId, agentId);
        
        try {
            TmTargetDto target = tmService.getTargetDetail(targetId, agentId, ipAddress, userAgent);
            return ResponseEntity.ok(ApiResponse.success(target));
        } catch (IllegalStateException e) {
            // 동의 철회 또는 파기된 경우
            log.warn("고객 조회 불가: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("TARGET_UNAVAILABLE", e.getMessage()));
        }
    }
    
    /**
     * 상담 시작 (통화 시작)
     * POST /api/tm/targets/{targetId}/start
     */
    @PostMapping("/targets/{targetId}/start")
    @Operation(summary = "상담 시작", description = "고객과 통화 시작")
    public ResponseEntity<ApiResponse<Void>> startCall(
            @PathVariable Long targetId,
            @RequestHeader("X-Agent-Id") Long agentId) {
        
        log.info("상담 시작: targetId={}, agentId={}", targetId, agentId);
        
        tmService.startCall(targetId, agentId);
        return ResponseEntity.ok(ApiResponse.success("상담이 시작되었습니다."));
    }
    
    /**
     * 상담 결과 저장
     * POST /api/tm/results
     */
    @PostMapping("/results")
    @Operation(summary = "상담 결과 저장", description = "상담 결과 및 녹취 동의 정보 저장")
    public ResponseEntity<ApiResponse<CallResultDto>> saveCallResult(
            @Valid @RequestBody CallResultRequest request,
            @RequestHeader("X-Agent-Id") Long agentId,
            HttpServletRequest httpRequest) {
        
        String ipAddress = getClientIp(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");
        
        log.info("상담 결과 저장: targetId={}, resultCode={}", 
                request.getTmTargetId(), request.getResultCode());
        
        try {
            CallResultDto result = tmService.saveCallResult(request, agentId, ipAddress, userAgent);
            return ResponseEntity.ok(ApiResponse.success(result, "상담 결과가 저장되었습니다."));
        } catch (IllegalArgumentException e) {
            // 목적 외 사용 시도 등
            log.warn("상담 결과 저장 실패: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("INVALID_CONSULTATION", e.getMessage()));
        }
    }
    
    /**
     * 상담 이력 조회
     * GET /api/tm/targets/{targetId}/history
     */
    @GetMapping("/targets/{targetId}/history")
    @Operation(summary = "상담 이력 조회", description = "특정 고객의 상담 이력 조회")
    public ResponseEntity<ApiResponse<List<CallResultDto>>> getCallHistory(
            @PathVariable Long targetId) {
        
        List<CallResultDto> history = tmService.getCallHistory(targetId);
        return ResponseEntity.ok(ApiResponse.success(history));
    }
    
    /**
     * 재통화 대상 목록 조회
     * GET /api/tm/callbacks
     */
    @GetMapping("/callbacks")
    @Operation(summary = "재통화 대상 조회", description = "재통화 예정인 고객 목록 조회")
    public ResponseEntity<ApiResponse<List<TmTargetDto>>> getCallbackTargets() {
        List<TmTargetDto> callbacks = tmService.getCallbackTargets();
        return ResponseEntity.ok(ApiResponse.success(callbacks));
    }
    
    /**
     * 필수 상담 스크립트 조회
     * GET /api/tm/script
     */
    @GetMapping("/script")
    @Operation(summary = "상담 스크립트 조회", description = "필수 상담 스크립트 (녹취 안내 등)")
    public ResponseEntity<ApiResponse<ConsultationScript>> getScript() {
        ConsultationScript script = ConsultationScript.builder()
                .greeting("안녕하세요 고객님, ○○은행입니다.")
                .recordingNotice("본 통화는 서비스 품질 향상을 위해 녹음됩니다. 동의하십니까?")
                .recordingRequired(true)
                .retryNotice("이전에 동의하신 마케팅 상담입니다. 계속 진행하시겠습니까?")
                .closingMessage("이용해 주셔서 감사합니다. 좋은 하루 되세요.")
                .build();
        
        return ResponseEntity.ok(ApiResponse.success(script));
    }
    
    /**
     * 상담 스크립트 DTO
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ConsultationScript {
        private String greeting;
        private String recordingNotice;
        private boolean recordingRequired;
        private String retryNotice;
        private String closingMessage;
    }
    
    /**
     * 클라이언트 IP 주소 추출
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
