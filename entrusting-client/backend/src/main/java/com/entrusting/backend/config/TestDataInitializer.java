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
                
                // Additional Requested Test Users
                String[][] testUsers = {
                    {"홍길순", "hong2", "01000000001"},
                    {"고길동", "go", "01000000002"},
                    {"차은우", "cha", "01010041004"},
                    {"일영이", "one", "01011111111"},
                    {"이영이", "two", "01022222222"},
                    {"삼영이", "three", "01033333333"},
                    {"사영이", "four", "01044444444"},
                    {"오영이", "five", "01055555555"},
                    {"육영이", "six", "01066666666"},
                    {"칠영이", "seven", "01077777777"},
                    {"팔영이", "eight", "01088888888"},
                    {"구영이", "nine", "01099999999"},
                    {"십영이", "ten", "01010101010"}
                };

                for (String[] u : testUsers) {
                    if (userRepository.findByPhoneNumber(EncryptionUtils.encrypt(u[2])).isEmpty()) {
                        User user = new User(
                            EncryptionUtils.encrypt(u[0]),
                            u[1],
                            passwordEncoder.encode("test1234"),
                            EncryptionUtils.encrypt(u[2]),
                            true
                        );
                        user.setTermsAgreed(true);
                        user.setPrivacyAgreed(true);
                        user.setUniqueIdAgreed(true);
                        user.setMarketingAgreed(true);
                        user.setMarketingSms(true);
                        user.setTermsAgreedAt(java.time.LocalDateTime.now());
                        userRepository.save(user);
                    }
                }

                System.out.println("[TEST-DATA] 테스트 고객 " + testUsers.length + "명 추가 완료");
            }
        };
    }
}
