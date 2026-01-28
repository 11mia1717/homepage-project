package com.tossbank.tmcenter.repository;

import com.tossbank.tmcenter.entity.CustomerNotification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomerNotificationRepository extends JpaRepository<CustomerNotification, Long> {
    
    // 마케팅 요청별 알림 조회
    List<CustomerNotification> findByMarketingRequestIdOrderByCreatedAtDesc(Long marketingRequestId);
    
    // 발송 대기중인 알림 조회
    List<CustomerNotification> findByStatus(CustomerNotification.Status status);
    
    // 알림 유형별 조회
    Page<CustomerNotification> findByNotificationType(
            CustomerNotification.NotificationType type, Pageable pageable);
}
