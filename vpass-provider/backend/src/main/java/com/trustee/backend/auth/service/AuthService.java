package com.trustee.backend.auth.service;

import com.trustee.backend.auth.dto.AuthConfirmRequest;
import com.trustee.backend.auth.dto.AuthInitRequest;
import com.trustee.backend.auth.dto.AuthInitResponse;
import com.trustee.backend.auth.dto.AuthStatusResponse;
import com.trustee.backend.auth.dto.AuthVerificationResponse;
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
    private final MockCarrierDatabase mockCarrierDatabase;
    private final com.trustee.backend.auth.util.JwtProvider jwtProvider;
    private final Random random = new Random();

    public AuthService(AuthTokenRepository authTokenRepository, MockCarrierDatabase mockCarrierDatabase,
            com.trustee.backend.auth.util.JwtProvider jwtProvider) {
        this.authTokenRepository = authTokenRepository;
        this.mockCarrierDatabase = mockCarrierDatabase;
        this.jwtProvider = jwtProvider;
    }

    @Transactional
    public AuthInitResponse initAuth(AuthInitRequest request) {
        UUID tokenId = UUID.randomUUID();
        // Generate a 6-digit OTP
        String otp = String.format("%06d", random.nextInt(1000000));

        // [핵심] 가상 통신사 명의 원장 대조 추가
        String cleanPhone = request.getClientData().replaceAll("\\D", "");
        String receivedName = request.getName();
        System.out.println(
                "[TRUSTEE-DEBUG] initAuth - Received Name: [" + receivedName + "], Phone: [" + cleanPhone + "]");
        if (receivedName != null) {
            StringBuilder sb = new StringBuilder();
            for (byte b : receivedName.getBytes(java.nio.charset.StandardCharsets.UTF_8)) {
                sb.append(String.format("%02X ", b));
            }
            System.out.println("[TRUSTEE-DEBUG] initAuth - Received Name Bytes (UTF-8): " + sb.toString());
        }

        // [로직 변경] 여기서 바로 검증하지 않고, 일단 세션만 생성함 (요청 수용)
        // 실제 명의인 대조는 인증번호 확인(confirmAuth) 또는 재발송(requestOtp) 시점에 수행
        System.out.println(
                "[TRUSTEE-DEBUG] initAuth - Session initialized for: " + receivedName + " (" + cleanPhone + ")");

        // In a real scenario, you'd send this OTP via SMS/Kakao to
        // request.getClientData()
        System.out.println("[TRUSTEE] Generated OTP for " + request.getClientData() + ": " + otp);

        // [Compliance] Encrypt PII(Personally Identifiable Information) before saving
        String encryptedPhone = com.trustee.backend.auth.util.EncryptionUtil.encrypt(request.getClientData());
        String encryptedName = com.trustee.backend.auth.util.EncryptionUtil.encrypt(request.getName());

        AuthToken authToken = new AuthToken(tokenId, encryptedPhone, encryptedName, request.getCarrier(),
                otp,
                AuthStatus.PENDING,
                LocalDateTime.now());
        // [New] Save the authRequestId provided by the Entruster
        authToken.setAuthRequestId(request.getAuthRequestId());
        authTokenRepository.save(authToken);
        return new AuthInitResponse(tokenId, otp); // Returning OTP for testing convenience
    }

    @Transactional(readOnly = true)
    public AuthStatusResponse getAuthStatus(UUID tokenId) {
        AuthToken authToken = authTokenRepository.findById(tokenId)
                .orElseThrow(() -> new IllegalArgumentException("AuthToken not found with id: " + tokenId));
        
        // [Fix] 복호화된 이름을 사용하여 응답 (위탁사에서 실명 표시 가능하도록)
        String decryptedName = com.trustee.backend.auth.util.EncryptionUtil.decrypt(authToken.getName());
        
        String accessToken = null;
        if (authToken.getStatus() == AuthStatus.COMPLETED) {
            // [JWT] Generate JWT using jti=tokenId, authRequestId, and name
            // [Compliance] Include CI in JWT
             accessToken = jwtProvider.generateToken(
                authToken.getTokenId(), 
                authToken.getAuthRequestId(), 
                decryptedName,
                authToken.getCi()
            );
        }

        return new AuthStatusResponse(authToken.getTokenId(), authToken.getStatus(), decryptedName, accessToken);
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

        // [추가] 유효 시간 검증 (3분)
        if (java.time.LocalDateTime.now().isAfter(authToken.getCreatedAt().plusMinutes(3))) {
            System.err.println("[TRUSTEE-ERROR] Token Expired: " + requestedTokenId);
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.GONE, "인증 유효 시간이 만료되었습니다. 다시 시작해 주세요.");
        }

        String storedOtp = authToken.getOtp().trim();
        String sentOtp = requestedOtp.trim();

        System.out.println("[TRUSTEE-DEBUG] Comparing OTPs - Stored: [" + storedOtp + "], Received: [" + sentOtp + "]");

        if (!storedOtp.equals(sentOtp)) {
            System.err.println("[TRUSTEE-DEBUG] MISMATH !! Stored: [" + storedOtp + "] != Received: [" + sentOtp + "]");
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST, "인증번호가 일치하지 않습니다. (입력값: " + sentOtp + ")");
        }

        // [Compliance] Decrypt for verification
         String decryptedPhone = com.trustee.backend.auth.util.EncryptionUtil.decrypt(authToken.getClientData());
         String decryptedName = com.trustee.backend.auth.util.EncryptionUtil.decrypt(authToken.getName());

        // [핵심] 인증번호가 맞더라도, 실제 통신사 정보와 일치하는지 최종 단계에서 검증
        String cleanPhone = decryptedPhone.replaceAll("\\D", "");
        if (!mockCarrierDatabase.verifyIdentity(cleanPhone, decryptedName, authToken.getCarrier())) {
            System.err
                    .println("[TRUSTEE-ERROR] Identity Disclosure Mismatch at FINAL step for: " + decryptedName
                            + " (" + authToken.getCarrier() + ")");
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.FORBIDDEN, "정보 불일치: 입력하신 정보와 통신사 명의 정보가 일치하지 않습니다.");
        }

        // [Compliance] Generate CI
        String ci = mockCarrierDatabase.generateCI(cleanPhone);
        authToken.setCi(ci);

        authToken.setStatus(AuthStatus.COMPLETED);
        authTokenRepository.save(authToken);
        System.out.println("[TRUSTEE-DEBUG] SUCCESS !! Verification Completed for Token: " + requestedTokenId);
    }

    @Transactional
    public AuthInitResponse requestOtp(com.trustee.backend.auth.dto.AuthOtpRequest request) {
        AuthToken authToken = authTokenRepository.findById(request.getTokenId())
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 인증 세션입니다. (Token NOT FOUND)"));

        // [추가] 유효 시간 검증 (3분 - 재발송 가능한 시간 제한)
        if (java.time.LocalDateTime.now().isAfter(authToken.getCreatedAt().plusMinutes(3))) {
            throw new IllegalArgumentException("인증 유효 시간이 만료되었습니다. 처음부터 다시 진행해 주세요.");
        }

        // [핵심] 위탁사에서 등록한 정보와 현재 입력한 정보가 일치하는지 검증
        // [Compliance] Decrypt for verification
        String decryptedPhone = com.trustee.backend.auth.util.EncryptionUtil.decrypt(authToken.getClientData());
        String decryptedName = com.trustee.backend.auth.util.EncryptionUtil.decrypt(authToken.getName());

        String expectedPhone = decryptedPhone.replaceAll("\\D", "");
        String inputPhone = request.getPhoneNumber().replaceAll("\\D", "");
        String expectedName = decryptedName;
        String inputName = request.getName();

        System.out.println("[TRUSTEE-SEC] Validating Identity - Expected: [" + expectedName + ", " + expectedPhone
                + "], Input: [" + inputName + ", " + inputPhone + "]");

        if (!expectedPhone.equals(inputPhone) || (expectedName != null && !expectedName.equals(inputName))) {
            throw new IllegalArgumentException("정보 불일치: 본인인증 정보가 올바르지 않습니다.");
        }

        // [핵심] 재전송 시에도 통신사 실명 대조
        if (!mockCarrierDatabase.verifyIdentity(inputPhone, inputName, request.getCarrier())) {
            throw new IllegalArgumentException("정보 불일치: 통신사 명의 정보와 일치하지 않습니다.");
        }

        // [추가] 선택한 통신사 정보를 세션에 업데이트 (최종 검증 시 사용됨)
        authToken.setCarrier(request.getCarrier());

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

    @Transactional(readOnly = true)
    public AuthVerificationResponse verifyToken(UUID tokenId) {
        AuthToken authToken = authTokenRepository.findById(tokenId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid token ID"));

        // [Fix] S2S 검증 시 복호화된 평문 반환 (위탁사에서 이름 비교 가능하도록)
        String decryptedName = com.trustee.backend.auth.util.EncryptionUtil.decrypt(authToken.getName());
        String decryptedPhone = com.trustee.backend.auth.util.EncryptionUtil.decrypt(authToken.getClientData());

        return new AuthVerificationResponse(
                authToken.getStatus(),
                decryptedName,
                decryptedPhone);
    }
}
