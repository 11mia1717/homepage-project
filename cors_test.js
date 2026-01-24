// 브라우저 개발자 도구 (F12) 콘솔에 붙여넣어 실행하세요.
// 이 코드는 Ubuntu 서버 (예: http://192.168.59.138)로 접속된 브라우저에서 실행되어야 합니다.

const backendApiUrl = window.location.origin + '/api/health'; // Nginx 프록시를 통해 백엔드로 요청

console.log(`CORS 테스트 시작: ${backendApiUrl}`);

fetch(backendApiUrl, {
    method: 'GET',
    headers: {
        'Content-Type': 'application/json',
        // 필요한 경우 인증 토큰 등을 여기에 추가
    },
    // credentials: 'include' // 쿠키나 인증 헤더를 보낼 경우 주석 해제
})
.then(response => {
    if (!response.ok) {
        // HTTP 상태 코드가 2xx 범위가 아닌 경우 에러 처리
        // CORS 문제의 경우 네트워크 탭에서 실제 에러를 확인해야 합니다.
        throw new Error(`HTTP error! status: ${response.status}`);
    }
    return response.text(); // 또는 response.json() if API returns JSON
})
.then(data => {
    console.log('CORS 테스트 성공! 백엔드로부터의 응답:', data);
    console.log('네트워크 탭에서 HTTP 상태 코드 200 (OK) 및 응답을 확인하세요.');
})
.catch(error => {
    console.error('CORS 테스트 실패 또는 네트워크 오류:', error);
    console.error('네트워크 탭 (F12)에서 CORS 관련 오류 메시지 (예: CORS policy, blocked by client) 또는 기타 네트워크 오류를 확인하세요.');
    console.error('Windows Server (192.168.59.1:8080)가 실행 중인지, Windows 방화벽이 8080 포트를 열었는지 확인하세요.');
});
