import React, { useState, useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { ChevronLeft, CheckCircle2 } from 'lucide-react';
import Logo from '../components/Logo';

const IdentityVerification = () => {
  const navigate = useNavigate();
  const [name, setName] = useState('');
  const [residentFront, setResidentFront] = useState('');
  const [telecom, setTelecom] = useState('');
  const [phoneNumber, setPhoneNumber] = useState('');
  const [otpSent, setOtpSent] = useState(false);
  const [otp, setOtp] = useState('');
  const [timer, setTimer] = useState(180);
  const [message, setMessage] = useState('');
  const [tokenId, setTokenId] = useState(null);
  const [isDataLocked, setIsDataLocked] = useState(false);

  useEffect(() => {
    const query = new URLSearchParams(window.location.search);
    const urlTokenId = query.get('tokenId');
    const urlName = query.get('name');
    const urlPhone = query.get('phoneNumber');

    if (urlTokenId) setTokenId(urlTokenId);

    if (urlName || urlPhone) {
      if (urlName) setName(urlName);
      if (urlPhone) setPhoneNumber(formatPhoneNumber(urlPhone));
      setIsDataLocked(true);
    }
  }, []);

  // 휴대폰 번호 포맷팅 함수
  const formatPhoneNumber = (val) => {
    const numbers = val.replace(/\D/g, '');
    if (numbers.length <= 3) return numbers;
    if (numbers.length <= 7) return `${numbers.slice(0, 3)}-${numbers.slice(3)}`;
    return `${numbers.slice(0, 3)}-${numbers.slice(3, 7)}-${numbers.slice(7, 11)}`;
  };

  const isFormValid = name && residentFront.length === 6 && telecom && phoneNumber.length >= 10;

  const timerRef = useRef(null);

  useEffect(() => {
    if (otpSent && timer > 0) {
      timerRef.current = setInterval(() => {
        setTimer(prev => prev - 1);
      }, 1000);
    } else {
      clearInterval(timerRef.current);
    }
    return () => clearInterval(timerRef.current);
  }, [otpSent, timer]);

  const handleRequestOtp = async () => {
    if (!name || !residentFront || !telecom || !phoneNumber) {
      setMessage('모든 정보를 입력해 주세요.');
      return;
    }

    try {
      const cleanPhoneNumber = phoneNumber.replace(/\D/g, '');
      // [보안 강화] 신규 세션을 생성하지 않고, 전달받은 TokenId로 정보 일치 여부 확인
      const response = await fetch('/api/v1/auth/request-otp', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          tokenId: tokenId,
          name: name,
          phoneNumber: cleanPhoneNumber,
          residentFront: residentFront
        }),
      });

      const rawBody = await response.text();
      let data = {};
      try {
        if (rawBody) data = JSON.parse(rawBody);
      } catch (e) {
        data = { message: rawBody };
      }

      if (response.ok && data.otp) {
        setOtp(data.otp); // 테스트 편의를 위해 설정
        setOtpSent(true);
        setTimer(180);
        setMessage('✅ 인증번호가 발송되었습니다.');
      } else {
        setMessage(`❌ ${data.message || '정보가 불일치하거나 요청에 실패했습니다.'}`);
      }
    } catch (error) {
      setMessage('⚠️ 오류 발생: ' + error.message);
    }
  };

  const handleVerifyOtp = async () => {
    if (otp.length !== 6) {
      setMessage('인증번호 6자리를 입력하세요.');
      return;
    }

    try {
      console.log('[DEBUG] Verification Request:', { tokenId, otp });
      const response = await fetch('/api/v1/auth/confirm', {
        method: 'PATCH',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ tokenId: tokenId, otp: otp }),
      });

      if (response.ok) {
        const query = new URLSearchParams(window.location.search);
        const redirectUrl = query.get('redirectUrl');

        if (redirectUrl) {
          const finalUrl = new URL(redirectUrl);
          const cleanPhone = phoneNumber.replace(/\D/g, '');
          finalUrl.searchParams.set('tokenId', tokenId || '');
          finalUrl.searchParams.set('phoneNumber', cleanPhone);
          finalUrl.searchParams.set('name', name);
          window.location.href = finalUrl.toString();
        } else {
          setMessage('✅ 본인인증이 성공적으로 완료되었습니다.');
        }
      } else {
        const errorMsg = await response.text();
        setMessage(`❌ 인증번호가 일치하지 않습니다.`);
      }
    } catch (error) {
      setMessage('⚠️ 네트워크 오류: ' + error.message);
    }
  };

  const formatTime = (seconds) => {
    const mins = Math.floor(seconds / 60);
    const secs = seconds % 60;
    return `${mins}:${secs.toString().padStart(2, '0')}`;
  };

  return (
    <div className="flex flex-col min-h-screen bg-white">
      {/* Header */}
      <header className="flex items-center h-20 px-6">
        <button
          onClick={() => {
            const query = new URLSearchParams(window.location.search);
            const redirectUrl = query.get('redirectUrl');
            if (redirectUrl) {
              try {
                const url = new URL(redirectUrl);
                // 위탁사의 메인(로그인) 페이지 등으로 복귀
                window.location.href = url.origin + '/login';
              } catch (e) {
                navigate(-1);
              }
            } else {
              navigate(-1);
            }
          }}
          className="p-2 -ml-2 rounded-full hover:bg-gray-50 transition-colors"
        >
          <ChevronLeft size={28} className="text-gray-700" />
        </button>
        <div className="flex-1 flex justify-center -ml-10">
          <Logo />
        </div>
      </header>

      {/* Content */}
      <main className="flex-1 px-8 py-12 max-w-[480px] mx-auto w-full">
        <h1 className="text-[32px] font-extrabold text-gray-900 leading-tight tracking-tight mb-10">
          안전한 서비스 이용을 위해<br />
          본인인증이 필요합니다.
        </h1>

        <div className="space-y-8">
          {/* 이름 */}
          <div>
            <label className="input-label">성명</label>
            <div className="relative">
              <input
                type="text"
                value={name}
                onChange={(e) => setName(e.target.value)}
                placeholder="성명을 입력하세요"
                className={`input-field !rounded-lg ${isDataLocked ? 'bg-gray-100/50 text-gray-400 font-bold pr-12' : ''}`}
                readOnly={isDataLocked}
              />
              {isDataLocked && (
                <div className="absolute right-4 top-1/2 -translate-y-1/2 text-emerald-600">
                  <CheckCircle2 size={20} />
                </div>
              )}
            </div>
          </div>

          {/* 주민등록번호 */}
          <div>
            <label className="input-label">주민등록번호 앞 6자리</label>
            <input
              type="text"
              inputMode="numeric"
              maxLength={6}
              value={residentFront}
              onChange={(e) => setResidentFront(e.target.value.replace(/\D/g, ''))}
              placeholder="생년월일 6자리"
              className="input-field !rounded-lg"
            />
          </div>

          {/* 통신사 */}
          <div>
            <label className="input-label">통신사 선택</label>
            <select
              value={telecom}
              onChange={(e) => setTelecom(e.target.value)}
              className="select-field !rounded-lg"
            >
              <option value="" disabled hidden>통신사를 선택하세요</option>
              <option value="SKT">SKT</option>
              <option value="KT">KT</option>
              <option value="LGU+">LG U+</option>
              <option value="ALDDLE">알뜰폰</option>
            </select>
          </div>

          {/* 휴대폰 번호 */}
          <div>
            <label className="input-label">휴대폰 번호</label>
            <div className="flex gap-3">
              <div className="relative flex-1">
                <input
                  type="tel"
                  inputMode="numeric"
                  value={phoneNumber}
                  onChange={(e) => setPhoneNumber(formatPhoneNumber(e.target.value))}
                  placeholder="인증받을 번호 입력"
                  className={`input-field !rounded-lg w-full ${isDataLocked ? 'bg-gray-100/50 text-gray-400 font-bold pr-12' : ''}`}
                  readOnly={isDataLocked}
                  disabled={otpSent}
                />
                {isDataLocked && (
                  <div className="absolute right-4 top-1/2 -translate-y-1/2 text-emerald-600">
                    <CheckCircle2 size={20} />
                  </div>
                )}
              </div>
              <button
                type="button"
                onClick={handleRequestOtp}
                className="btn-action whitespace-nowrap self-center !rounded-lg"
                disabled={otpSent && timer > 150}
              >
                {otpSent ? '재발송' : '인증번호발송'}
              </button>
            </div>
          </div>

          {/* OTP 입력란 */}
          {otpSent && (
            <div className="space-y-5 animate-in fade-in slide-in-from-top-2 duration-300">
              {/* 테스트 가이드 박스 (개발용) */}
              <div className="p-5 bg-amber-50 border border-amber-100 rounded-2xl">
                <p className="text-amber-800 text-sm font-medium leading-relaxed">
                  💡 <span className="font-bold underline">인증번호 안내</span>: 현재 테스트 모드입니다.<br />
                  입력하실 번호는 <span className="text-red-600 font-extrabold ml-1">{otp}</span> 입니다.
                </p>
              </div>

              <div>
                <label className="input-label">인증번호 6자리</label>
                <div className="relative">
                  <input
                    type="text"
                    inputMode="numeric"
                    maxLength={6}
                    value={otp}
                    onChange={(e) => setOtp(e.target.value.replace(/\D/g, ''))}
                    placeholder="인증번호 6자리 입력"
                    className="input-field pr-16 !rounded-lg"
                  />
                  <span className="absolute right-4 top-1/2 -translate-y-1/2 timer-text">
                    {formatTime(timer)}
                  </span>
                </div>
              </div>
            </div>
          )}
        </div>

        {/* Feedback Message */}
        {message && (
          <p className={`mt-8 text-center text-sm font-semibold flex items-center justify-center gap-2 ${message.includes('성공') || message.includes('발송') ? 'text-[#1A73E8]' : 'text-red-500'}`}>
            <span>{message.includes('성공') || message.includes('발송') ? '✅' : '⚠️'}</span> {message}
          </p>
        )}
      </main>

      {/* Primary Action */}
      <div className="px-8 pb-12 max-w-[480px] mx-auto w-full">
        <button
          onClick={otpSent ? handleVerifyOtp : handleRequestOtp}
          disabled={otpSent ? otp.length !== 6 : !isFormValid}
          className="btn-primary !rounded-lg"
        >
          {otpSent ? '인증번호 확인' : '인증번호발송'}
        </button>
      </div>
    </div>
  );
};

export default IdentityVerification;
