import { useState, useEffect } from 'react';
import { motion } from 'framer-motion';
import { format } from 'date-fns';
import toast from 'react-hot-toast';
import { adminApi } from '../../services/api';

export default function AdminDestruction() {
  const [pendingInfo, setPendingInfo] = useState<{
    pendingCount: number;
    nextScheduledAt: string;
  } | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [isExecuting, setIsExecuting] = useState(false);
  const [recentDestructions, setRecentDestructions] = useState<
    { date: string; count: number; type: string }[]
  >([]);

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    setIsLoading(true);
    try {
      const data = await adminApi.getPendingDestroyCount();
      setPendingInfo(data);
    } catch (error) {
      console.error('파기 정보 로딩 실패:', error);
      // 데모 데이터
      setPendingInfo({
        pendingCount: 15,
        nextScheduledAt: new Date(
          new Date().setHours(24, 0, 0, 0)
        ).toISOString(),
      });
    }

    // 데모 최근 파기 이력
    setRecentDestructions([
      { date: format(new Date(), 'yyyy-MM-dd'), count: 8, type: '자동' },
      {
        date: format(new Date(Date.now() - 86400000), 'yyyy-MM-dd'),
        count: 12,
        type: '자동',
      },
      {
        date: format(new Date(Date.now() - 86400000 * 2), 'yyyy-MM-dd'),
        count: 5,
        type: '수동',
      },
      {
        date: format(new Date(Date.now() - 86400000 * 3), 'yyyy-MM-dd'),
        count: 10,
        type: '자동',
      },
    ]);

    setIsLoading(false);
  };

  const handleExecuteDestroy = async () => {
    if (
      !confirm(
        '수동 파기를 실행하시겠습니까?\n\n보유기간이 만료된 모든 개인정보가 즉시 마스킹 처리됩니다.\n이 작업은 되돌릴 수 없습니다.'
      )
    ) {
      return;
    }

    setIsExecuting(true);
    try {
      const result = await adminApi.executeDestroy();
      toast.success(`${result.destroyedCount}건의 개인정보가 파기되었습니다.`);
      loadData();
    } catch (error) {
      toast.error(
        error instanceof Error ? error.message : '파기 실행에 실패했습니다.'
      );
    } finally {
      setIsExecuting(false);
    }
  };

  if (isLoading) {
    return (
      <div className="space-y-6">
        <div className="card h-48 skeleton" />
        <div className="card h-64 skeleton" />
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* 헤더 */}
      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
      >
        <h2 className="text-xl font-bold text-toss-gray-900">개인정보 파기 관리</h2>
        <p className="text-sm text-toss-gray-500 mt-1">
          보유기간이 만료된 개인정보를 관리하고 파기합니다
        </p>
      </motion.div>

      {/* 파기 예정 현황 */}
      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ delay: 0.1 }}
        className="card"
      >
        <div className="flex items-start justify-between">
          <div>
            <h3 className="text-lg font-semibold text-toss-gray-900 mb-2">
              파기 예정 현황
            </h3>
            <p className="text-sm text-toss-gray-500">
              보유기간이 만료되어 파기 대상인 개인정보 건수입니다
            </p>
          </div>
          <button
            onClick={handleExecuteDestroy}
            disabled={isExecuting || (pendingInfo?.pendingCount || 0) === 0}
            className="btn-danger px-6"
          >
            {isExecuting ? '처리중...' : '수동 파기 실행'}
          </button>
        </div>

        <div className="grid grid-cols-2 gap-6 mt-6">
          <div className="bg-toss-red-50 rounded-toss p-6">
            <div className="flex items-center gap-3 mb-2">
              <span className="text-2xl">🗑️</span>
              <span className="text-sm text-toss-gray-600">파기 예정 건수</span>
            </div>
            <p className="text-3xl font-bold text-toss-red-500">
              {pendingInfo?.pendingCount || 0}
              <span className="text-lg font-normal text-toss-gray-500 ml-1">건</span>
            </p>
          </div>

          <div className="bg-toss-blue-50 rounded-toss p-6">
            <div className="flex items-center gap-3 mb-2">
              <span className="text-2xl">⏰</span>
              <span className="text-sm text-toss-gray-600">다음 자동 파기</span>
            </div>
            <p className="text-xl font-bold text-toss-blue-500">
              {pendingInfo?.nextScheduledAt
                ? format(new Date(pendingInfo.nextScheduledAt), 'MM월 dd일 HH:mm')
                : '-'}
            </p>
          </div>
        </div>
      </motion.div>

      {/* 파기 정책 안내 */}
      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ delay: 0.2 }}
        className="card bg-toss-gray-50"
      >
        <h3 className="text-lg font-semibold text-toss-gray-900 mb-4">
          📋 개인정보 파기 정책
        </h3>
        <div className="space-y-4">
          <div className="flex items-start gap-3">
            <div className="w-6 h-6 bg-toss-blue-100 rounded-full flex items-center justify-center text-sm flex-shrink-0 mt-0.5">
              1
            </div>
            <div>
              <p className="font-medium text-toss-gray-900">자동 파기</p>
              <p className="text-sm text-toss-gray-600">
                매일 자정(00:00)에 보유기간이 만료된 개인정보가 자동으로 마스킹 처리됩니다.
              </p>
            </div>
          </div>
          <div className="flex items-start gap-3">
            <div className="w-6 h-6 bg-toss-blue-100 rounded-full flex items-center justify-center text-sm flex-shrink-0 mt-0.5">
              2
            </div>
            <div>
              <p className="font-medium text-toss-gray-900">보유 기간</p>
              <p className="text-sm text-toss-gray-600">
                상담 완료 후 3개월간 보유하며, 보유기간 만료 시 개인정보(이름, 연락처, CI)가 마스킹(***) 처리됩니다.
              </p>
            </div>
          </div>
          <div className="flex items-start gap-3">
            <div className="w-6 h-6 bg-toss-blue-100 rounded-full flex items-center justify-center text-sm flex-shrink-0 mt-0.5">
              3
            </div>
            <div>
              <p className="font-medium text-toss-gray-900">동의 철회 시</p>
              <p className="text-sm text-toss-gray-600">
                고객이 동의를 철회할 경우 즉시 개인정보가 파기됩니다.
              </p>
            </div>
          </div>
          <div className="flex items-start gap-3">
            <div className="w-6 h-6 bg-toss-blue-100 rounded-full flex items-center justify-center text-sm flex-shrink-0 mt-0.5">
              4
            </div>
            <div>
              <p className="font-medium text-toss-gray-900">파기 로그</p>
              <p className="text-sm text-toss-gray-600">
                모든 파기 내역은 destruction_logs 테이블에 기록되어 감사 추적이 가능합니다.
              </p>
            </div>
          </div>
        </div>
      </motion.div>

      {/* 최근 파기 이력 */}
      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ delay: 0.3 }}
        className="card"
      >
        <h3 className="text-lg font-semibold text-toss-gray-900 mb-4">
          최근 파기 이력
        </h3>
        <div className="space-y-3">
          {recentDestructions.map((item, index) => (
            <div
              key={index}
              className="flex items-center justify-between p-4 bg-toss-gray-50 rounded-toss-sm"
            >
              <div className="flex items-center gap-3">
                <div
                  className={`w-10 h-10 rounded-full flex items-center justify-center ${
                    item.type === '자동'
                      ? 'bg-blue-100 text-blue-600'
                      : 'bg-purple-100 text-purple-600'
                  }`}
                >
                  {item.type === '자동' ? '⚙️' : '👤'}
                </div>
                <div>
                  <p className="font-medium text-toss-gray-900">{item.date}</p>
                  <p className="text-sm text-toss-gray-500">{item.type} 파기</p>
                </div>
              </div>
              <div className="text-right">
                <p className="text-lg font-bold text-toss-gray-900">{item.count}건</p>
                <p className="text-sm text-toss-gray-500">개인정보 파기</p>
              </div>
            </div>
          ))}
        </div>
      </motion.div>

      {/* 경고 안내 */}
      <motion.div
        initial={{ opacity: 0 }}
        animate={{ opacity: 1 }}
        transition={{ delay: 0.4 }}
        className="bg-red-50 border border-red-200 rounded-toss p-4"
      >
        <div className="flex items-start gap-3">
          <span className="text-xl">⚠️</span>
          <div className="text-sm">
            <p className="font-medium text-red-900 mb-1">주의사항</p>
            <p className="text-red-700">
              수동 파기 실행 시 보유기간이 만료된 모든 개인정보가 즉시 마스킹 처리됩니다.
              이 작업은 되돌릴 수 없으며, 파기된 개인정보는 복구할 수 없습니다.
              자동 파기 시스템이 정상 작동하고 있다면 수동 파기는 필요하지 않습니다.
            </p>
          </div>
        </div>
      </motion.div>
    </div>
  );
}
