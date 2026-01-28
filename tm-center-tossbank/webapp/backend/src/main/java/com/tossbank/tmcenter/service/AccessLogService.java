package com.tossbank.tmcenter.service;

import com.tossbank.tmcenter.entity.AccessLog;
import com.tossbank.tmcenter.repository.AccessLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccessLogService {
    
    private final AccessLogRepository accessLogRepository;
    
    /**
     * 접근 로그 조회 (기간별)
     */
    @Transactional(readOnly = true)
    public Page<AccessLog> getAccessLogs(LocalDateTime start, LocalDateTime end, Pageable pageable) {
        return accessLogRepository.findByDateRange(start, end, pageable);
    }
    
    /**
     * 특정 사용자의 접근 로그 조회
     */
    @Transactional(readOnly = true)
    public Page<AccessLog> getAccessLogsByAgent(Long agentId, Pageable pageable) {
        return accessLogRepository.findByAgentId(agentId, pageable);
    }
    
    /**
     * 특정 대상의 접근 로그 조회
     */
    @Transactional(readOnly = true)
    public Page<AccessLog> getAccessLogsByTarget(Long targetId, 
                                                  AccessLog.TargetType targetType, 
                                                  Pageable pageable) {
        return accessLogRepository.findByTargetIdAndTargetType(targetId, targetType, pageable);
    }
    
    /**
     * 접근 유형별 통계
     */
    @Transactional(readOnly = true)
    public Map<String, Long> getAccessTypeStats(int days) {
        LocalDateTime startDate = LocalDateTime.now().minusDays(days);
        List<Object[]> results = accessLogRepository.countByAccessType(startDate);
        
        return results.stream()
                .collect(Collectors.toMap(
                        row -> ((AccessLog.AccessType) row[0]).name(),
                        row -> (Long) row[1]
                ));
    }
    
    /**
     * 사용자별 접근 통계
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getAgentAccessStats(int days) {
        LocalDateTime startDate = LocalDateTime.now().minusDays(days);
        List<Object[]> results = accessLogRepository.countByAgent(startDate);
        
        return results.stream()
                .map(row -> Map.of(
                        "agentId", row[0],
                        "agentName", row[1],
                        "accessCount", row[2]
                ))
                .toList();
    }
}
