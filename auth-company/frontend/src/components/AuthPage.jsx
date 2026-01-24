import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../api';

/**
 * @module AuthPage
 * @description 본인인증을 요청하는 페이지 컴포넌트입니다.
 * 사용자로부터 이름과 휴대폰 번호를 입력받아 백엔드에 인증 요청을 보냅니다.
 * 의도된 보안 취약점: 클라이언트 측 입력값 검증 미흡, 토큰 저장 방식.
 */
function AuthPage() {
  const [name, setName] = useState('');
  const [phoneNumber, setPhoneNumber] = useState('');
  const [error, setError] = useState('');
  const navigate = useNavigate();

  /**
   * @function handleSubmit
   * @description 본인인증 요청 버튼 클릭 시 호출됩니다.
   * 백엔드 API `/api/auth/request`로 인증 요청을 보냅니다.
   * @param {Event} e - 폼 제출 이벤트 객체
   */
  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');

    // [보안 취약점] 클라이언트 측 입력값 검증 미흡:
    // 실제 금융 서비스에서는 이름, 휴대폰 번호 형식에 대한 엄격한 검증 로직이 필요합니다.
    // 여기서는 최소한의 입력 여부만 확인합니다.
    if (!name || !phoneNumber) {
      setError('이름과 휴대폰 번호를 모두 입력해주세요.');
      return;
    }

    try {
      console.log('인증 요청 중:', { name, phoneNumber });
      const response = await api.post('/api/auth/request', {
        name,
        phoneNumber,
        // [보안 취약점] 실제 금융권에서는 요청 데이터 암호화 및 무결성 검증 필수
        // 교육용으로 단순화하여 평문 전송
      });

      // [보안 취약점] 서버에서 발급한 토큰을 클라이언트 저장 시 보안 고려 사항:
      // 교육용 예시에서는 단순히 console.log로 표시하지만,
      // 실제 애플리케이션에서는 Local Storage, Session Storage 사용 지양.
      // HttpOnly, Secure 속성이 설정된 Cookie 사용을 강력히 권장.
      console.log('인증 요청 성공:', response.data);

      // 인증 성공 시 결과 페이지로 리다이렉트. 실제로는 백엔드에서 콜백 URL을 받아 처리.
      // 여기서는 교육용으로 결과 페이지로 바로 이동.
      navigate('/result', { state: { success: true, message: '본인인증 요청이 성공적으로 완료되었습니다. 잠시 후 결과가 위탁사로 전달됩니다.' } });
    } catch (err) {
      console.error('인증 요청 실패:', err.response ? err.response.data : err.message);
      setError(err.response?.data?.message || '인증 요청 중 오류가 발생했습니다.');
      navigate('/result', { state: { success: false, message: '본인인증 요청에 실패했습니다. 다시 시도해주세요.' } });
    }
  };

  return (
    <div className="auth-container">
      <h2>본인인증</h2>
      <p>본인 확인을 위해 이름과 휴대폰 번호를 입력해주세요.</p>
      <form onSubmit={handleSubmit} className="auth-form">
        <div className="form-group">
          <label htmlFor="name">이름</label>
          <input
            type="text"
            id="name"
            value={name}
            onChange={(e) => setName(e.target.value)}
            placeholder="예: 홍길동"
            required
          />
        </div>
        <div className="form-group">
          <label htmlFor="phoneNumber">휴대폰 번호</label>
          <input
            type="tel"
            id="phoneNumber"
            value={phoneNumber}
            onChange={(e) => setPhoneNumber(e.target.value)}
            placeholder="예: 010-1234-5678" // 실제로는 숫자만 입력받고 백엔드에서 처리
            required
          />
        </div>
        {error && <p className="error-message">{error}</p>}
        <button type="submit" className="auth-button">인증 요청</button>
      </form>

      {/* k3s/컨테이너 전제 설명 주석 */}
      {/*
        이 React 프론트엔드 애플리케이션은 컨테이너화되어 k3s 환경에서 동작합니다.
        빌드 시 Dockerfile을 통해 이미지가 생성되고,
        Kubernetes Deployment에 의해 파드가 배포됩니다.
        Service를 통해 내부적으로 노출되며,
        Ingress 또는 NodePort를 통해 외부에서 접근 가능하도록 설정될 예정입니다.
      */}
    </div>
  );
}

export default AuthPage;
