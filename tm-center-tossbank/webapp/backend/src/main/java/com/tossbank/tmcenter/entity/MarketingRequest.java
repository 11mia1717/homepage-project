package com.tossbank.tmcenter.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "marketing_requests")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MarketingRequest {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true, length = 50)
    private String requestId;  // TM20260127001 형태
    
    // 고객 정보 (개인정보)
    @Column(nullable = false, length = 100)
    private String customerName;
    
    @Column(nullable = false, length = 20)
    private String customerPhone;
    
    @Column(length = 255)
    private String customerCi;  // CI 연계정보 (암호화 저장)
    
    // 동의 정보
    @Column(nullable = false, length = 100)
    private String productName;  // 상품명
    
    @Column(nullable = false, length = 500)
    private String consentPurpose;  // 동의 목적
    
    @Column(nullable = false, length = 500)
    private String consentItems;  // 제공 항목
    
    @Column(nullable = false, length = 200)
    private String consentRecipient;  // 제공받는 자
    
    @Column(nullable = false)
    private LocalDateTime consentedAt;  // 동의 일시
    
    // 보유기간 및 파기 관련
    @Column(nullable = false)
    private LocalDate retentionUntil;  // 개인정보 보유 기한
    
    // 상태
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private RequestStatus status = RequestStatus.PENDING;
    
    // 동의 철회 관련
    private LocalDateTime withdrawnAt;
    
    @Column(length = 500)
    private String withdrawalReason;
    
    // 파기 관련
    @Column(length = 1)
    @Builder.Default
    private String destroyedYn = "N";
    
    private LocalDateTime destroyedAt;
    
    @OneToOne(mappedBy = "marketingRequest", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private TmTarget tmTarget;
    
    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    private LocalDateTime updatedAt;
    
    public enum RequestStatus {
        PENDING,      // 대기중
        IN_PROGRESS,  // 처리중
        COMPLETED,    // 완료
        CANCELLED,    // 취소
        WITHDRAWN     // 동의 철회
    }
    
    // 동의 철회 처리
    public void withdraw(String reason) {
        this.status = RequestStatus.WITHDRAWN;
        this.withdrawnAt = LocalDateTime.now();
        this.withdrawalReason = reason;
    }
    
    // 개인정보 마스킹 처리 (파기)
    public void maskPersonalInfo() {
        this.customerName = "***";
        this.customerPhone = "***";
        this.customerCi = "***";
        this.destroyedYn = "Y";
        this.destroyedAt = LocalDateTime.now();
    }
    
    // 파기 대상 여부 확인
    public boolean isRetentionExpired() {
        return LocalDate.now().isAfter(this.retentionUntil) && "N".equals(this.destroyedYn);
    }
}
