import React, { useState, useEffect, useRef } from 'react';
import { useSearchParams, useNavigate } from 'react-router-dom';
import { ChevronLeft } from 'lucide-react';

const OtpInput = () => {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const [otp, setOtp] = useState(Array(6).fill(''));
  const [message, setMessage] = useState('');
  const [timer, setTimer] = useState(180);
  const [isResendDisabled, setIsResendDisabled] = useState(true);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const inputRefs = useRef([]);

  const tokenId = searchParams.get('tokenId');
  const phoneNumber = searchParams.get('phoneNumber');
  const redirectUrl = searchParams.get('redirectUrl');

  useEffect(() => {
    if (!tokenId || !phoneNumber || !redirectUrl) {
      setMessage('잘못된 접근입니다. 필요한 정보가 누락되었습니다.');
      return;
    }

    const countdown = setInterval(() => {
      setTimer((prev) => {
        if (prev <= 1) {
          clearInterval(countdown);
          setIsResendDisabled(false);
          return 0;
        }
        return prev - 1;
      });
    }, 1000);

    // Focus first input
    if (inputRefs.current[0]) {
      inputRefs.current[0].focus();
    }

    return () => clearInterval(countdown);
  }, [tokenId, phoneNumber, redirectUrl]);

  const handleOtpChange = (e, index) => {
    const value = e.target.value;
    if (/[^0-9]/.test(value)) return;

    const newOtp = [...otp];
    newOtp[index] = value;
    setOtp(newOtp);

    if (value && index < 5) {
      inputRefs.current[index + 1].focus();
    }
  };

  const handlePaste = (e) => {
    const pasteData = e.clipboardData.getData('text/plain').slice(0, 6);
    if (/[^0-9]/.test(pasteData)) return;

    const newOtp = pasteData.split('').concat(Array(6 - pasteData.length).fill(''));
    setOtp(newOtp);
    inputRefs.current[Math.min(pasteData.length, 5)].focus();
    e.preventDefault();
  };

  const handleKeyDown = (e, index) => {
    if (e.key === 'Backspace' && !otp[index] && index > 0) {
      inputRefs.current[index - 1].focus();
    }
  };

  const formatTime = (seconds) => {
    const minutes = Math.floor(seconds / 60);
    const remainingSeconds = seconds % 60;
    return `${minutes}:${remainingSeconds.toString().padStart(2, '0')}`;
  };

  const isOtpComplete = otp.every((digit) => digit !== '');

  const handleConfirm = async () => {
    if (!isOtpComplete) {
      setMessage('인증번호 6자리를 모두 입력해주세요.');
      return;
    }

    setIsSubmitting(true);
    try {
      const response = await fetch('/api/v1/auth/confirm', {
        method: 'PATCH',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ tokenId: tokenId }),
      });

      if (response.ok) {
        const callbackUrl = new URL(redirectUrl);
        callbackUrl.searchParams.append('tokenId', tokenId);
        callbackUrl.searchParams.append('phoneNumber', phoneNumber);
        window.location.href = callbackUrl.toString();
      } else {
        const errorData = await response.text();
        setMessage('인증 실패: ' + errorData);
      }
    } catch (error) {
      setMessage('오류 발생: ' + error.message);
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleResendOtp = () => {
    setMessage('인증번호를 재전송했습니다.');
    setTimer(180);
    setIsResendDisabled(true);
  };

  return (
    <div className="flex flex-col min-h-screen">
      {/* Header */}
      <header className="flex items-center h-14 px-4 border-b border-gray-100">
        <button onClick={() => navigate(-1)} className="p-1 -ml-1">
          <ChevronLeft size={24} className="text-gray-700" />
        </button>
      </header>

      {/* Content */}
      <main className="flex-1 px-5 py-6">
        <h1 className="text-[22px] font-bold text-gray-900 leading-tight mb-2">
          인증번호를<br />입력해주세요
        </h1>
        <p className="text-[15px] text-gray-500 mb-8">
          {phoneNumber || '등록된 번호'}로 전송된 6자리 인증번호를 입력하세요
        </p>

        {/* OTP Input */}
        <div className="flex justify-center gap-2 mb-6">
          {otp.map((digit, index) => (
            <input
              key={index}
              ref={(el) => (inputRefs.current[index] = el)}
              type="text"
              inputMode="numeric"
              maxLength={1}
              value={digit}
              onChange={(e) => handleOtpChange(e, index)}
              onKeyDown={(e) => handleKeyDown(e, index)}
              onPaste={handlePaste}
              className="otp-input"
            />
          ))}
        </div>

        {/* Timer */}
        <div className="text-center mb-6">
          {timer > 0 ? (
            <span className="timer-text">{formatTime(timer)}</span>
          ) : (
            <span className="text-gray-500 text-[15px]">인증 시간이 만료되었습니다</span>
          )}
        </div>

        {/* Resend Button */}
        <div className="text-center">
          <button
            type="button"
            onClick={handleResendOtp}
            disabled={isResendDisabled}
            className="link-button"
          >
            인증번호 재전송
          </button>
        </div>

        {/* Error Message */}
        {message && (
          <p className="error-text text-center mt-4">{message}</p>
        )}
      </main>

      {/* Bottom Button */}
      <div className="px-5 py-4 pb-8">
        <button
          onClick={handleConfirm}
          disabled={!isOtpComplete || isSubmitting}
          className="btn-primary"
        >
          {isSubmitting ? '확인 중...' : '확인'}
        </button>
      </div>
    </div>
  );
};

export default OtpInput;
