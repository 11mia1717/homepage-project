package com.tossbank.tmcenter.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 대시보드 통계 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardStats {
    
    // 전체 현황
    private long totalRequests;
    private long pendingRequests;
    private long inProgressRequests;
    private long completedRequests;
    private long withdrawnRequests;
    
    // 오늘 통계
    private long todayConsents;
    private long todayCalls;
    private long todayCompletions;
    
    // 상담 결과 통계
    private Map<String, Long> callResultStats;
    
    // 상품별 통계
    private Map<String, ProductStats> productStats;
    
    // 상담사별 통계
    private List<AgentStats> agentStats;
    
    // 일자별 추이
    private List<DailyStats> dailyStats;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ProductStats {
        private String productName;
        private long totalCount;
        private long agreedCount;
        private long pendingCount;
        private long refusedCount;
        private double conversionRate;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AgentStats {
        private Long agentId;
        private String agentName;
        private long totalCalls;
        private long successCalls;
        private long callbackCalls;
        private double successRate;
        private int avgCallDuration;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DailyStats {
        private String date;
        private long consents;
        private long calls;
        private long completions;
    }
}
