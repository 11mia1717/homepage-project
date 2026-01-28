package com.tossbank.tmcenter.scheduler;

import com.tossbank.tmcenter.entity.DestructionLog;
import com.tossbank.tmcenter.entity.MarketingRequest;
import com.tossbank.tmcenter.entity.TmTarget;
import com.tossbank.tmcenter.repository.DestructionLogRepository;
import com.tossbank.tmcenter.repository.MarketingRequestRepository;
import com.tossbank.tmcenter.repository.TmTargetRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * 개인정보 자동 파기 배치 작업
 * 매일 자정에 실행되어 보유기간이 만료된 개인정보를 마스킹 처리
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataDestructionScheduler {
    
    private final MarketingRequestRepository marketingRequestRepository;
    private final TmTargetRepository tmTargetRepository;
    private final DestructionLogRepository destructionLogRepository;
    
    /**
     * 자동 파기 배치 작업
     * 매일 자정(00:00:00)에 실행
     */
    @Scheduled(cron = "${app.batch.destroy.cron:0 0 0 * * ?}")
    @Transactional
    public void destroyExpiredPersonalData() {
        log.info("=== 개인정보 자동 파기 배치 작업 시작 ===");
        
        LocalDate today = LocalDate.now();
        
        try {
            // 1. 만료된 마케팅 요청 조회
            List<MarketingRequest> expiredRequests = 
                    marketingRequestRepository.findExpiredAndNotDestroyed(today);
            
            log.info("파기 대상 마케팅 요청 건수: {}", expiredRequests.size());
            
            // 2. 마케팅 요청 파기 처리
            for (MarketingRequest request : expiredRequests) {
                destroyMarketingRequest(request);
            }
            
            // 3. 만료된 TM 타겟 조회
            List<TmTarget> expiredTargets = 
                    tmTargetRepository.findExpiredAndNotDestroyed(today);
            
            log.info("파기 대상 TM 타겟 건수: {}", expiredTargets.size());
            
            // 4. TM 타겟 파기 처리
            for (TmTarget target : expiredTargets) {
                destroyTmTarget(target);
            }
            
            log.info("=== 개인정보 자동 파기 배치 작업 완료 ===");
            log.info("처리 결과 - 마케팅 요청: {}건, TM 타겟: {}건", 
                    expiredRequests.size(), expiredTargets.size());
            
        } catch (Exception e) {
            log.error("개인정보 자동 파기 배치 작업 실패", e);
            throw e;
        }
    }
    
    /**
     * 마케팅 요청 파기 처리
     */
    private void destroyMarketingRequest(MarketingRequest request) {
        log.debug("마케팅 요청 파기 처리: requestId={}", request.getRequestId());
        
        // 개인정보 마스킹
        request.maskPersonalInfo();
        
        // 파기 로그 기록
        DestructionLog destructionLog = DestructionLog.createAutoDestructionLog(
                "marketing_requests",
                request.getId(),
                "customer_name, customer_phone, customer_ci"
        );
        destructionLogRepository.save(destructionLog);
    }
    
    /**
     * TM 타겟 파기 처리
     */
    private void destroyTmTarget(TmTarget target) {
        log.debug("TM 타겟 파기 처리: targetId={}", target.getId());
        
        // 개인정보 마스킹
        target.maskPersonalInfo();
        
        // 파기 로그 기록
        DestructionLog destructionLog = DestructionLog.createAutoDestructionLog(
                "tm_targets",
                target.getId(),
                "customer_name, phone"
        );
        destructionLogRepository.save(destructionLog);
    }
    
    /**
     * 수동 파기 실행 (관리자용)
     */
    @Transactional
    public int manualDestroy() {
        log.info("수동 파기 실행");
        
        LocalDate today = LocalDate.now();
        int requestCount = marketingRequestRepository.maskExpiredPersonalInfo(today);
        int targetCount = tmTargetRepository.maskExpiredPersonalInfo(today);
        
        log.info("수동 파기 완료 - 마케팅 요청: {}건, TM 타겟: {}건", requestCount, targetCount);
        
        return requestCount + targetCount;
    }
}
