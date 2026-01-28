package com.tossbank.tmcenter.repository;

import com.tossbank.tmcenter.entity.MarketingRequest;
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
public interface MarketingRequestRepository extends JpaRepository<MarketingRequest, Long> {
    
    Optional<MarketingRequest> findByRequestId(String requestId);
    
    Page<MarketingRequest> findByStatus(MarketingRequest.RequestStatus status, Pageable pageable);
    
    // 파기 대상 조회 (보유기간 만료 & 미파기)
    @Query("SELECT m FROM MarketingRequest m WHERE m.retentionUntil < :today AND m.destroyedYn = 'N'")
    List<MarketingRequest> findExpiredAndNotDestroyed(@Param("today") LocalDate today);
    
    // 고객별 마케팅 동의 내역 조회
    @Query("SELECT m FROM MarketingRequest m WHERE m.customerPhone = :phone ORDER BY m.createdAt DESC")
    List<MarketingRequest> findByCustomerPhone(@Param("phone") String phone);
    
    // 상품별 통계
    @Query("SELECT m.productName, m.status, COUNT(m) FROM MarketingRequest m " +
           "GROUP BY m.productName, m.status")
    List<Object[]> countByProductAndStatus();
    
    // 일자별 동의 건수
    @Query("SELECT CAST(m.consentedAt AS LocalDate), COUNT(m) FROM MarketingRequest m " +
           "WHERE m.consentedAt >= :startDate GROUP BY CAST(m.consentedAt AS LocalDate)")
    List<Object[]> countByConsentDate(@Param("startDate") java.time.LocalDateTime startDate);
    
    // 배치 업데이트: 만료된 데이터 마스킹
    @Modifying
    @Query("UPDATE MarketingRequest m SET m.customerName = '***', m.customerPhone = '***', " +
           "m.customerCi = '***', m.destroyedYn = 'Y', m.destroyedAt = CURRENT_TIMESTAMP " +
           "WHERE m.retentionUntil < :today AND m.destroyedYn = 'N'")
    int maskExpiredPersonalInfo(@Param("today") LocalDate today);
}
