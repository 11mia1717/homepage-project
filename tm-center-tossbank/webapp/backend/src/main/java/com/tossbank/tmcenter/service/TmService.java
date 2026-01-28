package com.tossbank.tmcenter.service;

import com.tossbank.tmcenter.dto.CallResultDto;
import com.tossbank.tmcenter.dto.CallResultRequest;
import com.tossbank.tmcenter.dto.TmTargetDto;
import com.tossbank.tmcenter.entity.*;
import com.tossbank.tmcenter.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TmService {
    
    private final TmTargetRepository tmTargetRepository;
    private final CallResultRepository callResultRepository;
    private final UserRepository userRepository;
    private final AccessLogRepository accessLogRepository;
    private final CustomerNotificationRepository notificationRepository;
    private final MarketingRequestRepository marketingRequestRepository;
    
    /**
     * 다음 고객 불러오기 (상담 대상 배정)
     */
    @Transactional
    public TmTargetDto getNextTarget(Long agentId, String ipAddress, String userAgent) {
        User agent = userRepository.findById(agentId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상담사입니다."));
        
        // 대기중인 타겟 조회
        List<TmTarget> waitingTargets = tmTargetRepository.findWaitingTargets(PageRequest.of(0, 1));
        
        if (waitingTargets.isEmpty()) {
            return null;  // 대기중인 고객 없음
        }
        
        TmTarget target = waitingTargets.get(0);
        
        // 동의 철회 확인
        if (target.getStatus() == TmTarget.TargetStatus.WITHDRAWN) {
            log.info("동의 철회된 고객: targetId={}", target.getId());
            return null;
        }
        
        // 상담사 배정
        target.assignTo(agent);
        
        // 접근 로그 기록
        recordAccessLog(agent, target, AccessLog.AccessType.VIEW, 
                target.getConsentPurpose(), ipAddress, userAgent);
        
        // 마케팅 요청 상태 업데이트
        MarketingRequest marketingRequest = target.getMarketingRequest();
        if (marketingRequest.getStatus() == MarketingRequest.RequestStatus.PENDING) {
            marketingRequest.setStatus(MarketingRequest.RequestStatus.IN_PROGRESS);
        }
        
        return TmTargetDto.from(target);
    }
    
    /**
     * 배정된 고객 목록 조회
     */
    @Transactional(readOnly = true)
    public Page<TmTargetDto> getAssignedTargets(Long agentId, Pageable pageable, 
                                                 String ipAddress, String userAgent) {
        User agent = userRepository.findById(agentId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상담사입니다."));
        
        List<TmTarget.TargetStatus> statuses = List.of(
                TmTarget.TargetStatus.ASSIGNED,
                TmTarget.TargetStatus.IN_CALL,
                TmTarget.TargetStatus.CALLBACK
        );
        
        Page<TmTarget> targets = tmTargetRepository.findByAssignedAgentAndStatusIn(
                agent, statuses, pageable);
        
        return targets.map(TmTargetDto::from);
    }
    
    /**
     * 고객 상세 정보 조회
     */
    @Transactional(readOnly = true)
    public TmTargetDto getTargetDetail(Long targetId, Long agentId, 
                                       String ipAddress, String userAgent) {
        TmTarget target = tmTargetRepository.findById(targetId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 고객입니다."));
        
        User agent = userRepository.findById(agentId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상담사입니다."));
        
        // 동의 철회 확인
        if (target.getStatus() == TmTarget.TargetStatus.WITHDRAWN) {
            throw new IllegalStateException("해당 고객이 동의를 철회하였습니다.");
        }
        
        // 파기 여부 확인
        if ("Y".equals(target.getDestroyedYn())) {
            throw new IllegalStateException("해당 고객 정보는 파기되었습니다.");
        }
        
        // 접근 로그 기록
        recordAccessLog(agent, target, AccessLog.AccessType.VIEW, 
                target.getConsentPurpose(), ipAddress, userAgent);
        
        return TmTargetDto.from(target);
    }
    
    /**
     * 상담 시작 (통화 시작)
     */
    @Transactional
    public void startCall(Long targetId, Long agentId) {
        TmTarget target = tmTargetRepository.findById(targetId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 고객입니다."));
        
        target.setStatus(TmTarget.TargetStatus.IN_CALL);
    }
    
    /**
     * 상담 결과 저장
     */
    @Transactional
    public CallResultDto saveCallResult(CallResultRequest request, Long agentId,
                                        String ipAddress, String userAgent) {
        TmTarget target = tmTargetRepository.findById(request.getTmTargetId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 고객입니다."));
        
        User agent = userRepository.findById(agentId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상담사입니다."));
        
        // 목적 외 사용 제한 검증
        if (!target.isValidProduct(request.getConsultedProduct())) {
            throw new IllegalArgumentException(
                    String.format("동의한 상품(%s)과 다른 상담(%s)은 불가능합니다.",
                            target.getProductName(), request.getConsultedProduct()));
        }
        
        // 상담 결과 생성
        CallResult callResult = CallResult.builder()
                .tmTarget(target)
                .agent(agent)
                .callStartedAt(LocalDateTime.now().minusMinutes(5))  // 임시
                .callEndedAt(LocalDateTime.now())
                .recordingAgreedYn(request.getRecordingAgreed() ? "Y" : "N")
                .recordedYn(request.getRecordingAgreed() ? "Y" : "N")
                .resultCode(request.getResultCode())
                .resultDetail(request.getResultDetail())
                .consultedProduct(request.getConsultedProduct())
                .productResult(request.getProductResult())
                .memo(request.getMemo())
                .build();
        
        // 재통화 예약
        if (request.getResultCode() == CallResult.ResultCode.CALLBACK 
                && request.getCallbackScheduledAt() != null) {
            callResult.scheduleCallback(request.getCallbackScheduledAt());
            target.setStatus(TmTarget.TargetStatus.CALLBACK);
        } else if (request.getResultCode() == CallResult.ResultCode.SUCCESS) {
            // 상담 성공 시 완료 처리
            target.setStatus(TmTarget.TargetStatus.COMPLETED);
            
            // 마케팅 요청도 완료 처리
            MarketingRequest marketingRequest = target.getMarketingRequest();
            marketingRequest.setStatus(MarketingRequest.RequestStatus.COMPLETED);
            
            // 보유 기한 재설정 (상담 완료일 + 3개월)
            LocalDate newRetention = LocalDate.now().plusMonths(3);
            target.setRetentionUntil(newRetention);
            marketingRequest.setRetentionUntil(newRetention);
            
            // 고객 알림 생성
            sendCallCompleteNotification(marketingRequest, callResult);
        } else {
            target.setStatus(TmTarget.TargetStatus.ASSIGNED);
        }
        
        // 재통화 시 재동의 처리
        if (Boolean.TRUE.equals(request.getRetryAgreed())) {
            callResult.agreeRetry();
        }
        
        callResult.endCall();
        callResultRepository.save(callResult);
        
        // 접근 로그 기록
        recordAccessLog(agent, target, AccessLog.AccessType.UPDATE, 
                "상담 결과 저장: " + request.getResultCode(), ipAddress, userAgent);
        
        return CallResultDto.from(callResult);
    }
    
    /**
     * 상담 이력 조회
     */
    @Transactional(readOnly = true)
    public List<CallResultDto> getCallHistory(Long targetId) {
        return callResultRepository.findByTmTargetIdOrderByCallStartedAtDesc(targetId)
                .stream()
                .map(CallResultDto::from)
                .toList();
    }
    
    /**
     * 재통화 대상 목록 조회
     */
    @Transactional(readOnly = true)
    public List<TmTargetDto> getCallbackTargets() {
        return tmTargetRepository.findCallbackTargets()
                .stream()
                .map(TmTargetDto::from)
                .toList();
    }
    
    /**
     * 접근 로그 기록
     */
    private void recordAccessLog(User agent, TmTarget target, AccessLog.AccessType accessType,
                                 String purpose, String ipAddress, String userAgent) {
        AccessLog accessLog = AccessLog.createViewLog(
                agent.getId(),
                agent.getName(),
                target.getId(),
                AccessLog.TargetType.TM_TARGET,
                purpose,
                ipAddress,
                userAgent
        );
        accessLog.setAccessType(accessType);
        accessLogRepository.save(accessLog);
    }
    
    /**
     * 상담 완료 알림 발송
     */
    private void sendCallCompleteNotification(MarketingRequest request, CallResult result) {
        String productResultText = "";
        if (result.getProductResult() != null) {
            productResultText = switch (result.getProductResult()) {
                case AGREED -> "가입 동의";
                case PENDING -> "검토중";
                case REFUSED -> "거절";
            };
        }
        
        String content = String.format(
            "[토스뱅크]\n%s 상담이 완료되었습니다.\n" +
            "상담 결과: %s\n" +
            "제공하신 개인정보는 %s 후 자동으로 파기됩니다.",
            request.getProductName(),
            productResultText,
            request.getRetentionUntil().format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일"))
        );
        
        CustomerNotification notification = CustomerNotification.createSmsNotification(
                request,
                CustomerNotification.NotificationType.CALL_COMPLETE,
                content
        );
        notificationRepository.save(notification);
    }
}
