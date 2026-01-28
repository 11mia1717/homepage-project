package com.tossbank.tmcenter.repository;

import com.tossbank.tmcenter.entity.DestructionLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DestructionLogRepository extends JpaRepository<DestructionLog, Long> {
    
    // 테이블별 파기 로그 조회
    Page<DestructionLog> findByTargetTable(String targetTable, Pageable pageable);
    
    // 기간별 파기 로그 조회
    @Query("SELECT d FROM DestructionLog d WHERE d.executedAt BETWEEN :start AND :end " +
           "ORDER BY d.executedAt DESC")
    Page<DestructionLog> findByDateRange(
            @Param("start") LocalDateTime start, 
            @Param("end") LocalDateTime end, 
            Pageable pageable);
    
    // 파기 유형별 통계
    @Query("SELECT d.destructionType, COUNT(d) FROM DestructionLog d " +
           "WHERE d.executedAt >= :startDate GROUP BY d.destructionType")
    List<Object[]> countByType(@Param("startDate") LocalDateTime startDate);
    
    // 일자별 파기 건수
    @Query("SELECT CAST(d.executedAt AS LocalDate), COUNT(d) FROM DestructionLog d " +
           "WHERE d.executedAt >= :startDate GROUP BY CAST(d.executedAt AS LocalDate)")
    List<Object[]> countByDate(@Param("startDate") LocalDateTime startDate);
}
