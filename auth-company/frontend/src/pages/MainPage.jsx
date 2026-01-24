import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import api from '../api';

function MainPage() {
  const [backendMessage, setBackendMessage] = useState('');
  const [healthStatus, setHealthStatus] = useState('연결 중...');

  useEffect(() => {
    const fetchBackendHealth = async () => {
      try {
        const response = await api.get('/api/health');
        // 백엔드가 "OK" 문자열 대신 JSON 객체를 반환할 수 있으므로, 응답 구조를 확인해야 합니다.
        if (response.data && typeof response.data === 'object' && response.data.status === 'OK') {
          setBackendMessage('백엔드와 성공적으로 연결되었습니다.');
          setHealthStatus('OK');
        } else if (response.data === 'OK') { // 단순 문자열 "OK" 응답 처리
          setBackendMessage('백엔드와 성공적으로 연결되었습니다.');
          setHealthStatus('OK');
        } else {
          setBackendMessage('백엔드 연결 실패: 응답이 "OK"가 아닙니다.');
          setHealthStatus('연결 실패');
        }
      } catch (error) {
        console.error('백엔드 헬스체크 오류:', error);
        setBackendMessage('백엔드 연결 실패: 서버에 접속할 수 없습니다. 콘솔을 확인해주세요.');
        setHealthStatus('연결 실패');
      }
    };
    fetchBackendHealth();
  }, []);

  return (
    <div className="min-h-screen bg-gray-900 text-gray-100 p-8">
      <div className="max-w-4xl mx-auto bg-gray-800 p-8 rounded-lg shadow-lg">
        <h2 className="text-4xl font-extrabold text-white mb-6">기업 인증 시스템 대시보드</h2>
        <p className="text-lg text-gray-300 mb-8">
          이 시스템은 교육 목적으로 개발된 본인인증 수탁사 웹 시스템입니다.
          실제 금융사 수탁사 구조를 단순화하여 React 프론트엔드와 Spring Boot 백엔드로 구성되었습니다.
        </p>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-8 mb-10">
          <div className="bg-gray-700 p-6 rounded-lg shadow-md">
            <h3 className="text-2xl font-bold text-white mb-4">빠른 메뉴</h3>
            <ul className="space-y-3">
              <li>
                <Link to="/auth" className="text-blue-400 hover:text-blue-300 transition duration-300 ease-in-out text-lg">
                  <span className="mr-2">➡️</span> 본인인증 시작
                </Link>
              </li>
              <li>
                <Link to="/mypage" className="text-blue-400 hover:text-blue-300 transition duration-300 ease-in-out text-lg">
                  <span className="mr-2">⚙️</span> 마이페이지 (구현 예정)
                </Link>
              </li>
              <li>
                <Link to="/admin" className="text-blue-400 hover:text-blue-300 transition duration-300 ease-in-out text-lg">
                  <span className="mr-2">🔒</span> 관리자 패널 (구현 예정)
                </Link>
              </li>
            </ul>
          </div>

          <div className="bg-gray-700 p-6 rounded-lg shadow-md">
            <h3 className="text-2xl font-bold text-white mb-4">백엔드 연결 상태</h3>
            <p className="text-lg text-gray-300 mb-2">메시지: {backendMessage}</p>
            <p className="text-lg">상태: 
              <span className={`font-bold ${healthStatus === 'OK' ? 'text-green-400' : 'text-red-400'}`}>
                {healthStatus}
              </span>
            </p>
            {healthStatus === '연결 실패' && (
              <p className="text-sm text-red-300 mt-2">연결에 문제가 있다면, 백엔드 서버가 실행 중인지, Nginx 설정이 올바른지, 그리고 네트워크 방화벽을 확인해주세요.</p>
            )}
          </div>
        </div>

        <footer className="text-center text-gray-500 mt-10">
          <p>© 2026 기업 인증 시스템. All rights reserved.</p>
        </footer>
      </div>
    </div>
  );
}

export default MainPage;
