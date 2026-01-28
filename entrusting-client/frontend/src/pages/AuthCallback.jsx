import React, { useEffect, useState } from 'react';
import { useSearchParams, useNavigate } from 'react-router-dom';
import { Loader2, CheckCircle2, AlertCircle } from 'lucide-react';

const AuthCallback = () => {
  const [searchParams] = useSearchParams();
  const [status, setStatus] = useState('loading'); // 'loading', 'success', 'redirecting', 'error'
  const [message, setMessage] = useState('본인인증 결과를 확인하고 있습니다');
  const navigate = useNavigate();

  useEffect(() => {
    const tokenId = searchParams.get('tokenId');
    const phoneNumber = searchParams.get('phoneNumber');

    if (tokenId) {
      const cleanPhone = phoneNumber?.replace(/\D/g, '') || '';
      fetch('/api/v1/auth/callback', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ tokenId, phoneNumber: cleanPhone }),
      })
        .then(async (response) => {
          console.log('[CALLBACK-DEBUG] Status:', response.status);
          const rawText = await response.text();
          console.log('[CALLBACK-DEBUG] Raw Body:', rawText);

          let isSuccess = false;
          let responseData = null;

          try {
            responseData = JSON.parse(rawText);
            if (response.ok && (responseData.status === 'success' || (responseData.message && responseData.message.toLowerCase().includes('success')))) {
              isSuccess = true;
            }
          } catch (e) {
            if (response.ok && rawText.toLowerCase().includes('success')) {
              isSuccess = true;
            }
          }

          if (isSuccess) {
            // 1단계: 인증 성공 표시
            setStatus('success');
            setMessage('본인인증을 완료했습니다');
            
            // 2단계: 1.2초 후 Continue Bank 로딩 화면으로 전환
            setTimeout(() => {
              setStatus('redirecting');
              setMessage('Continue Bank로 이동하고 있습니다');
              
              // 3단계: 2초 후 최종 페이지 이동
              const isRegistering = !!sessionStorage.getItem('register_form_data');
              const nameValue = (responseData && responseData.name) ? responseData.name : '인증완료';
              const nameParam = `&name=${encodeURIComponent(nameValue)}`;
              
              const targetPath = isRegistering 
                ? `/register?verified=true&phoneNumber=${phoneNumber}&tokenId=${tokenId}${nameParam}`
                : '/dashboard';
                
              setTimeout(() => {
                navigate(targetPath);
              }, 2000);
            }, 1200);

          } else {
            setStatus('error');
            setMessage(`검증 실패: 정보가 불일치합니다`);
          }
        })
        .catch((error) => {
          console.error('[CALLBACK-DEBUG] CRITICAL Network Error:', error);
          setStatus('error');
          setMessage('네트워크 연결 확인이 필요합니다');
        });
    } else {
      setStatus('error');
      setMessage('잘못된 접근입니다');
    }
  }, [searchParams, navigate]);

  if (status === 'redirecting') {
    return (
      <div className="flex flex-col items-center justify-center min-h-screen px-8 bg-white overflow-hidden">
        {/* Continue Bank 로고 애니메이션 영역 */}
        <div className="relative mb-12">
          <div className="absolute inset-0 bg-blue-100 rounded-full scale-150 blur-2xl opacity-20 animate-pulse"></div>
          <div className="relative flex items-center justify-center space-x-2 animate-in zoom-in slide-in-from-bottom-8 duration-700">
            <div className="w-12 h-12 bg-[#1A73E8] rounded-xl flex items-center justify-center shadow-lg shadow-blue-200">
              <span className="text-white text-3xl font-black italic">C</span>
            </div>
            <span className="text-3xl font-black text-gray-900 tracking-tight">
              Continue <span className="text-[#1A73E8]">Bank</span>
            </span>
          </div>
        </div>

        {/* 로딩 진행 바 */}
        <div className="w-64 h-1.5 bg-gray-100 rounded-full mb-8 overflow-hidden relative">
          <div className="absolute top-0 left-0 h-full bg-[#1A73E8] rounded-full animate-[loading_2s_infinite]"></div>
        </div>

        <h1 className="text-2xl font-bold text-gray-900 text-center mb-3">
          {message}
        </h1>
        <p className="text-[17px] text-gray-500 text-center leading-relaxed">
          고객님의 정보를 확인하여 <br />
          안전하게 서비스를 연결하고 있습니다.
        </p>

        <style>{`
          @keyframes loading {
            0% { width: 0%; left: 0%; }
            50% { width: 40%; left: 30%; }
            100% { width: 0%; left: 100%; }
          }
        `}</style>
      </div>
    );
  }

  return (
    <div className="flex flex-col items-center justify-center min-h-screen px-8 bg-white">
      <div className="mb-10">
        {status === 'loading' && (
          <Loader2 size={64} className="text-[#1A73E8] animate-spin" />
        )}
        {status === 'success' && (
          <div className="w-20 h-20 bg-blue-50 rounded-full flex items-center justify-center animate-in zoom-in duration-500">
            <CheckCircle2 size={48} className="text-[#1A73E8]" />
          </div>
        )}
        {status === 'error' && (
          <div className="w-20 h-20 bg-red-50 rounded-full flex items-center justify-center animate-in zoom-in duration-500">
            <AlertCircle size={48} className="text-red-500" />
          </div>
        )}
      </div>

      <h1 className="text-2xl font-bold text-gray-900 text-center mb-3">
        {message}
      </h1>
      <p className="text-[17px] text-gray-500 text-center leading-relaxed">
        {status === 'loading' ? '잠시만 기다려주세요' :
          status === 'success' ? '잠시 후 은행 시스템으로 복귀합니다' :
            '정보 검증 중 오류가 발생했습니다. 다시 입력해 주세요.'}
      </p>

      {status === 'error' && (
        <div className="mt-12 w-full space-y-4">
          <button
            onClick={() => navigate('/login')}
            className="w-full h-14 bg-gray-900 text-white rounded-xl font-bold text-lg hover:bg-gray-800 transition-colors"
          >
            홈으로 돌아가기
          </button>
        </div>
      )}
    </div>
  );
};

export default AuthCallback;
