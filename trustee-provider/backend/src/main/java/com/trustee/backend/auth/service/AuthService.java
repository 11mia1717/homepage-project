package com.trustee.backend.auth.service;

import com.trustee.backend.auth.dto.AuthConfirmRequest;
import com.trustee.backend.auth.dto.AuthInitRequest;
import com.trustee.backend.auth.dto.AuthInitResponse;
import com.trustee.backend.auth.dto.AuthStatusResponse;
import com.trustee.backend.auth.entity.AuthStatus;
import com.trustee.backend.auth.entity.AuthToken;
import com.trustee.backend.auth.repository.AuthTokenRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;

@Service
public class AuthService {

    private final AuthTokenRepository authTokenRepository;
    private final Random random = new Random();

    public AuthService(AuthTokenRepository authTokenRepository) {
        this.authTokenRepository = authTokenRepository;
    }

    @Transactional
    public AuthInitResponse initAuth(AuthInitRequest request) {
        UUID tokenId = UUID.randomUUID();
        // Generate a 6-digit OTP
        String otp = String.format("%06d", random.nextInt(1000000));

        // In a real scenario, you'd send this OTP via SMS/Kakao to
        // request.getClientData()
        System.out.println("[TRUSTEE] Generated OTP for " + request.getClientData() + ": " + otp);

        AuthToken authToken = new AuthToken(tokenId, request.getClientData(), request.getName(), otp,
                AuthStatus.PENDING,
                LocalDateTime.now());
        authTokenRepository.save(authToken);
        return new AuthInitResponse(tokenId, otp); // Returning OTP for testing convenience
    }

    @Transactional(readOnly = true)
    public AuthStatusResponse getAuthStatus(UUID tokenId) {
        AuthToken authToken = authTokenRepository.findById(tokenId)
                .orElseThrow(() -> new IllegalArgumentException("AuthToken not found with id: " + tokenId));
        return new AuthStatusResponse(authToken.getTokenId(), authToken.getStatus(), authToken.getName());
    }

    @Transactional
    public void confirmAuth(AuthConfirmRequest request) {
        UUID requestedTokenId = request.getTokenId();
        String requestedOtp = request.getOtp();

        System.out.println("[TRUSTEE-DEBUG] Verification Attempt - TokenID: " + requestedTokenId + ", OTP: ["
                + requestedOtp + "]");

        if (requestedTokenId == null || requestedOtp == null || requestedOtp.trim().isEmpty()) {
            System.err.println("[TRUSTEE-DEBUG] Validation Failed: TokenID or OTP is null/empty");
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST, "인증 토큰 또는 번호가 누락되었습니다.");
        }

        AuthToken authToken = authTokenRepository.findById(requestedTokenId)
                .orElseThrow(() -> {
                    System.err.println("[TRUSTEE-DEBUG] Token NOT FOUND in DB: " + requestedTokenId);
                    return new org.springframework.web.server.ResponseStatusException(
                            org.springframework.http.HttpStatus.NOT_FOUND, "인증 세션이 만료되었거나 존재하지 않습니다. 다시 시도해 주세요.");
                });

        String storedOtp = authToken.getOtp().trim();
        String sentOtp = requestedOtp.trim();

        System.out.println("[TRUSTEE-DEBUG] Comparing OTPs - Stored: [" + storedOtp + "], Received: [" + sentOtp + "]");

        if (!storedOtp.equals(sentOtp)) {
            System.err.println("[TRUSTEE-DEBUG] MISMATH !! Stored: [" + storedOtp + "] != Received: [" + sentOtp + "]");
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST, "인증번호가 일치하지 않습니다. (입력값: " + sentOtp + ")");
        }

        authToken.setStatus(AuthStatus.COMPLETED);
        authTokenRepository.save(authToken);
        System.out.println("[TRUSTEE-DEBUG] SUCCESS !! Verification Completed for Token: " + requestedTokenId);
    }

    @Transactional
    public AuthInitResponse requestOtp(com.trustee.backend.auth.dto.AuthOtpRequest request) {
        AuthToken authToken = authTokenRepository.findById(request.getTokenId())
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 인증 세션입니다. (Token NOT FOUND)"));

        // [핵심] 위탁사에서 등록한 정보와 현재 입력한 정보가 일치하는지 검증
        String expectedPhone = authToken.getClientData().replaceAll("\\D", "");
        String inputPhone = request.getPhoneNumber().replaceAll("\\D", "");
        String expectedName = authToken.getName();
        String inputName = request.getName();

        System.out.println("[TRUSTEE-SEC] Validating Identity - Expected: [" + expectedName + ", " + expectedPhone
                + "], Input: [" + inputName + ", " + inputPhone + "]");

        if (!expectedPhone.equals(inputPhone) || (expectedName != null && !expectedName.equals(inputName))) {
            throw new IllegalArgumentException("정보 불일치: 회원정보와 입력하신 정보가 다릅니다.");
        }

        // [실물 시뮬레이션] 주민번호 검증 (여기서는 생년월일이 6자인지만 체크)
        if (request.getResidentFront() == null || request.getResidentFront().length() != 6) {
            throw new IllegalArgumentException("주민등록번호 형식이 올바르지 않습니다.");
        }

        // 새로운 OTP 생성 및 세션 업데이트
        String newOtp = String.format("%06d", random.nextInt(1000000));
        authToken.setOtp(newOtp);
        authTokenRepository.save(authToken);

        System.out.println(
                "[TRUSTEE-SEC] OTP Regenerated for Security - Token: " + authToken.getTokenId() + ", OTP: " + newOtp);
        return new AuthInitResponse(authToken.getTokenId(), newOtp);
    }
}
