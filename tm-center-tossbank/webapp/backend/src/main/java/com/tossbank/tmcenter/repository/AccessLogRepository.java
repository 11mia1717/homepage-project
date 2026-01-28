package com.tossbank.tmcenter.repository;

import com.tossbank.tmcenter.entity.AccessLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AccessLogRepository extends JpaRepository<AccessLog, Long> {
    
    // 특정 대상에 대한 접근 로그
    Page<AccessLog> findByTargetIdAndTargetType(
            Long targetId, AccessLog.TargetType targetType, Pageable pageable);
    
    // 특정 사용자의 접근 로그
    Page<AccessLog> findByAgentId(Long agentId, Pageable pageable);
    
    // 기간별 접근 로그
    @Query("SELECT a FROM AccessLog a WHERE a.accessedAt BETWEEN :start AND :end " +
           "ORDER BY a.accessedAt DESC")
    Page<AccessLog> findByDateRange(
            @Param("start") LocalDateTime start, 
            @Param("end") LocalDateTime end, 
            Pageable pageable);
    
    // 접근 유형별 통계
    @Query("SELECT a.accessType, COUNT(a) FROM AccessLog a " +
           "WHERE a.accessedAt >= :startDate GROUP BY a.accessType")
    List<Object[]> countByAccessType(@Param("startDate") LocalDateTime startDate);
    
    // 사용자별 접근 횟수
    @Query("SELECT a.agentId, a.agentName, COUNT(a) FROM AccessLog a " +
           "WHERE a.accessedAt >= :startDate GROUP BY a.agentId, a.agentName " +
           "ORDER BY COUNT(a) DESC")
    List<Object[]> countByAgent(@Param("startDate") LocalDateTime startDate);
    
    // 특정 사용자의 최근 접근 로그
    @Query("SELECT a FROM AccessLog a WHERE a.agentId = :agentId " +
           "ORDER BY a.accessedAt DESC")
    List<AccessLog> findRecentByAgent(@Param("agentId") Long agentId, Pageable pageable);
}
