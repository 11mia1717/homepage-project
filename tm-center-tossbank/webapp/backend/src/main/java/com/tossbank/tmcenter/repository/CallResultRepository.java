package com.tossbank.tmcenter.repository;

import com.tossbank.tmcenter.entity.CallResult;
import com.tossbank.tmcenter.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CallResultRepository extends JpaRepository<CallResult, Long> {
    
    // 타겟별 상담 결과 조회
    List<CallResult> findByTmTargetIdOrderByCallStartedAtDesc(Long tmTargetId);
    
    // 상담사별 상담 결과 조회
    Page<CallResult> findByAgent(User agent, Pageable pageable);
    
    // 기간별 상담 결과 조회
    @Query("SELECT c FROM CallResult c WHERE c.callStartedAt BETWEEN :start AND :end " +
           "ORDER BY c.callStartedAt DESC")
    Page<CallResult> findByDateRange(
            @Param("start") LocalDateTime start, 
            @Param("end") LocalDateTime end, 
            Pageable pageable);
    
    // 재통화 예정 목록 조회
    @Query("SELECT c FROM CallResult c WHERE c.callbackScheduledAt <= :now " +
           "AND c.tmTarget.status = 'CALLBACK' AND c.tmTarget.destroyedYn = 'N'")
    List<CallResult> findDueCallbacks(@Param("now") LocalDateTime now);
    
    // 상담사별 통계
    @Query("SELECT c.agent.id, c.resultCode, COUNT(c) FROM CallResult c " +
           "WHERE c.callStartedAt >= :startDate GROUP BY c.agent.id, c.resultCode")
    List<Object[]> countByAgentAndResult(@Param("startDate") LocalDateTime startDate);
    
    // 일자별 상담 건수
    @Query("SELECT CAST(c.callStartedAt AS LocalDate), COUNT(c) FROM CallResult c " +
           "WHERE c.callStartedAt >= :startDate GROUP BY CAST(c.callStartedAt AS LocalDate)")
    List<Object[]> countByDate(@Param("startDate") LocalDateTime startDate);
    
    // 상품 결과별 통계
    @Query("SELECT c.consultedProduct, c.productResult, COUNT(c) FROM CallResult c " +
           "WHERE c.productResult IS NOT NULL GROUP BY c.consultedProduct, c.productResult")
    List<Object[]> countByProductResult();
    
    // 녹취 동의 통계
    @Query("SELECT c.recordingAgreedYn, COUNT(c) FROM CallResult c GROUP BY c.recordingAgreedYn")
    List<Object[]> countByRecordingAgreed();
}
