import { useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { motion, AnimatePresence } from 'framer-motion';
import toast from 'react-hot-toast';
import { consentApi } from '../services/api';

export default function ConsentPage() {
  const { productName } = useParams<{ productName: string }>();
  const navigate = useNavigate();
  const decodedProductName = decodeURIComponent(productName || '');
  
  const [step, setStep] = useState(1);
  const [formData, setFormData] = useState({
    customerName: '',
    customerPhone: '',
    agreeThirdPartyProvision: false,
  });
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [result, setResult] = useState<{ requestId: string; retentionUntil: string } | null>(null);
  
  // Verification State
  const [tokenId, setTokenId] = useState<string | null>(null);
  const [isVerified, setIsVerified] = useState(false);

  useEffect(() => {
    // Check for verification callback
    const query = new URLSearchParams(window.location.search);
    const urlTokenId = query.get('tokenId');
    const urlName = query.get('name');
    const urlPhone = query.get('phoneNumber');

    if (urlTokenId && urlName && urlPhone) {
      setTokenId(urlTokenId);
      setFormData(prev => ({
        ...prev,
        customerName: urlName,
        customerPhone: urlPhone
      }));
      setIsVerified(true);
      toast.success('본인인증이 완료되었습니다.');
      // Clean URL
      window.history.replaceState({}, '', window.location.pathname + `#/consent/${productName}`);
    }
  }, []);

  const handleVerify = async () => {
    if (!formData.customerName || !formData.customerPhone) {
      toast.error('이름과 연락처를 입력해 주세요.');
      return;
    }

    setIsSubmitting(true);
    try {
      // 1. Init Auth to get Token ID
      const response = await fetch('/trustee-api/v1/auth/init', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          name: formData.customerName,
          clientData: formData.customerPhone, // Assuming simple string for demo
          carrier: 'SKT', // Default or add carrier select if needed
          authRequestId: `TM-CONSENT-${Date.now()}`
        })
      });

      if (!response.ok) throw new Error('인증 초기화 실패');
      
      const data = await response.json();
      const newTokenId = data.tokenId;

      // 2. Redirect to Trustee Frontend
      const redirectUrl = encodeURIComponent(window.location.href);
      // Use raw values for verification match
      window.location.href = `http://localhost:5176/verify?tokenId=${newTokenId}&redirectUrl=${redirectUrl}&name=${encodeURIComponent(formData.customerName)}&phoneNumber=${formData.customerPhone}`;
      
    } catch (error) {
      toast.error('인증 시작 중 오류가 발생했습니다.');
      setIsSubmitting(false);
    }
  };

  const handleSubmit = async () => {
    if (!formData.agreeThirdPartyProvision) {
      toast.error('제3자 제공 동의는 필수입니다.');
      return;
    }

    setIsSubmitting(true);
    try {
      const response = await consentApi.processConsent({
        customerName: formData.customerName,
        customerPhone: formData.customerPhone,
        productName: decodedProductName,
        agreeThirdPartyProvision: formData.agreeThirdPartyProvision,
        agreeMarketing: true,
        tokenId: tokenId, // Pass Token ID
      });

      setResult({
        requestId: response.requestId,
        retentionUntil: response.retentionUntil,
      });
      setStep(3);
      toast.success('동의가 완료되었습니다!');
    } catch (error) {
      toast.error(error instanceof Error ? error.message : '오류가 발생했습니다.');
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="min-h-[calc(100vh-4rem)] py-12">
      <div className="max-w-lg mx-auto px-4">
        {/* 진행 상태 */}
        <div className="flex items-center justify-center mb-8">
          {[1, 2, 3].map((s) => (
            <div key={s} className="flex items-center">
              <div
                className={`w-8 h-8 rounded-full flex items-center justify-center text-sm font-medium transition-colors ${
                  s <= step
                    ? 'bg-toss-blue-500 text-white'
                    : 'bg-toss-gray-200 text-toss-gray-500'
                }`}
              >
                {s < step ? '✓' : s}
              </div>
              {s < 3 && (
                <div
                  className={`w-16 h-1 mx-2 transition-colors ${
                    s < step ? 'bg-toss-blue-500' : 'bg-toss-gray-200'
                  }`}
                />
              )}
            </div>
          ))}
        </div>

        <AnimatePresence mode="wait">
          {/* Step 1: 정보 입력 */}
          {step === 1 && (
            <motion.div
              key="step1"
              initial={{ opacity: 0, x: 20 }}
              animate={{ opacity: 1, x: 0 }}
              exit={{ opacity: 0, x: -20 }}
              className="card"
            >
              <div className="text-center mb-8">
                <div className="w-16 h-16 mx-auto bg-toss-blue-50 rounded-full flex items-center justify-center text-3xl mb-4">
                  💳
                </div>
                <h1 className="text-2xl font-bold text-toss-gray-900 mb-2">
                  {decodedProductName} 상담 신청
                </h1>
                <p className="text-toss-gray-600">
                  본인인증 후 상담 신청이 가능합니다
                </p>
              </div>

              <div className="space-y-4">
                <div>
                  <label className="label-toss">이름</label>
                  <input
                    type="text"
                    className="input-toss"
                    placeholder="홍길동"
                    value={formData.customerName}
                    disabled={isVerified}
                    onChange={(e) =>
                      setFormData({ ...formData, customerName: e.target.value })
                    }
                  />
                </div>
                <div>
                  <label className="label-toss">연락처</label>
                  <input
                    type="tel"
                    className="input-toss"
                    placeholder="010-1234-5678"
                    value={formData.customerPhone}
                    disabled={isVerified}
                    onChange={(e) =>
                      setFormData({ ...formData, customerPhone: e.target.value })
                    }
                  />
                </div>
              </div>

              {!isVerified ? (
                <button
                  onClick={handleVerify}
                  disabled={!formData.customerName || !formData.customerPhone || isSubmitting}
                  className="btn-primary w-full mt-6"
                >
                  {isSubmitting ? '인증 준비중...' : '본인인증 하기'}
                </button>
              ) : (
                <div className="mt-6">
                  <div className="bg-green-50 text-green-700 px-4 py-3 rounded-toss text-sm font-medium text-center mb-4">
                    ✅ 본인인증이 완료되었습니다
                  </div>
                  <button
                    onClick={() => setStep(2)}
                    className="btn-primary w-full"
                  >
                    다음
                  </button>
                </div>
              )}
            </motion.div>
          )}

          {/* Step 2: 동의 */}
          {step === 2 && (
            <motion.div
              key="step2"
              initial={{ opacity: 0, x: 20 }}
              animate={{ opacity: 1, x: 0 }}
              exit={{ opacity: 0, x: -20 }}
              className="card"
            >
              <div className="text-center mb-6">
                <h1 className="text-2xl font-bold text-toss-gray-900 mb-2">
                  개인정보 제3자 제공 동의
                </h1>
                <p className="text-toss-gray-600">
                  상담을 위해 아래 내용에 동의해 주세요
                </p>
              </div>

              {/* 동의서 내용 */}
              <div className="bg-toss-gray-50 rounded-toss p-5 mb-6">
                <h3 className="font-semibold text-toss-gray-900 mb-4 flex items-center gap-2">
                  <span className="text-toss-blue-500">📋</span>
                  개인정보 제3자 제공 동의서
                </h3>
                
                <div className="space-y-4 text-sm">
                  <div className="flex gap-3">
                    <span className="text-toss-gray-500 w-24 flex-shrink-0">제공받는 자</span>
                    <span className="text-toss-gray-900 font-medium">○○TM센터</span>
                  </div>
                  <div className="flex gap-3">
                    <span className="text-toss-gray-500 w-24 flex-shrink-0">제공 목적</span>
                    <span className="text-toss-gray-900 font-medium">{decodedProductName} 상담</span>
                  </div>
                  <div className="flex gap-3">
                    <span className="text-toss-gray-500 w-24 flex-shrink-0">제공 항목</span>
                    <span className="text-toss-gray-900 font-medium">이름, 연락처</span>
                  </div>
                  <div className="flex gap-3">
                    <span className="text-toss-gray-500 w-24 flex-shrink-0">보유 기간</span>
                    <span className="text-toss-gray-900 font-medium text-toss-blue-500">상담 완료 후 3개월</span>
                  </div>
                </div>

                <div className="mt-4 pt-4 border-t border-toss-gray-200">
                  <p className="text-xs text-toss-gray-500">
                    ※ 귀하는 위 동의를 거부할 권리가 있습니다. 다만, 동의를 거부하시는 경우 
                    상담 서비스를 이용하실 수 없습니다.
                  </p>
                </div>
              </div>

              {/* 체크박스 */}
              <label className="flex items-start gap-3 p-4 bg-white border border-toss-gray-200 rounded-toss cursor-pointer hover:border-toss-blue-500 transition-colors">
                <input
                  type="checkbox"
                  className="checkbox-toss mt-0.5"
                  checked={formData.agreeThirdPartyProvision}
                  onChange={(e) =>
                    setFormData({
                      ...formData,
                      agreeThirdPartyProvision: e.target.checked,
                    })
                  }
                />
                <div>
                  <span className="font-medium text-toss-gray-900">
                    [필수] 위 내용을 확인하였으며 동의합니다
                  </span>
                  <p className="text-sm text-toss-gray-500 mt-1">
                    마케팅 목적 개인정보 제3자 제공에 동의합니다
                  </p>
                </div>
              </label>

              <div className="flex gap-3 mt-6">
                <button
                  onClick={() => setStep(1)}
                  className="btn-secondary flex-1"
                >
                  이전
                </button>
                <button
                  onClick={handleSubmit}
                  disabled={!formData.agreeThirdPartyProvision || isSubmitting}
                  className="btn-primary flex-1"
                >
                  {isSubmitting ? '처리중...' : '동의하기'}
                </button>
              </div>
            </motion.div>
          )}

          {/* Step 3: 완료 */}
          {step === 3 && result && (
            <motion.div
              key="step3"
              initial={{ opacity: 0, scale: 0.95 }}
              animate={{ opacity: 1, scale: 1 }}
              className="card text-center"
            >
              <div className="w-20 h-20 mx-auto bg-toss-green-50 rounded-full flex items-center justify-center text-4xl mb-6">
                ✅
              </div>
              
              <h1 className="text-2xl font-bold text-toss-gray-900 mb-2">
                동의가 완료되었습니다!
              </h1>
              <p className="text-toss-gray-600 mb-6">
                빠른 시일 내에 전문 상담사가 연락드리겠습니다
              </p>

              <div className="bg-toss-gray-50 rounded-toss p-5 mb-6 text-left">
                <div className="space-y-3 text-sm">
                  <div className="flex justify-between">
                    <span className="text-toss-gray-500">신청 번호</span>
                    <span className="font-medium text-toss-gray-900">{result.requestId}</span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-toss-gray-500">상담 상품</span>
                    <span className="font-medium text-toss-gray-900">{decodedProductName}</span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-toss-gray-500">정보 보유 기한</span>
                    <span className="font-medium text-toss-blue-500">{result.retentionUntil}</span>
                  </div>
                </div>
              </div>

              <div className="bg-toss-blue-50 rounded-toss p-4 mb-6">
                <p className="text-sm text-toss-blue-700">
                  📌 개인정보는 보유 기한 만료 후 자동으로 파기됩니다.
                  <br />
                  언제든지 '내 동의 내역'에서 동의를 철회할 수 있습니다.
                </p>
              </div>

              <div className="flex gap-3">
                <button
                  onClick={() => navigate('/my-consent')}
                  className="btn-secondary flex-1"
                >
                  내 동의 내역
                </button>
                <button
                  onClick={() => navigate('/')}
                  className="btn-primary flex-1"
                >
                  홈으로
                </button>
              </div>
            </motion.div>
          )}
        </AnimatePresence>
      </div>
    </div>
  );
}
