package com.authcompany.backend.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * @class CallbackRequest
 * @description 백엔드가 위탁사(Client Company)로 인증 결과를 콜백 형태로 전달할 때 사용되는 DTO 클래스입니다.
 * 인증 성공 여부, 메시지, 그리고 원본 요청의 트랜잭션 ID 또는 관련 정보를 포함할 수 있습니다.
 * (여기서는 교육용으로 단순화하여 본인인증 성공 여부와 메시지만 포함합니다.)
 * 의도된 보안 취약점: 콜백 API 인증 없음.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CallbackRequest {
    private boolean success;
    private String message;
    private String transactionId; // [보안 취약점] 실제 환경에서는 트랜잭션 ID 등의 중요 정보는 서명(Signature) 필요

    // [보안 취약점] 콜백 요청에 대한 인증/인가 메커니즘 부재:
    // 실제 금융권 콜백 API는 상호 인증 (예: SSL Client Certificate), IP 화이트리스트,
    // 메시지 서명(HMAC-SHA256) 등 강력한 보안 메커니즘이 필수적입니다.
    // 여기서는 교육용으로 별도의 인증 없이 요청을 수락합니다.

    @Override
    public String toString() {
        return "CallbackRequest{" +
               "success=" + success +
               ", message=\'" + message + "\'" +
               ", transactionId=\'" + transactionId + "\'}";
    }
}
