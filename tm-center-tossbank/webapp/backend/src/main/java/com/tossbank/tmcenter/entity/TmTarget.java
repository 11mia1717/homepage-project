package com.tossbank.tmcenter.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tm_targets")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TmTarget {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 50)
    private String externalRef;  // 외부 참조 ID (CI 대체)
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "marketing_request_id", nullable = false)
    private MarketingRequest marketingRequest;
    
    // 고객 정보 (최소한의 정보만)
    @Column(nullable = false, length = 100)
    private String customerName;
    
    @Column(nullable = false, length = 20)
    private String phone;
    
    // 상담 정보
    @Column(nullable = false, length = 100)
    private String productName;
    
    @Column(nullable = false, length = 500)
    private String consentPurpose;  // 동의 목적 (목적 외 사용 제한용)
    
    // 보유기간
    @Column(nullable = false)
    private LocalDate retentionUntil;
    
    // 배정 정보
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_agent_id")
    private User assignedAgent;
    
    private LocalDateTime assignedAt;
    
    // 상태
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private TargetStatus status = TargetStatus.WAITING;
    
    @Builder.Default
    private Integer priority = 0;
    
    // 파기 관련
    @Column(length = 1)
    @Builder.Default
    private String destroyedYn = "N";
    
    private LocalDateTime destroyedAt;
    
    @OneToMany(mappedBy = "tmTarget", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<CallResult> callResults = new ArrayList<>();
    
    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    private LocalDateTime updatedAt;
    
    public enum TargetStatus {
        WAITING,    // 대기중
        ASSIGNED,   // 배정됨
        IN_CALL,    // 통화중
        COMPLETED,  // 완료
        CALLBACK,   // 재통화 대기
        WITHDRAWN   // 동의 철회
    }
    
    // 상담사 배정
    public void assignTo(User agent) {
        this.assignedAgent = agent;
        this.assignedAt = LocalDateTime.now();
        this.status = TargetStatus.ASSIGNED;
    }
    
    // 개인정보 마스킹 처리 (파기)
    public void maskPersonalInfo() {
        this.customerName = "***";
        this.phone = "***";
        this.destroyedYn = "Y";
        this.destroyedAt = LocalDateTime.now();
    }
    
    // 동의 철회 처리
    public void withdraw() {
        this.status = TargetStatus.WITHDRAWN;
        maskPersonalInfo();
    }
    
    // 파기 대상 여부 확인
    public boolean isRetentionExpired() {
        return LocalDate.now().isAfter(this.retentionUntil) && "N".equals(this.destroyedYn);
    }
    
    // 목적 외 사용 확인
    public boolean isValidProduct(String consultedProduct) {
        return this.productName.equals(consultedProduct);
    }
}
