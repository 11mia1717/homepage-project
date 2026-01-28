-- =====================================================
-- 토스뱅크 콜센터 수탁사 시스템 - 데이터베이스 스키마
-- =====================================================

-- -----------------------------------------------------
-- 사용자/상담사 테이블
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '로그인 ID',
    password VARCHAR(255) NOT NULL COMMENT '암호화된 비밀번호',
    name VARCHAR(100) NOT NULL COMMENT '이름',
    email VARCHAR(100) COMMENT '이메일',
    phone VARCHAR(20) COMMENT '연락처',
    role ENUM('ADMIN', 'MANAGER', 'AGENT') NOT NULL DEFAULT 'AGENT' COMMENT '역할',
    department VARCHAR(100) COMMENT '부서',
    enabled BOOLEAN DEFAULT TRUE COMMENT '계정 활성화 여부',
    last_login_at DATETIME COMMENT '마지막 로그인 일시',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_users_username (username),
    INDEX idx_users_role (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------
-- 마케팅 요청 테이블 (고객이 동의한 마케팅 상담 요청)
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS marketing_requests (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    request_id VARCHAR(50) NOT NULL UNIQUE COMMENT '요청 고유 ID (TM20260127001 형태)',
    
    -- 고객 정보 (개인정보)
    customer_name VARCHAR(100) NOT NULL COMMENT '고객명',
    customer_phone VARCHAR(20) NOT NULL COMMENT '고객 연락처',
    customer_ci VARCHAR(255) COMMENT 'CI 연계정보 (암호화 저장)',
    
    -- 동의 정보
    product_name VARCHAR(100) NOT NULL COMMENT '상품명 (예: 루키즈 카드)',
    consent_purpose VARCHAR(500) NOT NULL COMMENT '동의 목적',
    consent_items VARCHAR(500) NOT NULL COMMENT '제공 항목',
    consent_recipient VARCHAR(200) NOT NULL COMMENT '제공받는 자',
    consented_at DATETIME NOT NULL COMMENT '동의 일시',
    
    -- 보유기간 및 파기 관련
    retention_until DATE NOT NULL COMMENT '개인정보 보유 기한',
    
    -- 상태
    status ENUM('PENDING', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED', 'WITHDRAWN') 
        NOT NULL DEFAULT 'PENDING' COMMENT '처리 상태',
    
    -- 동의 철회 관련
    withdrawn_at DATETIME COMMENT '동의 철회 일시',
    withdrawal_reason VARCHAR(500) COMMENT '철회 사유',
    
    -- 파기 관련
    destroyed_yn CHAR(1) DEFAULT 'N' COMMENT '파기 여부',
    destroyed_at DATETIME COMMENT '파기 일시',
    
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_marketing_request_id (request_id),
    INDEX idx_marketing_status (status),
    INDEX idx_marketing_retention (retention_until),
    INDEX idx_marketing_destroyed (destroyed_yn)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------
-- TM 타겟 테이블 (수탁사에 전달되는 상담 대상)
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS tm_targets (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    external_ref VARCHAR(50) NOT NULL COMMENT '외부 참조 ID (request_id, CI 대체)',
    marketing_request_id BIGINT NOT NULL COMMENT '마케팅 요청 ID',
    
    -- 고객 정보 (최소한의 정보만)
    customer_name VARCHAR(100) NOT NULL COMMENT '고객명',
    phone VARCHAR(20) NOT NULL COMMENT '연락처',
    
    -- 상담 정보
    product_name VARCHAR(100) NOT NULL COMMENT '상담 상품',
    consent_purpose VARCHAR(500) NOT NULL COMMENT '동의 목적 (목적 외 사용 제한용)',
    
    -- 보유기간
    retention_until DATE NOT NULL COMMENT '보유 기한',
    
    -- 배정 정보
    assigned_agent_id BIGINT COMMENT '배정된 상담사 ID',
    assigned_at DATETIME COMMENT '배정 일시',
    
    -- 상태
    status ENUM('WAITING', 'ASSIGNED', 'IN_CALL', 'COMPLETED', 'CALLBACK', 'WITHDRAWN') 
        NOT NULL DEFAULT 'WAITING' COMMENT '상태',
    priority INT DEFAULT 0 COMMENT '우선순위',
    
    -- 파기 관련
    destroyed_yn CHAR(1) DEFAULT 'N' COMMENT '파기 여부',
    destroyed_at DATETIME COMMENT '파기 일시',
    
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (marketing_request_id) REFERENCES marketing_requests(id),
    FOREIGN KEY (assigned_agent_id) REFERENCES users(id),
    
    INDEX idx_tm_external_ref (external_ref),
    INDEX idx_tm_status (status),
    INDEX idx_tm_assigned_agent (assigned_agent_id),
    INDEX idx_tm_retention (retention_until),
    INDEX idx_tm_destroyed (destroyed_yn)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------
-- 상담 결과 테이블
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS call_results (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tm_target_id BIGINT NOT NULL COMMENT 'TM 타겟 ID',
    agent_id BIGINT NOT NULL COMMENT '상담사 ID',
    
    -- 통화 정보
    call_started_at DATETIME NOT NULL COMMENT '통화 시작 일시',
    call_ended_at DATETIME COMMENT '통화 종료 일시',
    call_duration INT DEFAULT 0 COMMENT '통화 시간 (초)',
    
    -- 녹취 관련
    recording_agreed_yn CHAR(1) DEFAULT 'N' COMMENT '녹취 사전 동의 여부',
    recorded_yn CHAR(1) DEFAULT 'N' COMMENT '실제 녹음 여부',
    recording_file_url VARCHAR(500) COMMENT '녹취 파일 URL (S3)',
    recording_file_key VARCHAR(255) COMMENT '녹취 파일 S3 Key',
    
    -- 상담 결과
    result_code ENUM('SUCCESS', 'NO_ANSWER', 'BUSY', 'CALLBACK', 'REFUSED', 'WRONG_NUMBER', 'OTHER') 
        NOT NULL COMMENT '상담 결과 코드',
    result_detail VARCHAR(1000) COMMENT '상담 결과 상세',
    
    -- 상품 관련 (목적 외 사용 제한)
    consulted_product VARCHAR(100) NOT NULL COMMENT '상담한 상품명',
    product_result ENUM('AGREED', 'PENDING', 'REFUSED') COMMENT '상품 상담 결과',
    
    -- 재통화 관련
    callback_scheduled_at DATETIME COMMENT '재통화 예정 일시',
    retry_agreed_yn CHAR(1) COMMENT '재통화 동의 여부',
    retry_agreed_at DATETIME COMMENT '재동의 일시',
    
    -- 메모
    memo TEXT COMMENT '상담 메모',
    
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (tm_target_id) REFERENCES tm_targets(id),
    FOREIGN KEY (agent_id) REFERENCES users(id),
    
    INDEX idx_call_tm_target (tm_target_id),
    INDEX idx_call_agent (agent_id),
    INDEX idx_call_result (result_code),
    INDEX idx_call_date (call_started_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------
-- 개인정보 접근 로그 테이블
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS access_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    agent_id BIGINT NOT NULL COMMENT '접근자 ID',
    agent_name VARCHAR(100) NOT NULL COMMENT '접근자 이름',
    
    target_id BIGINT NOT NULL COMMENT '접근 대상 ID',
    target_type ENUM('MARKETING_REQUEST', 'TM_TARGET', 'CALL_RESULT') NOT NULL COMMENT '대상 유형',
    
    access_type ENUM('VIEW', 'CREATE', 'UPDATE', 'DELETE', 'EXPORT') NOT NULL COMMENT '접근 유형',
    access_purpose VARCHAR(200) NOT NULL COMMENT '접근 목적',
    
    ip_address VARCHAR(50) NOT NULL COMMENT '접근 IP',
    user_agent VARCHAR(500) COMMENT 'User Agent',
    
    accessed_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '접근 일시',
    
    INDEX idx_access_agent (agent_id),
    INDEX idx_access_target (target_id, target_type),
    INDEX idx_access_date (accessed_at),
    INDEX idx_access_type (access_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------
-- 고객 알림 이력 테이블
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS customer_notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    marketing_request_id BIGINT NOT NULL COMMENT '마케팅 요청 ID',
    
    notification_type ENUM('CONSENT_COMPLETE', 'CALL_COMPLETE', 'RETENTION_REMINDER', 'DESTROY_COMPLETE') 
        NOT NULL COMMENT '알림 유형',
    channel ENUM('SMS', 'PUSH', 'EMAIL') NOT NULL COMMENT '알림 채널',
    
    recipient_phone VARCHAR(20) COMMENT '수신 연락처',
    recipient_email VARCHAR(100) COMMENT '수신 이메일',
    
    title VARCHAR(200) COMMENT '알림 제목',
    content TEXT NOT NULL COMMENT '알림 내용',
    
    sent_at DATETIME COMMENT '발송 일시',
    status ENUM('PENDING', 'SENT', 'FAILED') DEFAULT 'PENDING' COMMENT '발송 상태',
    error_message VARCHAR(500) COMMENT '실패 시 에러 메시지',
    
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (marketing_request_id) REFERENCES marketing_requests(id),
    
    INDEX idx_notification_request (marketing_request_id),
    INDEX idx_notification_status (status),
    INDEX idx_notification_date (sent_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------
-- 파기 이력 테이블
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS destruction_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    
    target_table VARCHAR(50) NOT NULL COMMENT '파기 대상 테이블',
    target_id BIGINT NOT NULL COMMENT '파기 대상 ID',
    
    destruction_type ENUM('AUTO', 'MANUAL', 'WITHDRAWAL') NOT NULL COMMENT '파기 유형',
    destruction_reason VARCHAR(500) NOT NULL COMMENT '파기 사유',
    
    destroyed_fields VARCHAR(500) NOT NULL COMMENT '파기된 필드 목록',
    
    executed_by VARCHAR(100) COMMENT '처리자 (배치: SYSTEM)',
    executed_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '파기 일시',
    
    INDEX idx_destruction_table (target_table),
    INDEX idx_destruction_date (executed_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------
-- Spring Batch 메타데이터 테이블 (자동 생성되지만 명시적 선언)
-- -----------------------------------------------------
-- Spring Batch가 자동으로 생성하는 테이블들:
-- BATCH_JOB_INSTANCE
-- BATCH_JOB_EXECUTION
-- BATCH_JOB_EXECUTION_PARAMS
-- BATCH_STEP_EXECUTION
-- BATCH_STEP_EXECUTION_CONTEXT
-- BATCH_JOB_EXECUTION_CONTEXT
