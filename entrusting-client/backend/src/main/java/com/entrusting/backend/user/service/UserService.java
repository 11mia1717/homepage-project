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
        if (userRepository.findByPhoneNumber(phone).isPresent()) {
            throw new IllegalArgumentException("Phone number already exists");
        }
        User newUser = new User(request.getName(), request.getUsername(), passwordEncoder.encode(request.getPassword()),
                phone, false);
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
        Optional<User> userOpt = userRepository.findByPhoneNumber(phone);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
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
        return userRepository.findByPhoneNumber(normalizePhone(phoneNumber));
    }

    @Transactional(readOnly = true)
    public String findUsernameByPhoneNumber(String phoneNumber, String name) {
        String phone = normalizePhone(phoneNumber);
        User user = userRepository.findByPhoneNumber(phone)
                .orElseThrow(() -> new IllegalArgumentException("입력하신 정보와 일치하는 회원을 찾을 수 없습니다."));

        if (name != null && !name.equals(user.getName())) {
            throw new IllegalArgumentException("입력하신 정보와 일치하는 회원을 찾을 수 없습니다.");
        }
        return user.getUsername();
    }

    @Transactional
    public void resetPassword(String username, String newPassword, String phoneNumber, String name) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found with username: " + username));

        String phone = normalizePhone(phoneNumber);
        if (!phone.equals(user.getPhoneNumber()) || (name != null && !name.equals(user.getName()))) {
            throw new IllegalArgumentException("본인확인 정보가 계정 정보와 일치하지 않습니다.");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
}