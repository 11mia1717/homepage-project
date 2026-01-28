package com.tossbank.tmcenter.service;

import com.tossbank.tmcenter.dto.DashboardStats;
import com.tossbank.tmcenter.entity.CallResult;
import com.tossbank.tmcenter.entity.MarketingRequest;
import com.tossbank.tmcenter.entity.TmTarget;
import com.tossbank.tmcenter.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StatisticsService {
    
    private final MarketingRequestRepository marketingRequestRepository;
    private final TmTargetRepository tmTargetRepository;
    private final CallResultRepository callResultRepository;
    private final UserRepository userRepository;
    
    /**
     * 대시보드 통계 조회
     */
    @Transactional(readOnly = true)
    public DashboardStats getDashboardStats() {
        long totalRequests = marketingRequestRepository.count();
        
        // 상태별 카운트
        Map<String, Long> statusCounts = getStatusCounts();
        
        // 오늘 통계
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        List<Object[]> todayConsents = marketingRequestRepository.countByConsentDate(todayStart);
        List<Object[]> todayCalls = callResultRepository.countByDate(todayStart);
        
        // 상담 결과 통계
        Map<String, Long> callResultStats = getCallResultStats(7);
        
        // 상품별 통계
        Map<String, DashboardStats.ProductStats> productStats = getProductStats();
        
        // 상담사별 통계
        List<DashboardStats.AgentStats> agentStats = getAgentStats(7);
        
        // 일자별 추이 (최근 7일)
        List<DashboardStats.DailyStats> dailyStats = getDailyStats(7);
        
        return DashboardStats.builder()
                .totalRequests(totalRequests)
                .pendingRequests(statusCounts.getOrDefault("PENDING", 0L))
                .inProgressRequests(statusCounts.getOrDefault("IN_PROGRESS", 0L))
                .completedRequests(statusCounts.getOrDefault("COMPLETED", 0L))
                .withdrawnRequests(statusCounts.getOrDefault("WITHDRAWN", 0L))
                .todayConsents(todayConsents.isEmpty() ? 0 : (Long) todayConsents.get(0)[1])
                .todayCalls(todayCalls.isEmpty() ? 0 : (Long) todayCalls.get(0)[1])
                .todayCompletions(getCompletedCountToday())
                .callResultStats(callResultStats)
                .productStats(productStats)
                .agentStats(agentStats)
                .dailyStats(dailyStats)
                .build();
    }
    
    /**
     * 상태별 카운트
     */
    private Map<String, Long> getStatusCounts() {
        List<Object[]> targetStats = tmTargetRepository.countByStatus();
        return targetStats.stream()
                .collect(Collectors.toMap(
                        row -> ((TmTarget.TargetStatus) row[0]).name(),
                        row -> (Long) row[1]
                ));
    }
    
    /**
     * 상담 결과 통계
     */
    private Map<String, Long> getCallResultStats(int days) {
        LocalDateTime startDate = LocalDateTime.now().minusDays(days);
        List<Object[]> results = callResultRepository.countByAgentAndResult(startDate);
        
        Map<String, Long> stats = new HashMap<>();
        for (Object[] row : results) {
            String resultCode = ((CallResult.ResultCode) row[1]).name();
            Long count = (Long) row[2];
            stats.merge(resultCode, count, Long::sum);
        }
        return stats;
    }
    
    /**
     * 상품별 통계
     */
    private Map<String, DashboardStats.ProductStats> getProductStats() {
        List<Object[]> results = callResultRepository.countByProductResult();
        Map<String, DashboardStats.ProductStats> productStats = new HashMap<>();
        
        for (Object[] row : results) {
            String productName = (String) row[0];
            CallResult.ProductResult result = (CallResult.ProductResult) row[1];
            Long count = (Long) row[2];
            
            DashboardStats.ProductStats stats = productStats.computeIfAbsent(
                    productName,
                    k -> DashboardStats.ProductStats.builder().productName(k).build()
            );
            
            switch (result) {
                case AGREED -> stats.setAgreedCount(count);
                case PENDING -> stats.setPendingCount(count);
                case REFUSED -> stats.setRefusedCount(count);
            }
            
            stats.setTotalCount(stats.getAgreedCount() + stats.getPendingCount() + stats.getRefusedCount());
            if (stats.getTotalCount() > 0) {
                stats.setConversionRate((double) stats.getAgreedCount() / stats.getTotalCount() * 100);
            }
        }
        
        return productStats;
    }
    
    /**
     * 상담사별 통계
     */
    private List<DashboardStats.AgentStats> getAgentStats(int days) {
        LocalDateTime startDate = LocalDateTime.now().minusDays(days);
        List<Object[]> results = callResultRepository.countByAgentAndResult(startDate);
        
        Map<Long, DashboardStats.AgentStats> agentStatsMap = new HashMap<>();
        
        for (Object[] row : results) {
            Long agentId = (Long) row[0];
            CallResult.ResultCode resultCode = (CallResult.ResultCode) row[1];
            Long count = (Long) row[2];
            
            DashboardStats.AgentStats stats = agentStatsMap.computeIfAbsent(
                    agentId,
                    k -> {
                        var user = userRepository.findById(k).orElse(null);
                        return DashboardStats.AgentStats.builder()
                                .agentId(k)
                                .agentName(user != null ? user.getName() : "Unknown")
                                .build();
                    }
            );
            
            stats.setTotalCalls(stats.getTotalCalls() + count);
            if (resultCode == CallResult.ResultCode.SUCCESS) {
                stats.setSuccessCalls(stats.getSuccessCalls() + count);
            } else if (resultCode == CallResult.ResultCode.CALLBACK) {
                stats.setCallbackCalls(stats.getCallbackCalls() + count);
            }
            
            if (stats.getTotalCalls() > 0) {
                stats.setSuccessRate((double) stats.getSuccessCalls() / stats.getTotalCalls() * 100);
            }
        }
        
        return new ArrayList<>(agentStatsMap.values());
    }
    
    /**
     * 일자별 통계
     */
    private List<DashboardStats.DailyStats> getDailyStats(int days) {
        LocalDateTime startDate = LocalDateTime.now().minusDays(days);
        
        List<Object[]> consentsByDate = marketingRequestRepository.countByConsentDate(startDate);
        List<Object[]> callsByDate = callResultRepository.countByDate(startDate);
        
        Map<String, DashboardStats.DailyStats> dailyMap = new LinkedHashMap<>();
        
        // 날짜 초기화
        for (int i = days - 1; i >= 0; i--) {
            String date = LocalDate.now().minusDays(i).toString();
            dailyMap.put(date, DashboardStats.DailyStats.builder().date(date).build());
        }
        
        // 동의 데이터
        for (Object[] row : consentsByDate) {
            String date = row[0].toString();
            if (dailyMap.containsKey(date)) {
                dailyMap.get(date).setConsents((Long) row[1]);
            }
        }
        
        // 상담 데이터
        for (Object[] row : callsByDate) {
            String date = row[0].toString();
            if (dailyMap.containsKey(date)) {
                dailyMap.get(date).setCalls((Long) row[1]);
            }
        }
        
        return new ArrayList<>(dailyMap.values());
    }
    
    /**
     * 오늘 완료 건수
     */
    private long getCompletedCountToday() {
        // 간소화된 구현
        return 0L;
    }
}
