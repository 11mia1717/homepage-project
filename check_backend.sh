#!/bin/bash

# Windows Server의 IP 주소와 Spring Boot API 포트
WINDOWS_SERVER_IP="192.168.59.1"
SPRING_BOOT_PORT="8080"
API_ENDPOINT="/api/health"

# API URL 조합
API_URL="http://${WINDOWS_SERVER_IP}:${SPRING_BOOT_PORT}${API_ENDPOINT}"

echo "Windows Server API (${API_URL}) 응답 확인 중..."

# curl 명령어를 사용하여 API 호출
# -s: silent mode (진행률 표시줄 및 오류 메시지 숨김)
# -o /dev/null: 출력을 /dev/null로 리다이렉션 (화면에 출력하지 않음)
# -w %{http_code}: HTTP 응답 코드 출력
# --connect-timeout 5: 연결 시도 시간 제한을 5초로 설정
# --max-time 10: 전체 작업 시간 제한을 10초로 설정
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" --connect-timeout 5 --max-time 10 "${API_URL}")

if [ "$HTTP_CODE" -eq 200 ]; then
    echo "성공: Windows Server API가 정상적으로 응답합니다. (HTTP 코드: ${HTTP_CODE})"
else
    echo "실패: Windows Server API 응답에 문제가 있습니다. (HTTP 코드: ${HTTP_CODE})"
    echo "API URL: ${API_URL}"
    echo "다음 사항을 확인하세요:"
    echo "  1. Windows Server의 IP 주소(${WINDOWS_SERVER_IP})가 올바른지 확인."
    echo "  2. Windows Server에서 Spring Boot 애플리케이션이 ${SPRING_BOOT_PORT} 포트에서 실행 중인지 확인."
    echo "  3. Windows Server 방화벽에서 ${SPRING_BOOT_PORT} 포트가 개방되어 있는지 확인."
    echo "  4. Ubuntu 서버에서 Windows Server로 네트워크 연결(ping ${WINDOWS_SERVER_IP})이 가능한지 확인."
fi

# 추가적으로 API 응답 본문을 보고 싶다면, -s 대신 -S (show error) 및 -v (verbose) 옵션을 사용하거나
# 응답 본문을 파일로 저장 후 확인할 수 있습니다.
# curl -s "${API_URL}"
