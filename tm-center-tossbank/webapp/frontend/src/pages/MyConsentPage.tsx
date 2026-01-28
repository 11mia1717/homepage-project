import { useState } from 'react';
import { motion } from 'framer-motion';
import toast from 'react-hot-toast';
import { consentApi } from '../services/api';
import type { ConsentResponse } from '../types';

export default function MyConsentPage() {
  const [phone, setPhone] = useState('');
  const [consents, setConsents] = useState<ConsentResponse[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [hasSearched, setHasSearched] = useState(false);
  const [withdrawingId, setWithdrawingId] = useState<string | null>(null);

  const handleSearch = async () => {
    if (!phone) {
      toast.error('연락처를 입력해 주세요.');
      return;
    }

    setIsLoading(true);
    setHasSearched(true);
    try {
      const data = await consentApi.getConsentHistory(phone);
      setConsents(data);
    } catch (error) {
      toast.error(error instanceof Error ? error.message : '조회 중 오류가 발생했습니다.');
    } finally {
      setIsLoading(false);
    }
  };

  const handleWithdraw = async (requestId: string) => {
    if (!confirm('동의를 철회하시겠습니까?\n철회 시 개인정보가 즉시 파기되며 상담이 불가능합니다.')) {
      return;
    }

    setWithdrawingId(requestId);
    try {
      await consentApi.withdrawConsent(requestId, '고객 요청에 의한 철회');
      toast.success('동의가 철회되었습니다.');
      // 목록 새로고침
      handleSearch();
    } catch (error) {
      toast.error(error instanceof Error ? error.message : '철회 중 오류가 발생했습니다.');
    } finally {
      setWithdrawingId(null);
    }
  };

  const getStatusBadge = (status: string) => {
    const badges: Record<string, { label: string; className: string }> = {
      PENDING: { label: '대기중', className: 'badge-blue' },
      IN_PROGRESS: { label: '상담 진행중', className: 'badge-yellow' },
      COMPLETED: { label: '완료', className: 'badge-green' },
      WITHDRAWN: { label: '철회됨', className: 'badge-gray' },
      CANCELLED: { label: '취소', className: 'badge-red' },
    };
    const badge = badges[status] || { label: status, className: 'badge-gray' };
    return <span className={badge.className}>{badge.label}</span>;
  };

  return (
    <div className="min-h-[calc(100vh-4rem)] py-12">
      <div className="max-w-2xl mx-auto px-4">
        {/* 헤더 */}
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          className="text-center mb-8"
        >
          <h1 className="text-3xl font-bold text-toss-gray-900 mb-3">
            내 동의 내역
          </h1>
          <p className="text-toss-gray-600">
            마케팅 동의 내역을 확인하고 관리할 수 있습니다
          </p>
        </motion.div>

        {/* 검색 폼 */}
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.1 }}
          className="card mb-6"
        >
          <div className="flex gap-3">
            <input
              type="tel"
              className="input-toss flex-1"
              placeholder="연락처를 입력하세요 (예: 01012345678)"
              value={phone}
              onChange={(e) => setPhone(e.target.value)}
              onKeyDown={(e) => e.key === 'Enter' && handleSearch()}
            />
            <button
              onClick={handleSearch}
              disabled={isLoading}
              className="btn-primary px-8"
            >
              {isLoading ? '조회중...' : '조회'}
            </button>
          </div>
        </motion.div>

        {/* 결과 목록 */}
        {hasSearched && (
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: 0.2 }}
          >
            {consents.length === 0 ? (
              <div className="card text-center py-12">
                <div className="w-16 h-16 mx-auto bg-toss-gray-100 rounded-full flex items-center justify-center text-3xl mb-4">
                  📋
                </div>
                <h3 className="text-lg font-semibold text-toss-gray-900 mb-2">
                  동의 내역이 없습니다
                </h3>
                <p className="text-toss-gray-600">
                  해당 연락처로 신청한 동의 내역이 없습니다
                </p>
              </div>
            ) : (
              <div className="space-y-4">
                {consents.map((consent) => (
                  <motion.div
                    key={consent.requestId}
                    initial={{ opacity: 0, y: 10 }}
                    animate={{ opacity: 1, y: 0 }}
                    className="card"
                  >
                    <div className="flex items-start justify-between mb-4">
                      <div>
                        <h3 className="text-lg font-semibold text-toss-gray-900">
                          {consent.productName}
                        </h3>
                        <p className="text-sm text-toss-gray-500">
                          신청번호: {consent.requestId}
                        </p>
                      </div>
                      {getStatusBadge(consent.status)}
                    </div>

                    <div className="grid grid-cols-2 gap-4 text-sm mb-4">
                      <div>
                        <span className="text-toss-gray-500">신청일</span>
                        <p className="font-medium text-toss-gray-900">
                          {new Date(consent.consentedAt).toLocaleDateString('ko-KR')}
                        </p>
                      </div>
                      <div>
                        <span className="text-toss-gray-500">정보 보유 기한</span>
                        <p className="font-medium text-toss-blue-500">
                          {consent.retentionUntil}
                        </p>
                      </div>
                      <div>
                        <span className="text-toss-gray-500">제공받는 자</span>
                        <p className="font-medium text-toss-gray-900">
                          {consent.consentRecipient}
                        </p>
                      </div>
                      <div>
                        <span className="text-toss-gray-500">제공 목적</span>
                        <p className="font-medium text-toss-gray-900">
                          {consent.consentPurpose}
                        </p>
                      </div>
                    </div>

                    {/* 철회 버튼 (PENDING, IN_PROGRESS 상태에서만 표시) */}
                    {['PENDING', 'IN_PROGRESS'].includes(consent.status) && (
                      <div className="pt-4 border-t border-toss-gray-200">
                        <button
                          onClick={() => handleWithdraw(consent.requestId)}
                          disabled={withdrawingId === consent.requestId}
                          className="text-sm text-toss-red-500 hover:text-red-600 font-medium transition-colors disabled:opacity-50"
                        >
                          {withdrawingId === consent.requestId
                            ? '처리중...'
                            : '동의 철회하기'}
                        </button>
                      </div>
                    )}

                    {/* 철회됨 상태 표시 */}
                    {consent.status === 'WITHDRAWN' && (
                      <div className="pt-4 border-t border-toss-gray-200">
                        <p className="text-sm text-toss-gray-500">
                          ✓ 동의가 철회되어 개인정보가 파기되었습니다
                        </p>
                      </div>
                    )}

                    {/* 완료 상태 표시 */}
                    {consent.status === 'COMPLETED' && (
                      <div className="pt-4 border-t border-toss-gray-200">
                        <p className="text-sm text-toss-gray-500">
                          ✓ 상담이 완료되었습니다. 개인정보는 {consent.retentionUntil}에 자동 파기됩니다
                        </p>
                      </div>
                    )}
                  </motion.div>
                ))}
              </div>
            )}
          </motion.div>
        )}

        {/* 안내 문구 */}
        <motion.div
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          transition={{ delay: 0.3 }}
          className="mt-8 text-center text-sm text-toss-gray-500"
        >
          <p>
            동의 철회 시 개인정보가 즉시 파기되며, 상담 서비스를 받으실 수 없습니다.
          </p>
          <p className="mt-1">
            문의사항이 있으시면 고객센터 (02-1234-5678)로 연락해 주세요.
          </p>
        </motion.div>
      </div>
    </div>
  );
}
