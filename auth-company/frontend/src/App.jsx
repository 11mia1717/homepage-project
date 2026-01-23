import { useState, useEffect } from 'react'
import axios from 'axios'
import './App.css'

function App() {
  const [backendMessage, setBackendMessage] = useState('');
  const [healthStatus, setHealthStatus] = useState('연결 중...');

  useEffect(() => {
    const fetchBackendData = async () => {
      try {
        // Fetch initial welcome message
        const backendApiUrl = import.meta.env.VITE_BACKEND_API_URL || 'http://localhost:8080';
        const healthResponse = await axios.get(`${backendApiUrl}/api/health`);
        if (healthResponse.data.status === 'OK') {
          setBackendMessage('Backend connected');
          setHealthStatus('OK');
        } else {
          setBackendMessage('Failed to connect to backend.');
          setHealthStatus('연결 실패');
        }
      } catch (error) {
        console.error('Error fetching backend data:', error);
        setBackendMessage('Failed to connect to backend.');
        setHealthStatus('연결 실패');
      }
    };
    fetchBackendData();
  }, []);

  return (
    <div className="App">
      <header className="App-header">
        <h1>금융사 본인인증 서비스 안내</h1>
        <p>안전하고 편리한 본인인증 서비스를 경험하세요.</p>
        <p>백엔드 메시지: {backendMessage}</p>
        <p>백엔드 연결 상태: {healthStatus}</p>
        <div className="service-details">
          <h2>서비스 특징</h2>
          <ul>
            <li>강력한 보안: 최신 암호화 기술 적용</li>
            <li>간편한 사용: 몇 번의 클릭으로 본인인증 완료</li>
            <li>다양한 인증 수단: 휴대폰, 공동인증서 등 지원</li>
            <li>24시간 365일 운영: 언제든 필요할 때 이용 가능</li>
          </ul>
        </div>
        <div className="contact-info">
          <h2>문의</h2>
          <p>궁금한 점이 있으시면 언제든지 고객센터로 문의해주세요.</p>
          <p>전화: 1588-1234 | 이메일: support@authcompany.com</p>
        </div>
      </header>
    </div>
  )
}

export default App
