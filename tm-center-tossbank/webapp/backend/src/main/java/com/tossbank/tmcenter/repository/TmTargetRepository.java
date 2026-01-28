package com.tossbank.tmcenter.repository;

import com.tossbank.tmcenter.entity.TmTarget;
import com.tossbank.tmcenter.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TmTargetRepository extends JpaRepository<TmTarget, Long> {
    
    Optional<TmTarget> findByExternalRef(String externalRef);
    
    // 상담사에게 배정된 타겟 조회
    Page<TmTarget> findByAssignedAgentAndStatusIn(
            User agent, List<TmTarget.TargetStatus> statuses, Pageable pageable);
    
    // 대기중인 타겟 조회 (배정 대상)
    @Query("SELECT t FROM TmTarget t WHERE t.status = 'WAITING' AND t.destroyedYn = 'N' " +
           "ORDER BY t.priority DESC, t.createdAt ASC")
    List<TmTarget> findWaitingTargets(Pageable pageable);
    
    // 재통화 대상 조회
    @Query("SELECT t FROM TmTarget t WHERE t.status = 'CALLBACK' AND t.destroyedYn = 'N'")
    List<TmTarget> findCallbackTargets();
    
    // 상담사별 처리 건수
    @Query("SELECT t.assignedAgent, t.status, COUNT(t) FROM TmTarget t " +
           "WHERE t.assignedAgent IS NOT NULL GROUP BY t.assignedAgent, t.status")
    List<Object[]> countByAgentAndStatus();
    
    // 파기 대상 조회
    @Query("SELECT t FROM TmTarget t WHERE t.retentionUntil < :today AND t.destroyedYn = 'N'")
    List<TmTarget> findExpiredAndNotDestroyed(@Param("today") LocalDate today);
    
    // 배치 업데이트: 만료된 데이터 마스킹
    @Modifying
    @Query("UPDATE TmTarget t SET t.customerName = '***', t.phone = '***', " +
           "t.destroyedYn = 'Y', t.destroyedAt = CURRENT_TIMESTAMP " +
           "WHERE t.retentionUntil < :today AND t.destroyedYn = 'N'")
    int maskExpiredPersonalInfo(@Param("today") LocalDate today);
    
    // 특정 마케팅 요청의 TM 타겟
    Optional<TmTarget> findByMarketingRequestId(Long marketingRequestId);
    
    // 상태별 카운트
    @Query("SELECT t.status, COUNT(t) FROM TmTarget t WHERE t.destroyedYn = 'N' GROUP BY t.status")
    List<Object[]> countByStatus();
}
