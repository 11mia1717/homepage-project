package com.authcompany.backend.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * @class AuthRequest
 * @description 본인인증 요청 데이터를 정의하는 DTO (Data Transfer Object) 클래스입니다.
 * 프론트엔드에서 이름과 휴대폰 번호를 받아 백엔드 API `/api/auth/request`로 전송됩니다.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AuthRequest {
    private String name;
    private String phoneNumber;

    // [보안 취약점] Request DTO에 대한 입력값 검증(Validation) 부족:
    // 실제 시스템에서는 @NotNull, @Size, @Pattern 등 Bean Validation 어노테이션을 사용하여
    // 이름, 휴대폰 번호 등의 입력값에 대한 유효성 검증을 수행해야 합니다.
    // 여기서는 교육용으로 단순화하여 별도의 검증 로직을 포함하지 않습니다.

    @Override
    public String toString() {
        return "AuthRequest{" +
               "name=\'" + name + "\'," +
               " phoneNumber=\'" + phoneNumber + "\'}";
    }
}
