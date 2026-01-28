package com.tossbank.tmcenter.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "call_results")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CallResult {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tm_target_id", nullable = false)
    private TmTarget tmTarget;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agent_id", nullable = false)
    private User agent;
    
    // 통화 정보
    @Column(nullable = false)
    private LocalDateTime callStartedAt;
    
    private LocalDateTime callEndedAt;
    
    @Builder.Default
    private Integer callDuration = 0;  // 통화 시간 (초)
    
    // 녹취 관련
    @Column(length = 1)
    @Builder.Default
    private String recordingAgreedYn = "N";  // 녹취 사전 동의 여부
    
    @Column(length = 1)
    @Builder.Default
    private String recordedYn = "N";  // 실제 녹음 여부
    
    @Column(length = 500)
    private String recordingFileUrl;  // 녹취 파일 URL (S3)
    
    @Column(length = 255)
    private String recordingFileKey;  // 녹취 파일 S3 Key
    
    // 상담 결과
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ResultCode resultCode;
    
    @Column(length = 1000)
    private String resultDetail;
    
    // 상품 관련 (목적 외 사용 제한)
    @Column(nullable = false, length = 100)
    private String consultedProduct;  // 상담한 상품명
    
    @Enumerated(EnumType.STRING)
    private ProductResult productResult;
    
    // 재통화 관련
    private LocalDateTime callbackScheduledAt;
    
    @Column(length = 1)
    private String retryAgreedYn;
    
    private LocalDateTime retryAgreedAt;
    
    // 메모
    @Column(columnDefinition = "TEXT")
    private String memo;
    
    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    private LocalDateTime updatedAt;
    
    public enum ResultCode {
        SUCCESS,       // 상담 성공
        NO_ANSWER,     // 부재중
        BUSY,          // 통화중
        CALLBACK,      // 재통화 요청
        REFUSED,       // 상담 거부
        WRONG_NUMBER,  // 잘못된 번호
        OTHER          // 기타
    }
    
    public enum ProductResult {
        AGREED,   // 가입 동의
        PENDING,  // 보류/검토중
        REFUSED   // 거절
    }
    
    // 통화 종료 처리
    public void endCall() {
        this.callEndedAt = LocalDateTime.now();
        if (this.callStartedAt != null) {
            this.callDuration = (int) java.time.Duration.between(
                this.callStartedAt, this.callEndedAt
            ).getSeconds();
        }
    }
    
    // 녹취 동의 설정
    public void agreeRecording() {
        this.recordingAgreedYn = "Y";
    }
    
    // 재통화 예약
    public void scheduleCallback(LocalDateTime scheduledAt) {
        this.callbackScheduledAt = scheduledAt;
        this.resultCode = ResultCode.CALLBACK;
    }
    
    // 재통화 시 재동의
    public void agreeRetry() {
        this.retryAgreedYn = "Y";
        this.retryAgreedAt = LocalDateTime.now();
    }
}
