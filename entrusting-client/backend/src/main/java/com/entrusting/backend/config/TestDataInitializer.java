package com.entrusting.backend.config;

import com.entrusting.backend.user.entity.User;
import com.entrusting.backend.user.repository.UserRepository;
import com.entrusting.backend.util.EncryptionUtils;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Seed test data for development.
 * 테스트용 고객 데이터를 초기화합니다.
 */
@Configuration
public class TestDataInitializer {

    @Bean
    CommandLineRunner initTestData(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder) {
        return args -> {
            // 테스트 고객: 홍길동 (01000000000)
            if (userRepository.findByPhoneNumber(EncryptionUtils.encrypt("01000000000")).isEmpty()) {
                User testCustomer = new User(
                    EncryptionUtils.encrypt("홍길동"),    // name (encrypted)
                    "hong",                               // username
                    passwordEncoder.encode("test1234"),   // password
                    EncryptionUtils.encrypt("01000000000"), // phone (encrypted)
                    true                                  // verified
                );
                
                // 컴플라이언스: 약관 동의 설정
                testCustomer.setTermsAgreed(true);
                testCustomer.setPrivacyAgreed(true);
                testCustomer.setUniqueIdAgreed(true);
                testCustomer.setMarketingAgreed(true);
                testCustomer.setMarketingSms(true);
                testCustomer.setTermsAgreedAt(java.time.LocalDateTime.now());
                
                userRepository.save(testCustomer);
                System.out.println("[TEST-DATA] 테스트 고객 생성: 홍길동 (01000000000)");
            }
        };
    }
}
