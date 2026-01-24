import React, { useEffect, useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';

/**
 * @module ResultPage
 * @description 본인인증 결과를 표시하는 페이지 컴포넌트입니다.
 * 이전 페이지(AuthPage)로부터 전달받은 인증 성공/실패 여부 및 메시지를 보여줍니다.
 */
function ResultPage() {
  const location = useLocation();
  const navigate = useNavigate();
  const [result, setResult] = useState(null);

  useEffect(() => {
    // location.state에서 결과 데이터를 가져옵니다.
    // AuthPage에서 navigate 시 state로 전달한 데이터를 받습니다.
    if (location.state) {
      setResult(location.state);
    }
  }, [location.state]);

  // 결과 데이터가 없을 경우 홈으로 리다이렉트
  if (!result) {
    // navigate("/", { replace: true }); // 무한 리다이렉트 방지를 위해 주석 처리하거나, 필요에 따라 구현
    return (
      <div className="result-container">
        <h2>잘못된 접근</h2>
        <p>올바른 경로로 접근해주세요.</p>
        <button onClick={() => navigate("/", { replace: true })}>홈으로 돌아가기</button>
      </div>
    );
  }

  const { success, message } = result;

  return (
    <div className={`result-container ${success ? 'success' : 'failure'}`}>
      <h2>본인인증 결과</h2>
      <p>{message}</p>
      <button onClick={() => navigate("/", { replace: true })}>다시 시도 또는 홈으로</button>

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

export default ResultPage;
