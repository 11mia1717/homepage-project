package com.authcompany.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.client.RestTemplate; // RestTemplate 임포트 추가
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @class SecurityConfig
 * @description Spring Security 설정을 정의하는 클래스입니다.
 * CORS(Cross-Origin Resource Sharing) 설정과 HTTP 요청에 대한 보안 규칙을 구성합니다.
 * 의도된 보안 취약점: CORS 허용 범위, CSRF 비활성화 (교육 목적).
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // [보안 취약점] CSRF 보호 비활성화:
            // 실제 운영 환경에서는 CSRF(Cross-Site Request Forgery) 공격 방어를 위해 CSRF 보호를 활성화하고,
            // 적절한 토큰 기반의 보호 메커니즘을 구현해야 합니다.
            // 여기서는 교육용 및 React/REST API 연동 편의를 위해 비활성화합니다.
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(authorize -> authorize
                // 본인인증 요청 및 콜백 엔드포인트를 허용
                .requestMatchers("/api/auth/request", "/api/auth/callback", "/api/health").permitAll()
                .anyRequest().authenticated() // 그 외 모든 요청은 인증 필요
            );
        return http.build();
    }

    /**
     * @method corsConfigurer
     * @description CORS(Cross-Origin Resource Sharing) 설정을 구성합니다.
     * 프론트엔드 애플리케이션이 백엔드 API에 접근할 수 있도록 허용합니다.
     * [보안 취약점] 넓은 범위의 CORS 허용:
     * `allowedOrigins`를 `*` 또는 너무 넓은 범위로 설정하면 CSRF, XSS 공격에 취약해질 수 있습니다.
     * 실제 운영 환경에서는 명확하게 신뢰할 수 있는 도메인만 허용해야 합니다.
     * 여기서는 개발 및 교육의 편의를 위해 `allowedOrigins`를 조금 더 유연하게 설정하거나,
     * 특정 주소를 명시하고, 주석으로 보안 주의사항을 명시합니다.
     * @return WebMvcConfigurer
     */
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**") // 모든 경로에 대해 CORS 적용
                        // [보안 취약점] 허용 오리진을 너무 넓게 설정:
                        // 실제 환경에서는 React 프론트엔드의 도메인 또는 IP를 명시해야 합니다.
                        // 예: .allowedOrigins("http://localhost:3000", "https://your-frontend-domain.com")
                        // k3s 환경에서는 Ingress 또는 Service NodePort를 통해 노출되는 프론트엔드 주소가 됩니다.
                        .allowedOrigins("http://localhost:3000", "http://192.168.59.138") // 개발 및 k3s 테스트용 (추후 실제 환경에 맞게 수정 필요)
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // 허용할 HTTP 메서드
                        .allowedHeaders("*") // 모든 헤더 허용 (필요에 따라 최소한으로 제한)
                        .allowCredentials(true); // 자격 증명 (쿠키, HTTP 인증 등) 허용
            }
        };
    }

    /**
     * @method restTemplate
     * @description 외부 API 호출을 위한 RestTemplate 빈을 등록합니다.
     * AuthService에서 위탁사 콜백을 전송할 때 사용됩니다.
     * @return RestTemplate
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    // [보안 취약점] 이 구조에서 발생 가능한 보안 취약점 목록 (Spring Security/CORS 관련) 주석 정리:
    // 1. CSRF 보호 비활성화: 실제 운영 환경에서는 CSRF 보호를 활성화하고 적절한 토큰 기반 메커니즘 구현 필수.
    // 2. 넓은 범위의 CORS 허용: `allowedOrigins`를 너무 넓게 설정하면 XSS, CSRF 공격에 취약. 신뢰할 수 있는 도메인만 허용해야 함.
    // 3. HTTP Only, Secure 플래그 없는 쿠키 사용: 세션 쿠키 또는 인증 쿠키 사용 시 HttpOnly 및 Secure 플래그 설정 필수.
    // 4. 보안 헤더 미적용: HSTS, X-Content-Type-Options, X-Frame-Options 등 보안 헤더 설정 필요.
    // 5. 민감 정보 URL 노출: URL 경로에 민감 정보가 포함되지 않도록 주의.
}
