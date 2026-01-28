package com.tossbank.tmcenter.service;

import com.tossbank.tmcenter.dto.ConsentRequest;
import com.tossbank.tmcenter.dto.ConsentResponse;
import com.tossbank.tmcenter.entity.*;
import com.tossbank.tmcenter.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConsentService {
    
    private final MarketingRequestRepository marketingRequestRepository;
    private final TmTargetRepository tmTargetRepository;
    private final CustomerNotificationRepository notificationRepository;
    private final TrusteeClient trusteeClient; // Inject TrusteeClient
    
    @Value("${app.retention-days:90}")
    private int retentionDays;
    
    private static final AtomicLong sequence = new AtomicLong(1);
    
    /**
     * 마케팅 동의 처리
     */
    @Transactional
    public ConsentResponse processConsent(ConsentRequest request) {
        log.info("마케팅 동의 처리 시작: 상품={}, TokenID={}", 
                request.getProductName(), request.getTokenId());
        
        // [본인인증 검증]
        String verifiedName;
        String verifiedPhone;

        if (request.getTokenId() != null) {
            TrusteeClient.VerificationResult verification = trusteeClient.verifyToken(request.getTokenId());
            verifiedName = verification.getName();
            verifiedPhone = verification.getPhoneNumber();
            log.info("본인인증 완료: {} ({})", verifiedName, verifiedPhone);
        } else {
             // 기존 호환성 유지 또는 강제 차단 based on requirement.
             // "본인인증 수탁사와 비슷하게 로직 다시 구성해줘" -> Verification is mandatory.
             throw new IllegalArgumentException("본인인증이 수행되지 않았습니다.");
        }

        // 동의 여부 검증
        if (!request.getAgreeThirdPartyProvision()) {
            throw new IllegalArgumentException("제3자 제공 동의는 필수입니다.");
        }
        
        // 요청 ID 생성
        String requestId = generateRequestId();
        
        // 보유 기한 계산 (상담 완료 후 3개월이므로 일단 동의일 + 3개월로 설정)
        LocalDate retentionUntil = LocalDate.now().plusDays(retentionDays);
        
        // 마케팅 요청 생성
        MarketingRequest marketingRequest = MarketingRequest.builder()
                .requestId(requestId)
                .customerName(verifiedName) // Use verified name
                .customerPhone(normalizePhone(verifiedPhone)) // Use verified phone
                .productName(request.getProductName())
                .consentPurpose(request.getProductName() + " 상담")
                .consentItems("이름, 연락처")
                .consentRecipient("○○TM센터")
                .consentedAt(LocalDateTime.now())
                .retentionUntil(retentionUntil)
                .status(MarketingRequest.RequestStatus.PENDING)
                .build();
        
        marketingRequestRepository.save(marketingRequest);
        
        // TM 타겟 생성 (CI 대신 request_id 사용)
        TmTarget tmTarget = TmTarget.builder()
                .externalRef(requestId)
                .marketingRequest(marketingRequest)
                .customerName(verifiedName) // Use verified name
                .phone(normalizePhone(verifiedPhone)) // Use verified phone
                .productName(request.getProductName())
                .consentPurpose(request.getProductName() + " 상담")
                .retentionUntil(retentionUntil)
                .status(TmTarget.TargetStatus.WAITING)
                .build();
        
        tmTargetRepository.save(tmTarget);
        
        // 고객 알림 생성
        String notificationContent = String.format(
            "[토스뱅크]\n%s 마케팅 상담 동의가 완료되었습니다.\n" +
            "개인정보는 %s까지 보유 후 자동 파기됩니다.",
            request.getProductName(),
            retentionUntil.format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일"))
        );
        
        CustomerNotification notification = CustomerNotification.createSmsNotification(
                marketingRequest,
                CustomerNotification.NotificationType.CONSENT_COMPLETE,
                notificationContent
        );
        notificationRepository.save(notification);
        
        log.info("마케팅 동의 처리 완료: requestId={}", requestId);
        
        return ConsentResponse.from(marketingRequest);
    }
    
    /**
     * 동의 철회 처리
     */
    @Transactional
    public void withdrawConsent(String requestId, String reason) {
        log.info("동의 철회 처리 시작: requestId={}", requestId);
        
        MarketingRequest marketingRequest = marketingRequestRepository.findByRequestId(requestId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 요청입니다."));
        
        if (marketingRequest.getStatus() == MarketingRequest.RequestStatus.WITHDRAWN) {
            throw new IllegalStateException("이미 철회된 동의입니다.");
        }
        
        // 마케팅 요청 철회 처리
        marketingRequest.withdraw(reason);
        
        // TM 타겟 철회 처리
        TmTarget tmTarget = tmTargetRepository.findByMarketingRequestId(marketingRequest.getId())
                .orElse(null);
        
        if (tmTarget != null) {
            tmTarget.withdraw();
        }
        
        // 고객 알림 생성
        String notificationContent = String.format(
            "[토스뱅크]\n%s 마케팅 동의가 철회되었습니다.\n" +
            "개인정보가 즉시 파기 처리되었습니다.",
            marketingRequest.getProductName()
        );
        
        CustomerNotification notification = CustomerNotification.createSmsNotification(
                marketingRequest,
                CustomerNotification.NotificationType.DESTROY_COMPLETE,
                notificationContent
        );
        notificationRepository.save(notification);
        
        log.info("동의 철회 처리 완료: requestId={}", requestId);
    }
    
    /**
     * 고객별 마케팅 동의 내역 조회
     */
    @Transactional(readOnly = true)
    public List<ConsentResponse> getConsentHistory(String phone) {
        return marketingRequestRepository.findByCustomerPhone(normalizePhone(phone))
                .stream()
                .map(ConsentResponse::from)
                .toList();
    }
    
    /**
     * 요청 ID 생성 (TM + 날짜 + 시퀀스)
     */
    private String generateRequestId() {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long seq = sequence.getAndIncrement();
        return String.format("TM%s%03d", date, seq % 1000);
    }
    
    /**
     * 전화번호 정규화 (하이픈 제거)
     */
    private String normalizePhone(String phone) {
        return phone.replaceAll("-", "");
    }
}
