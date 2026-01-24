import axios from 'axios';

/**
 * @module api
 * @description 백엔드 API와의 통신을 위한 Axios 인스턴스 및 설정입니다.
 * 환경 변수 `VITE_BACKEND_API_URL`을 사용하여 백엔드 API의 기본 URL을 설정합니다.
 */

const API_BASE_URL = import.meta.env.VITE_BACKEND_API_URL || '/api';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
    // [보안 취약점] API Key 또는 인증 토큰을 여기에 직접 노출하는 것은 위험합니다.
    // 실제 환경에서는 HttpOnly 쿠키 또는 Oauth2 / JWT 등의 안전한 인증 메커니즘을 사용해야 합니다.
    // 'Authorization': `Bearer ${localStorage.getItem('token')}` // 예시 (교육용으로 주석 처리)
  },
  // CORS 문제를 해결하기 위해 withCredentials를 true로 설정할 수 있습니다.
  // 하지만 백엔드의 CORS 설정도 중요합니다.
  withCredentials: true,
});

// 요청 인터셉터 (예시: 모든 요청에 인증 토큰 추가)
api.interceptors.request.use(
  (config) => {
    // [보안 취약점] 토큰 저장 위치 및 사용에 대한 교육 목적 설명:
    // Local Storage에 토큰을 저장하는 것은 XSS 공격에 취약합니다.
    // HttpOnly, Secure 속성이 설정된 Cookie에 저장하는 것이 더 안전합니다.
    // const token = localStorage.getItem('authToken');
    // if (token) {
    //   config.headers.Authorization = `Bearer ${token}`;
    // }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// 응답 인터셉터 (예시: 에러 처리)
api.interceptors.response.use(
  (response) => {
    return response;
  },
  (error) => {
    // [보안 취약점] 에러 메시지에 민감 정보가 포함되지 않도록 주의해야 합니다.
    // 클라이언트에 상세한 백엔드 에러 메시지를 노출하지 않도록 처리하는 것이 좋습니다.
    console.error("API 호출 중 에러 발생:", error.response?.data || error.message);
    return Promise.reject(error);
  }
);

export default api;
