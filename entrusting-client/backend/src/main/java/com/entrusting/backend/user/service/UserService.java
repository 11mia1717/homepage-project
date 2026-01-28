package com.entrusting.backend.user.service;

import com.entrusting.backend.user.dto.LoginRequest;
import com.entrusting.backend.user.dto.RegisterRequest;
import com.entrusting.backend.user.entity.User;
import com.entrusting.backend.user.repository.UserRepository;
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
        String phone = normalizePhone(request.getPhoneNumber());
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username already exists");
        }
        // [Compliance] CI based Duplicate Check
        if (request.getCi() != null && userRepository.findByCi(request.getCi()).isPresent()) {
             throw new IllegalArgumentException("이미 가입된 회원입니다. (CI 중복)");
        }

        // [Compliance] Encrypt PII
        String encryptedPhone = com.entrusting.backend.common.util.EncryptionUtil.encrypt(phone);
        String encryptedName = com.entrusting.backend.common.util.EncryptionUtil.encrypt(request.getName());

        User newUser = new User(encryptedName, request.getUsername(), passwordEncoder.encode(request.getPassword()),
                encryptedPhone, request.getCi(), request.getDi(), request.isVerified());
        return userRepository.save(newUser);
    }

    @Transactional(readOnly = true)
    public User loginUser(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Invalid username or password"));
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid username or password");
        }
        
        // [Compliance] Decrypt PII before returning to controller/frontend
        user.setName(com.entrusting.backend.common.util.EncryptionUtil.decrypt(user.getName()));
        user.setPhoneNumber(com.entrusting.backend.common.util.EncryptionUtil.decrypt(user.getPhoneNumber()));
        
        return user;
    }

    @Transactional
    public void updateUserVerifiedStatus(String phoneNumber, boolean isVerified) {
        String phoneInput = normalizePhone(phoneNumber);
        // Find user by decrypting phone numbers (Demo strategy)
        Optional<User> userOpt = userRepository.findAll().stream()
                .filter(u -> phoneInput.equals(normalizePhone(com.entrusting.backend.common.util.EncryptionUtil.decrypt(u.getPhoneNumber()))))
                .findFirst();

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setVerified(isVerified);
            userRepository.save(user);
            System.out.println(
                    "[ENTRUSTING-DEBUG] Profile Updated - Number: " + phoneInput + ", Verified: " + isVerified);
        } else {
            // 회원가입 중일 수 있으므로 에러를 던지지 않고 로그만 남김
            System.out.println("[ENTRUSTING-DEBUG] Auth Callback received for NON-REGISTERED Number: " + phoneInput
                    + ". This is expected during registration flow.");
        }
    }

    @Transactional(readOnly = true)
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Transactional(readOnly = true)
    public Optional<User> findByPhoneNumber(String phoneNumber) {
        // [Compliance] Encrypt to search
        String encrypted = com.entrusting.backend.common.util.EncryptionUtil.encrypt(normalizePhone(phoneNumber));
        Optional<User> userOpt = userRepository.findByPhoneNumber(encrypted);
        
        // [Compliance] Decrypt if found
        userOpt.ifPresent(user -> {
            user.setName(com.entrusting.backend.common.util.EncryptionUtil.decrypt(user.getName()));
            user.setPhoneNumber(com.entrusting.backend.common.util.EncryptionUtil.decrypt(user.getPhoneNumber()));
        });
        
        return userOpt;
    }

    @Transactional(readOnly = true)
    public String findUsernameByPhoneNumber(String phoneNumber, String name) {
        String phoneInput = normalizePhone(phoneNumber);
        // Search by CI or find all and check (Demonstration logic)
        User user = userRepository.findAll().stream()
                .filter(u -> {
                    String decPhone = com.entrusting.backend.common.util.EncryptionUtil.decrypt(u.getPhoneNumber());
                    String decName = com.entrusting.backend.common.util.EncryptionUtil.decrypt(u.getName());
                    return phoneInput.equals(normalizePhone(decPhone)) && name.equals(decName);
                })
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("입력하신 정보와 일치하는 회원을 찾을 수 없습니다."));

        return user.getUsername();
    }

    @Transactional
    public void resetPassword(String username, String newPassword, String phoneNumber, String name) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found with username: " + username));

        String inputPhone = normalizePhone(phoneNumber);
        String dbPhone = com.entrusting.backend.common.util.EncryptionUtil.decrypt(user.getPhoneNumber());
        String dbName = com.entrusting.backend.common.util.EncryptionUtil.decrypt(user.getName());

        if (!inputPhone.equals(normalizePhone(dbPhone)) || (name != null && !name.equals(dbName))) {
            throw new IllegalArgumentException("본인확인 정보가 계정 정보와 일치하지 않습니다.");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
}