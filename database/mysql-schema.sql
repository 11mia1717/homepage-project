-- ============================================================
-- Continue Bank - MySQL 스키마 정의
-- Docker 마이그레이션용 DDL
-- ============================================================

-- 데이터베이스 생성
CREATE DATABASE IF NOT EXISTS entrusting_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS trustee_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- ============================================================
-- 1. entrusting_db (위탁사 - Continue Bank)
-- ============================================================
USE entrusting_db;

-- 사용자 테이블
CREATE TABLE IF NOT EXISTS site_users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    
    -- 기본 정보 (AES-256 암호화 저장)
    name VARCHAR(500),                      -- 이름 (암호화)
    username VARCHAR(255),                  -- 로그인 ID
    password VARCHAR(255),                  -- 비밀번호 (BCrypt 해시)
    phone_number VARCHAR(500),              -- 휴대폰 번호 (암호화)
    
    -- 본인인증 식별자 (금융권 표준)
    ci VARCHAR(120) UNIQUE,                 -- 연계정보 (CI) - 모든 금융기관 공통
    di VARCHAR(90),                         -- 중복가입확인정보 (DI) - 사이트별 고유
    is_verified BOOLEAN DEFAULT FALSE,      -- 본인인증 완료 여부
    
    -- 개인정보 수집/이용 동의 정보
    privacy_agreed_at DATETIME,             -- 개인정보 동의 일시
    data_expire_at DATETIME,                -- 데이터 보관 만료일 (금융거래 5년)
    
    -- 약관 동의 정보 (필수 9종 + 선택 2종)
    terms_agreed BOOLEAN DEFAULT FALSE,             -- 이용약관
    privacy_agreed BOOLEAN DEFAULT FALSE,           -- 개인정보 수집·이용
    unique_id_agreed BOOLEAN DEFAULT FALSE,         -- 고유식별정보 처리
    credit_info_agreed BOOLEAN DEFAULT FALSE,       -- 신용정보 조회·제공
    carrier_auth_agreed BOOLEAN DEFAULT FALSE,      -- 본인확인서비스 (SSAP)
    ssap_provision_agreed BOOLEAN DEFAULT FALSE,    -- SSAP 정보 제공 동의
    electronic_finance_agreed BOOLEAN DEFAULT FALSE, -- 전자금융거래 기본약관
    monitoring_agreed BOOLEAN DEFAULT FALSE,        -- 금융거래 모니터링/AML
    marketing_personal_agreed BOOLEAN DEFAULT FALSE, -- 개인맞춤형 상품 추천 (선택)
    
    -- 마케팅 동의 상세
    marketing_agreed BOOLEAN DEFAULT FALSE,         -- 마케팅 정보 수신 동의
    marketing_sms BOOLEAN DEFAULT FALSE,            -- SMS 마케팅 동의
    marketing_email BOOLEAN DEFAULT FALSE,          -- 이메일 마케팅 동의
    marketing_push BOOLEAN DEFAULT FALSE,           -- 푸시 알림 마케팅 동의
    
    terms_agreed_at DATETIME,               -- 약관 동의 일시
    
    -- 인덱스
    INDEX idx_username (username),
    INDEX idx_phone_number (phone_number(100)),
    INDEX idx_ci (ci)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 2. trustee_db (수탁사 - SSAP 본인인증)
-- ============================================================
USE trustee_db;

-- 본인인증 토큰 테이블
CREATE TABLE IF NOT EXISTS auth_token (
    token_id BINARY(16) PRIMARY KEY,        -- UUID (jti)
    auth_request_id VARCHAR(255),           -- 위탁사 요청 ID
    
    -- 인증 정보 (AES-256 암호화 저장)
    client_data VARCHAR(500),               -- 전화번호 (암호화)
    name VARCHAR(500),                      -- 이름 (암호화)
    carrier VARCHAR(50),                    -- 통신사
    otp VARCHAR(100),                       -- OTP (해시)
    
    -- 본인인증 완료 시 생성
    ci VARCHAR(120),                        -- 연계정보
    di VARCHAR(90),                         -- 중복가입확인정보
    
    -- 토큰 상태
    status ENUM('PENDING', 'OTP_SENT', 'CONFIRMED', 'USED', 'EXPIRED') DEFAULT 'PENDING',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    
    -- 인덱스
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 가상 통신사 사용자 (Mock 데이터)
CREATE TABLE IF NOT EXISTS carrier_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,             -- 이름
    phone_number VARCHAR(20) NOT NULL UNIQUE, -- 휴대폰 번호
    carrier VARCHAR(50) NOT NULL,           -- 통신사 (SKT, KT, LGU+)
    
    INDEX idx_phone_number (phone_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 3. 초기 데이터 (테스트용 가상 통신사 사용자)
-- ============================================================
USE trustee_db;

INSERT INTO carrier_user (name, phone_number, carrier) VALUES
('홍길동', '01012345678', 'SKT'),
('김철수', '01087654321', 'KT'),
('이영희', '01011112222', 'LGU+'),
('홍길순', '01033334444', 'SKT'),
('고길동', '01055556666', 'KT'),
('차은우', '01077778888', 'LGU+')
ON DUPLICATE KEY UPDATE name=VALUES(name), carrier=VALUES(carrier);

-- ============================================================
-- 4. 사용자 권한 설정 (프로덕션용)
-- ============================================================
-- CREATE USER IF NOT EXISTS 'continue'@'%' IDENTIFIED BY 'continue12!';
-- GRANT ALL PRIVILEGES ON entrusting_db.* TO 'continue'@'%';
-- GRANT ALL PRIVILEGES ON trustee_db.* TO 'continue'@'%';
-- FLUSH PRIVILEGES;
