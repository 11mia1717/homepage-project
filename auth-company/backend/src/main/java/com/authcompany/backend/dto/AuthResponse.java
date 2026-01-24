package com.authcompany.backend.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * @class AuthResponse
 * @description 본인인증 요청에 대한 응답 데이터를 정의하는 DTO 클래스입니다.
 * 인증 성공/실패 여부, 메시지, 그리고 교육용 Mock 인증 토큰을 포함합니다.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private boolean success;
    private String message;
    private String authToken; // [보안 취약점] 교육용 Mock 토큰. 실제 토큰은 민감 정보이므로 반환 시 주의.

    @Override
    public String toString() {
        return "AuthResponse{" +
               "success=" + success +
               ", message=\'" + message + "\'" +
               ", authToken=\'" + authToken + "\'}";
    }
}
