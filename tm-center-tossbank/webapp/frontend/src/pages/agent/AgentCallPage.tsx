import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { motion, AnimatePresence } from 'framer-motion';
import toast from 'react-hot-toast';
import { tmApi } from '../../services/api';
import type { TmTarget, CallResultRequest, ResultCode, ProductResult } from '../../types';

export default function AgentCallPage() {
  const { targetId } = useParams<{ targetId: string }>();
  const navigate = useNavigate();
  
  const [target, setTarget] = useState<TmTarget | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [isCallStarted, setIsCallStarted] = useState(false);
  const [showResultForm, setShowResultForm] = useState(false);
  const [isSaving, setIsSaving] = useState(false);
  
  const [formData, setFormData] = useState<{
    recordingAgreed: boolean;
    resultCode: ResultCode | '';
    resultDetail: string;
    productResult: ProductResult | '';
    callbackScheduledAt: string;
    retryAgreed: boolean;
    memo: string;
  }>({
    recordingAgreed: false,
    resultCode: '',
    resultDetail: '',
    productResult: '',
    callbackScheduledAt: '',
    retryAgreed: false,
    memo: '',
  });

  useEffect(() => {
    if (targetId) {
      loadTarget(Number(targetId));
    }
  }, [targetId]);

  const loadTarget = async (id: number) => {
    setIsLoading(true);
    try {
      const data = await tmApi.getTargetDetail(id);
      setTarget(data);
    } catch (error) {
      toast.error(error instanceof Error ? error.message : '고객 정보를 불러올 수 없습니다.');
      navigate('/agent');
    } finally {
      setIsLoading(false);
    }
  };

  const handleGetNextTarget = async () => {
    setIsLoading(true);
    try {
      const data = await tmApi.getNextTarget();
      if (data) {
        setTarget(data);
        toast.success('새로운 고객이 배정되었습니다.');
      } else {
        toast('대기중인 고객이 없습니다.', { icon: '📋' });
      }
    } catch (error) {
      toast.error(error instanceof Error ? error.message : '고객을 불러올 수 없습니다.');
    } finally {
      setIsLoading(false);
    }
  };

  const handleStartCall = async () => {
    if (!target) return;
    
    try {
      await tmApi.startCall(target.id);
      setIsCallStarted(true);
      toast.success('상담이 시작되었습니다.');
    } catch (error) {
      toast.error(error instanceof Error ? error.message : '상담 시작에 실패했습니다.');
    }
  };

  const handleSaveResult = async () => {
    if (!target || !formData.resultCode) {
      toast.error('상담 결과를 선택해 주세요.');
      return;
    }

    setIsSaving(true);
    try {
      const request: CallResultRequest = {
        tmTargetId: target.id,
        recordingAgreed: formData.recordingAgreed,
        resultCode: formData.resultCode as ResultCode,
        resultDetail: formData.resultDetail || undefined,
        consultedProduct: target.productName,
        productResult: formData.productResult as ProductResult || undefined,
        callbackScheduledAt: formData.callbackScheduledAt || undefined,
        retryAgreed: formData.retryAgreed,
        memo: formData.memo || undefined,
      };

      await tmApi.saveCallResult(request);
      toast.success('상담 결과가 저장되었습니다.');
      
      // 초기화 및 다음 고객 안내
      setTarget(null);
      setIsCallStarted(false);
      setShowResultForm(false);
      setFormData({
        recordingAgreed: false,
        resultCode: '',
        resultDetail: '',
        productResult: '',
        callbackScheduledAt: '',
        retryAgreed: false,
        memo: '',
      });
    } catch (error) {
      toast.error(error instanceof Error ? error.message : '저장에 실패했습니다.');
    } finally {
      setIsSaving(false);
    }
  };

  const resultCodes: { value: ResultCode; label: string; icon: string }[] = [
    { value: 'SUCCESS', label: '상담 성공', icon: '✅' },
    { value: 'NO_ANSWER', label: '부재중', icon: '📵' },
    { value: 'BUSY', label: '통화중', icon: '📞' },
    { value: 'CALLBACK', label: '재통화 요청', icon: '🔄' },
    { value: 'REFUSED', label: '상담 거부', icon: '❌' },
    { value: 'WRONG_NUMBER', label: '잘못된 번호', icon: '⚠️' },
    { value: 'OTHER', label: '기타', icon: '📝' },
  ];

  const productResults: { value: ProductResult; label: string }[] = [
    { value: 'AGREED', label: '가입 동의' },
    { value: 'PENDING', label: '검토중/보류' },
    { value: 'REFUSED', label: '거절' },
  ];

  if (isLoading) {
    return (
      <div className="flex items-center justify-center h-96">
        <div className="text-center">
          <div className="w-12 h-12 border-4 border-toss-blue-500 border-t-transparent rounded-full animate-spin mx-auto mb-4" />
          <p className="text-toss-gray-600">로딩 중...</p>
        </div>
      </div>
    );
  }

  // 고객이 없을 때
  if (!target) {
    return (
      <div className="max-w-2xl mx-auto">
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          className="card text-center py-12"
        >
          <div className="w-20 h-20 mx-auto bg-toss-gray-100 rounded-full flex items-center justify-center text-4xl mb-6">
            📞
          </div>
          <h2 className="text-2xl font-bold text-toss-gray-900 mb-3">
            상담할 고객을 불러오세요
          </h2>
          <p className="text-toss-gray-600 mb-6">
            다음 고객 불러오기 버튼을 클릭하여<br />
            대기중인 고객을 배정받으세요
          </p>
          <button
            onClick={handleGetNextTarget}
            className="btn-primary px-8"
          >
            다음 고객 불러오기
          </button>
        </motion.div>
      </div>
    );
  }

  return (
    <div className="max-w-4xl mx-auto space-y-6">
      {/* 경고 메시지 */}
      <motion.div
        initial={{ opacity: 0, y: -10 }}
        animate={{ opacity: 1, y: 0 }}
        className="bg-yellow-50 border border-yellow-200 rounded-toss p-4"
      >
        <div className="flex items-start gap-3">
          <span className="text-xl">⚠️</span>
          <div>
            <p className="font-medium text-yellow-800">{target.warningMessage}</p>
          </div>
        </div>
      </motion.div>

      {/* 고객 정보 카드 */}
      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        className="card"
      >
        <div className="flex items-start justify-between mb-6">
          <div className="flex items-center gap-4">
            <div className="w-14 h-14 bg-toss-blue-100 rounded-full flex items-center justify-center text-2xl">
              👤
            </div>
            <div>
              <h2 className="text-xl font-bold text-toss-gray-900">{target.customerName}</h2>
              <p className="text-toss-gray-600">{target.phone}</p>
            </div>
          </div>
          <div className="text-right">
            <span className={`badge ${target.status === 'IN_CALL' ? 'badge-yellow' : 'badge-blue'}`}>
              {target.status === 'IN_CALL' ? '통화중' : '배정됨'}
            </span>
          </div>
        </div>

        <div className="grid grid-cols-2 gap-4 p-4 bg-toss-gray-50 rounded-toss-sm">
          <div>
            <span className="text-sm text-toss-gray-500">상담 상품</span>
            <p className="font-medium text-toss-gray-900">{target.productName}</p>
          </div>
          <div>
            <span className="text-sm text-toss-gray-500">동의 목적</span>
            <p className="font-medium text-toss-gray-900">{target.consentPurpose}</p>
          </div>
          <div>
            <span className="text-sm text-toss-gray-500">정보 보유 기한</span>
            <p className="font-medium text-toss-blue-500">{target.retentionUntil}</p>
          </div>
          <div>
            <span className="text-sm text-toss-gray-500">요청 번호</span>
            <p className="font-medium text-toss-gray-900">{target.externalRef}</p>
          </div>
        </div>

        {/* 상담 시작 전 */}
        {!isCallStarted && (
          <div className="mt-6 pt-6 border-t border-toss-gray-200">
            <div className="bg-blue-50 rounded-toss-sm p-4 mb-4">
              <h4 className="font-semibold text-blue-900 mb-2">📋 상담 시작 전 확인</h4>
              <p className="text-sm text-blue-800">
                "안녕하세요 고객님, ○○은행입니다.<br />
                <strong>본 통화는 서비스 품질 향상을 위해 녹음됩니다. 동의하십니까?</strong>"
              </p>
            </div>
            <button
              onClick={handleStartCall}
              className="btn-primary w-full"
            >
              📞 상담 시작
            </button>
          </div>
        )}

        {/* 상담 진행 중 */}
        {isCallStarted && !showResultForm && (
          <div className="mt-6 pt-6 border-t border-toss-gray-200">
            <div className="flex items-center justify-center gap-3 mb-6">
              <div className="w-3 h-3 bg-green-500 rounded-full animate-pulse" />
              <span className="text-lg font-medium text-toss-gray-900">상담 진행 중</span>
            </div>
            <button
              onClick={() => setShowResultForm(true)}
              className="btn-secondary w-full"
            >
              상담 종료 및 결과 입력
            </button>
          </div>
        )}
      </motion.div>

      {/* 상담 결과 입력 폼 */}
      <AnimatePresence>
        {showResultForm && (
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0, y: -20 }}
            className="card"
          >
            <h3 className="text-lg font-semibold text-toss-gray-900 mb-6">상담 결과 입력</h3>

            {/* 녹취 동의 */}
            <div className="mb-6">
              <label className="flex items-center gap-3 p-4 bg-toss-gray-50 rounded-toss-sm cursor-pointer">
                <input
                  type="checkbox"
                  className="checkbox-toss"
                  checked={formData.recordingAgreed}
                  onChange={(e) =>
                    setFormData({ ...formData, recordingAgreed: e.target.checked })
                  }
                />
                <span className="font-medium text-toss-gray-900">
                  고객이 녹취에 동의함
                </span>
              </label>
            </div>

            {/* 상담 결과 코드 */}
            <div className="mb-6">
              <label className="label-toss">상담 결과</label>
              <div className="grid grid-cols-2 sm:grid-cols-4 gap-2">
                {resultCodes.map((code) => (
                  <button
                    key={code.value}
                    type="button"
                    onClick={() => setFormData({ ...formData, resultCode: code.value })}
                    className={`p-3 rounded-toss-sm border-2 text-center transition-all ${
                      formData.resultCode === code.value
                        ? 'border-toss-blue-500 bg-toss-blue-50'
                        : 'border-toss-gray-200 hover:border-toss-gray-300'
                    }`}
                  >
                    <span className="text-xl block mb-1">{code.icon}</span>
                    <span className="text-sm font-medium">{code.label}</span>
                  </button>
                ))}
              </div>
            </div>

            {/* 상품 상담 결과 (상담 성공 시) */}
            {formData.resultCode === 'SUCCESS' && (
              <div className="mb-6">
                <label className="label-toss">상품 상담 결과</label>
                <div className="flex gap-2">
                  {productResults.map((result) => (
                    <button
                      key={result.value}
                      type="button"
                      onClick={() => setFormData({ ...formData, productResult: result.value })}
                      className={`flex-1 p-3 rounded-toss-sm border-2 text-center transition-all ${
                        formData.productResult === result.value
                          ? 'border-toss-blue-500 bg-toss-blue-50'
                          : 'border-toss-gray-200 hover:border-toss-gray-300'
                      }`}
                    >
                      <span className="font-medium">{result.label}</span>
                    </button>
                  ))}
                </div>
              </div>
            )}

            {/* 재통화 예정일 (CALLBACK 선택 시) */}
            {formData.resultCode === 'CALLBACK' && (
              <div className="mb-6">
                <label className="label-toss">재통화 예정일</label>
                <input
                  type="datetime-local"
                  className="input-toss"
                  value={formData.callbackScheduledAt}
                  onChange={(e) =>
                    setFormData({ ...formData, callbackScheduledAt: e.target.value })
                  }
                />
              </div>
            )}

            {/* 메모 */}
            <div className="mb-6">
              <label className="label-toss">상담 메모 (선택)</label>
              <textarea
                className="input-toss h-24 resize-none"
                placeholder="상담 내용을 메모하세요"
                value={formData.memo}
                onChange={(e) => setFormData({ ...formData, memo: e.target.value })}
              />
            </div>

            {/* 버튼 */}
            <div className="flex gap-3">
              <button
                onClick={() => setShowResultForm(false)}
                className="btn-secondary flex-1"
              >
                취소
              </button>
              <button
                onClick={handleSaveResult}
                disabled={!formData.resultCode || isSaving}
                className="btn-primary flex-1"
              >
                {isSaving ? '저장 중...' : '결과 저장'}
              </button>
            </div>
          </motion.div>
        )}
      </AnimatePresence>

      {/* 하단 액션 */}
      {!showResultForm && (
        <div className="flex gap-3">
          <button
            onClick={() => {
              setTarget(null);
              setIsCallStarted(false);
            }}
            className="btn-outline flex-1"
          >
            다른 고객 선택
          </button>
          <button
            onClick={handleGetNextTarget}
            className="btn-secondary flex-1"
          >
            다음 고객 불러오기
          </button>
        </div>
      )}
    </div>
  );
}
