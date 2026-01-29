package com.entrusting.backend.user.service;

import com.entrusting.backend.user.dto.LoginRequest;
import com.entrusting.backend.user.dto.RegisterRequest;
import com.entrusting.backend.user.dto.TermsAgreementDto;
import com.entrusting.backend.user.entity.User;
import com.entrusting.backend.user.repository.UserRepository;
import com.entrusting.backend.util.EncryptionUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    private String normalizePhone(String phone) {
        if (phone == null)
            return null;
        return phone.replaceAll("\\D", "");
    }

    @Transactional
    public User registerUser(RegisterRequest request) {
        System.out.println("[ENTRUSTING-DEBUG] registerUser called for: " + request.getUsername());
        // [COMPLIANCE] 약관 동의 검증
        // [COMPLIANCE] 약관 동의 검증 제거 (사용자 요청)
        // if (request.getTermsAgreement() == null || !request.getTermsAgreement().isAllRequiredAgreed()) {
        //     throw new IllegalArgumentException("필수 약관에 모두 동의해야 합니다.");
        // }
        
        String phone = normalizePhone(request.getPhoneNumber());
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (userRepository.findByPhoneNumber(phone).size() > 0) {
            throw new IllegalArgumentException("Phone number already exists");
        }
        
        // [COMPLIANCE] 개인정보 암호화 저장
        String encryptedName = EncryptionUtils.encrypt(request.getName());
        String encryptedPhone = EncryptionUtils.encrypt(phone);
        
        User newUser = new User(
            encryptedName, 
            request.getUsername(), 
            passwordEncoder.encode(request.getPassword()),
            encryptedPhone, 
            request.isVerified()
        );
        
        // [COMPLIANCE] 약관 동의 정보 설정
        TermsAgreementDto terms = request.getTermsAgreement();
        if (terms != null && terms.getAgreements() != null) {
            newUser.setTermsAgreed(terms.getAgreements().getOrDefault("terms", false));
            newUser.setPrivacyAgreed(terms.getAgreements().getOrDefault("privacy", false));
            newUser.setUniqueIdAgreed(terms.getAgreements().getOrDefault("uniqueId", false));
            newUser.setCreditInfoAgreed(terms.getAgreements().getOrDefault("creditInfo", false));
            newUser.setCarrierAuthAgreed(terms.getAgreements().getOrDefault("carrierAuth", false));
            
            // [COMPLIANCE] 금융권 추가 필수 약관 매핑
            newUser.setSsapProvisionAgreed(terms.getAgreements().getOrDefault("ssapProvision", false));
            newUser.setElectronicFinanceAgreed(terms.getAgreements().getOrDefault("electronicFinance", false));
            newUser.setMonitoringAgreed(terms.getAgreements().getOrDefault("monitoring", false));
            
            // [COMPLIANCE] 선택 약관 매핑
            newUser.setMarketingPersonalAgreed(terms.getAgreements().getOrDefault("marketingPersonal", false));
            newUser.setMarketingAgreed(terms.getAgreements().getOrDefault("marketing", false));
            
            // 마케팅 채널 설정
            if (terms.getMarketingChannels() != null) {
                newUser.setMarketingSms(terms.getMarketingChannels().getOrDefault("sms", false));
            }
            
            newUser.setTermsAgreedAt(java.time.LocalDateTime.now());
        }
        
        // CI/DI는 S2S 인증 완료 시 설정됨
        
        return userRepository.save(newUser);
    }

    @Transactional(readOnly = true)
    public User loginUser(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Invalid username or password"));
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid username or password");
        }
        return user;
    }

    @Transactional
    public void updateUserVerifiedStatus(String phoneNumber, boolean isVerified) {
        String phone = normalizePhone(phoneNumber);
        String encryptedPhone = EncryptionUtils.encrypt(phone);
        java.util.List<User> users = userRepository.findByPhoneNumber(encryptedPhone);
        if (!users.isEmpty()) {
            User user = users.get(0); // 가장 최근 또는 첫 번째 사용자 업데이트
            user.setVerified(isVerified);
            userRepository.save(user);
            System.out.println(
                    "[ENTRUSTING-DEBUG] Profile Updated - Number: " + phone + ", Verified: " + isVerified);
        } else {
            // 회원가입 중일 수 있으므로 에러를 던지지 않고 로그만 남김
            System.out.println("[ENTRUSTING-DEBUG] Auth Callback received for NON-REGISTERED Number: " + phone
                    + ". This is expected during registration flow.");
        }
    }

    @Transactional(readOnly = true)
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Transactional(readOnly = true)
    public Optional<User> findByPhoneNumber(String phoneNumber) {
        String encryptedPhone = EncryptionUtils.encrypt(normalizePhone(phoneNumber));
        java.util.List<User> users = userRepository.findByPhoneNumber(encryptedPhone);
        return users.isEmpty() ? Optional.empty() : Optional.of(users.get(0));
    }

    @Transactional(readOnly = true)
    public String findUsernameByPhoneNumber(String phoneNumber, String name) {
        String phone = normalizePhone(phoneNumber);
        String encryptedPhone = EncryptionUtils.encrypt(phone);
        
        System.out.println("[ENTRUSTING-DEBUG] Searching for User - Normalized: " + phone + ", Encrypted: " + encryptedPhone);
        
        java.util.List<User> users = userRepository.findByPhoneNumber(encryptedPhone);
        if (users.isEmpty()) {
            System.err.println("[ENTRUSTING-DEBUG] User NOT FOUND for encrypted phone: " + encryptedPhone);
            throw new IllegalArgumentException("입력하신 정보와 일치하는 회원을 찾을 수 없습니다.");
        }

        String encryptedName = EncryptionUtils.encrypt(name);
        
        // 이름까지 일치하는 사용자 찾기
        User user = users.stream()
                .filter(u -> encryptedName.equals(u.getName()))
                .findFirst()
                .orElseThrow(() -> {
                    System.err.println("[ENTRUSTING-DEBUG] Name mismatch for phone: " + phone);
                    return new IllegalArgumentException("입력하신 정보와 일치하는 회원을 찾을 수 없습니다.");
                });

        System.out.println("[ENTRUSTING-DEBUG] Found User: " + user.getUsername());
        return user.getUsername();
    }

    @Transactional
    public void resetPassword(String username, String newPassword, String phoneNumber, String name) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found with username: " + username));

        String phone = normalizePhone(phoneNumber);
        String encryptedPhone = EncryptionUtils.encrypt(phone);
        String encryptedName = EncryptionUtils.encrypt(name);

        if (!encryptedPhone.equals(user.getPhoneNumber()) || (name != null && !encryptedName.equals(user.getName()))) {
            throw new IllegalArgumentException("본인확인 정보가 계정 정보와 일치하지 않습니다.");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
}