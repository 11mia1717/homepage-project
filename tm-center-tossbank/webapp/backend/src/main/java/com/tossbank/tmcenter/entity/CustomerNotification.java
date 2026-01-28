package com.tossbank.tmcenter.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "customer_notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerNotification {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "marketing_request_id", nullable = false)
    private MarketingRequest marketingRequest;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType notificationType;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Channel channel;
    
    @Column(length = 20)
    private String recipientPhone;
    
    @Column(length = 100)
    private String recipientEmail;
    
    @Column(length = 200)
    private String title;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;
    
    private LocalDateTime sentAt;
    
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Status status = Status.PENDING;
    
    @Column(length = 500)
    private String errorMessage;
    
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    
    public enum NotificationType {
        CONSENT_COMPLETE,     // 동의 완료
        CALL_COMPLETE,        // 상담 완료
        RETENTION_REMINDER,   // 보유기간 만료 알림
        DESTROY_COMPLETE      // 파기 완료
    }
    
    public enum Channel {
        SMS,
        PUSH,
        EMAIL
    }
    
    public enum Status {
        PENDING,
        SENT,
        FAILED
    }
    
    // 발송 성공 처리
    public void markAsSent() {
        this.status = Status.SENT;
        this.sentAt = LocalDateTime.now();
    }
    
    // 발송 실패 처리
    public void markAsFailed(String errorMessage) {
        this.status = Status.FAILED;
        this.errorMessage = errorMessage;
    }
    
    // SMS 알림 생성 정적 팩토리 메서드
    public static CustomerNotification createSmsNotification(
            MarketingRequest request, 
            NotificationType type, 
            String content) {
        return CustomerNotification.builder()
                .marketingRequest(request)
                .notificationType(type)
                .channel(Channel.SMS)
                .recipientPhone(request.getCustomerPhone())
                .content(content)
                .build();
    }
}
