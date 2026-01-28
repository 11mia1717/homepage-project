package com.tossbank.tmcenter.controller;

import com.tossbank.tmcenter.dto.ApiResponse;
import com.tossbank.tmcenter.dto.DashboardStats;
import com.tossbank.tmcenter.entity.AccessLog;
import com.tossbank.tmcenter.scheduler.DataDestructionScheduler;
import com.tossbank.tmcenter.service.AccessLogService;
import com.tossbank.tmcenter.service.StatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 관리자 API 컨트롤러
 * 통계, 접근 로그, 파기 관리 등 관리 기능
 */
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "관리자 API", description = "관리자 기능 관련 API")
public class AdminController {
    
    private final StatisticsService statisticsService;
    private final AccessLogService accessLogService;
    private final DataDestructionScheduler destructionScheduler;
    
    /**
     * 대시보드 통계 조회
     * GET /api/admin/dashboard
     */
    @GetMapping("/dashboard")
    @Operation(summary = "대시보드 통계", description = "전체 현황 및 통계 데이터 조회")
    public ResponseEntity<ApiResponse<DashboardStats>> getDashboardStats() {
        log.info("대시보드 통계 조회");
        
        DashboardStats stats = statisticsService.getDashboardStats();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }
    
    /**
     * 접근 로그 조회 (기간별)
     * GET /api/admin/access-logs
     */
    @GetMapping("/access-logs")
    @Operation(summary = "접근 로그 조회", description = "개인정보 접근 로그 조회")
    public ResponseEntity<ApiResponse<Page<AccessLog>>> getAccessLogs(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            @PageableDefault(size = 20) Pageable pageable) {
        
        log.info("접근 로그 조회: {} ~ {}", start, end);
        
        Page<AccessLog> logs = accessLogService.getAccessLogs(start, end, pageable);
        return ResponseEntity.ok(ApiResponse.success(logs));
    }
    
    /**
     * 특정 사용자의 접근 로그 조회
     * GET /api/admin/access-logs/agent/{agentId}
     */
    @GetMapping("/access-logs/agent/{agentId}")
    @Operation(summary = "사용자별 접근 로그", description = "특정 상담사의 접근 로그 조회")
    public ResponseEntity<ApiResponse<Page<AccessLog>>> getAccessLogsByAgent(
            @PathVariable Long agentId,
            @PageableDefault(size = 20) Pageable pageable) {
        
        log.info("사용자별 접근 로그 조회: agentId={}", agentId);
        
        Page<AccessLog> logs = accessLogService.getAccessLogsByAgent(agentId, pageable);
        return ResponseEntity.ok(ApiResponse.success(logs));
    }
    
    /**
     * 접근 유형별 통계
     * GET /api/admin/access-stats/type
     */
    @GetMapping("/access-stats/type")
    @Operation(summary = "접근 유형별 통계", description = "접근 유형(조회/수정/삭제 등)별 통계")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getAccessTypeStats(
            @RequestParam(defaultValue = "7") int days) {
        
        Map<String, Long> stats = accessLogService.getAccessTypeStats(days);
        return ResponseEntity.ok(ApiResponse.success(stats));
    }
    
    /**
     * 사용자별 접근 통계
     * GET /api/admin/access-stats/agent
     */
    @GetMapping("/access-stats/agent")
    @Operation(summary = "사용자별 접근 통계", description = "상담사별 개인정보 접근 횟수 통계")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getAgentAccessStats(
            @RequestParam(defaultValue = "7") int days) {
        
        List<Map<String, Object>> stats = accessLogService.getAgentAccessStats(days);
        return ResponseEntity.ok(ApiResponse.success(stats));
    }
    
    /**
     * 수동 파기 실행
     * POST /api/admin/destroy
     */
    @PostMapping("/destroy")
    @Operation(summary = "수동 파기 실행", description = "보유기간 만료된 개인정보 수동 파기")
    public ResponseEntity<ApiResponse<DestroyResult>> executeManualDestroy() {
        log.info("수동 파기 실행 요청");
        
        int destroyedCount = destructionScheduler.manualDestroy();
        
        DestroyResult result = DestroyResult.builder()
                .destroyedCount(destroyedCount)
                .message(destroyedCount + "건의 개인정보가 파기되었습니다.")
                .executedAt(LocalDateTime.now())
                .build();
        
        return ResponseEntity.ok(ApiResponse.success(result));
    }
    
    /**
     * 파기 예정 건수 조회
     * GET /api/admin/destroy/pending
     */
    @GetMapping("/destroy/pending")
    @Operation(summary = "파기 예정 조회", description = "파기 예정인 개인정보 건수 조회")
    public ResponseEntity<ApiResponse<PendingDestroyInfo>> getPendingDestroyCount() {
        // 구현 필요
        PendingDestroyInfo info = PendingDestroyInfo.builder()
                .pendingCount(0)
                .nextScheduledAt(LocalDateTime.now().plusDays(1).withHour(0).withMinute(0))
                .build();
        
        return ResponseEntity.ok(ApiResponse.success(info));
    }
    
    /**
     * 파기 결과 DTO
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class DestroyResult {
        private int destroyedCount;
        private String message;
        private LocalDateTime executedAt;
    }
    
    /**
     * 파기 예정 정보 DTO
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class PendingDestroyInfo {
        private int pendingCount;
        private LocalDateTime nextScheduledAt;
    }
}
