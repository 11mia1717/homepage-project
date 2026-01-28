import { useState, useEffect } from 'react';
import { motion } from 'framer-motion';
import { format, subDays } from 'date-fns';
import { adminApi } from '../../services/api';
import type { AccessLog } from '../../types';

export default function AdminAccessLogs() {
  const [logs, setLogs] = useState<AccessLog[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [totalElements, setTotalElements] = useState(0);
  const [page, setPage] = useState(0);
  const [dateRange, setDateRange] = useState({
    start: format(subDays(new Date(), 7), "yyyy-MM-dd'T'00:00:00"),
    end: format(new Date(), "yyyy-MM-dd'T'23:59:59"),
  });

  useEffect(() => {
    loadLogs();
  }, [page, dateRange]);

  const loadLogs = async () => {
    setIsLoading(true);
    try {
      const data = await adminApi.getAccessLogs(dateRange.start, dateRange.end, page, 20);
      setLogs(data.content);
      setTotalElements(data.totalElements);
    } catch (error) {
      console.error('접근 로그 로딩 실패:', error);
      // 데모 데이터
      setLogs([
        {
          id: 1,
          agentId: 1,
          agentName: '김상담',
          targetId: 101,
          targetType: 'TM_TARGET',
          accessType: 'VIEW',
          accessPurpose: '루키즈 카드 상담',
          ipAddress: '192.168.1.100',
          userAgent: 'Chrome/120.0',
          accessedAt: new Date().toISOString(),
        },
        {
          id: 2,
          agentId: 2,
          agentName: '이상담',
          targetId: 102,
          targetType: 'TM_TARGET',
          accessType: 'UPDATE',
          accessPurpose: '상담 결과 저장: SUCCESS',
          ipAddress: '192.168.1.101',
          userAgent: 'Chrome/120.0',
          accessedAt: new Date(Date.now() - 3600000).toISOString(),
        },
        {
          id: 3,
          agentId: 1,
          agentName: '김상담',
          targetId: 103,
          targetType: 'TM_TARGET',
          accessType: 'VIEW',
          accessPurpose: '토스 적금 상담',
          ipAddress: '192.168.1.100',
          userAgent: 'Chrome/120.0',
          accessedAt: new Date(Date.now() - 7200000).toISOString(),
        },
      ]);
      setTotalElements(3);
    } finally {
      setIsLoading(false);
    }
  };

  const getAccessTypeBadge = (type: string) => {
    const badges: Record<string, { label: string; className: string }> = {
      VIEW: { label: '조회', className: 'badge-blue' },
      CREATE: { label: '생성', className: 'badge-green' },
      UPDATE: { label: '수정', className: 'badge-yellow' },
      DELETE: { label: '삭제', className: 'badge-red' },
      EXPORT: { label: '내보내기', className: 'badge-gray' },
    };
    const badge = badges[type] || { label: type, className: 'badge-gray' };
    return <span className={badge.className}>{badge.label}</span>;
  };

  const getTargetTypeBadge = (type: string) => {
    const labels: Record<string, string> = {
      MARKETING_REQUEST: '마케팅 요청',
      TM_TARGET: 'TM 타겟',
      CALL_RESULT: '상담 결과',
    };
    return labels[type] || type;
  };

  return (
    <div className="space-y-6">
      {/* 헤더 */}
      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4"
      >
        <div>
          <h2 className="text-xl font-bold text-toss-gray-900">개인정보 접근 로그</h2>
          <p className="text-sm text-toss-gray-500 mt-1">
            상담사의 개인정보 접근 이력을 모니터링합니다
          </p>
        </div>
        <div className="flex items-center gap-3">
          <input
            type="date"
            className="input-toss text-sm py-2"
            value={dateRange.start.split('T')[0]}
            onChange={(e) =>
              setDateRange({
                ...dateRange,
                start: `${e.target.value}T00:00:00`,
              })
            }
          />
          <span className="text-toss-gray-400">~</span>
          <input
            type="date"
            className="input-toss text-sm py-2"
            value={dateRange.end.split('T')[0]}
            onChange={(e) =>
              setDateRange({
                ...dateRange,
                end: `${e.target.value}T23:59:59`,
              })
            }
          />
        </div>
      </motion.div>

      {/* 통계 카드 */}
      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ delay: 0.1 }}
        className="grid grid-cols-2 lg:grid-cols-4 gap-4"
      >
        <div className="card">
          <p className="text-sm text-toss-gray-500">전체 접근</p>
          <p className="text-2xl font-bold text-toss-gray-900">{totalElements.toLocaleString()}</p>
        </div>
        <div className="card">
          <p className="text-sm text-toss-gray-500">조회</p>
          <p className="text-2xl font-bold text-toss-blue-500">
            {logs.filter((l) => l.accessType === 'VIEW').length}
          </p>
        </div>
        <div className="card">
          <p className="text-sm text-toss-gray-500">수정</p>
          <p className="text-2xl font-bold text-toss-yellow-500">
            {logs.filter((l) => l.accessType === 'UPDATE').length}
          </p>
        </div>
        <div className="card">
          <p className="text-sm text-toss-gray-500">삭제</p>
          <p className="text-2xl font-bold text-toss-red-500">
            {logs.filter((l) => l.accessType === 'DELETE').length}
          </p>
        </div>
      </motion.div>

      {/* 로그 테이블 */}
      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ delay: 0.2 }}
        className="card overflow-hidden"
      >
        {isLoading ? (
          <div className="space-y-3">
            {[1, 2, 3, 4, 5].map((i) => (
              <div key={i} className="h-16 skeleton rounded-toss-sm" />
            ))}
          </div>
        ) : logs.length === 0 ? (
          <div className="text-center py-12">
            <div className="w-16 h-16 mx-auto bg-toss-gray-100 rounded-full flex items-center justify-center text-3xl mb-4">
              📋
            </div>
            <p className="text-toss-gray-500">접근 로그가 없습니다</p>
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full">
              <thead>
                <tr className="border-b border-toss-gray-200 bg-toss-gray-50">
                  <th className="text-left py-3 px-4 text-sm font-medium text-toss-gray-500">일시</th>
                  <th className="text-left py-3 px-4 text-sm font-medium text-toss-gray-500">상담사</th>
                  <th className="text-left py-3 px-4 text-sm font-medium text-toss-gray-500">유형</th>
                  <th className="text-left py-3 px-4 text-sm font-medium text-toss-gray-500">대상</th>
                  <th className="text-left py-3 px-4 text-sm font-medium text-toss-gray-500">목적</th>
                  <th className="text-left py-3 px-4 text-sm font-medium text-toss-gray-500">IP</th>
                </tr>
              </thead>
              <tbody>
                {logs.map((log, index) => (
                  <tr
                    key={log.id}
                    className={`border-b border-toss-gray-100 hover:bg-toss-gray-50 transition-colors ${
                      index % 2 === 0 ? '' : 'bg-toss-gray-50/50'
                    }`}
                  >
                    <td className="py-3 px-4 text-sm text-toss-gray-600">
                      {format(new Date(log.accessedAt), 'MM/dd HH:mm:ss')}
                    </td>
                    <td className="py-3 px-4">
                      <div className="flex items-center gap-2">
                        <div className="w-6 h-6 bg-toss-gray-200 rounded-full flex items-center justify-center text-xs">
                          👤
                        </div>
                        <span className="text-sm font-medium text-toss-gray-900">
                          {log.agentName}
                        </span>
                      </div>
                    </td>
                    <td className="py-3 px-4">
                      {getAccessTypeBadge(log.accessType)}
                    </td>
                    <td className="py-3 px-4 text-sm text-toss-gray-600">
                      {getTargetTypeBadge(log.targetType)} #{log.targetId}
                    </td>
                    <td className="py-3 px-4 text-sm text-toss-gray-900 max-w-xs truncate">
                      {log.accessPurpose}
                    </td>
                    <td className="py-3 px-4 text-sm text-toss-gray-500 font-mono">
                      {log.ipAddress}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}

        {/* 페이지네이션 */}
        {totalElements > 20 && (
          <div className="flex items-center justify-center gap-2 mt-4 pt-4 border-t border-toss-gray-200">
            <button
              onClick={() => setPage(Math.max(0, page - 1))}
              disabled={page === 0}
              className="px-3 py-1 text-sm rounded-toss-sm bg-toss-gray-100 text-toss-gray-600 hover:bg-toss-gray-200 disabled:opacity-50 disabled:cursor-not-allowed"
            >
              이전
            </button>
            <span className="text-sm text-toss-gray-600">
              {page + 1} / {Math.ceil(totalElements / 20)}
            </span>
            <button
              onClick={() => setPage(page + 1)}
              disabled={(page + 1) * 20 >= totalElements}
              className="px-3 py-1 text-sm rounded-toss-sm bg-toss-gray-100 text-toss-gray-600 hover:bg-toss-gray-200 disabled:opacity-50 disabled:cursor-not-allowed"
            >
              다음
            </button>
          </div>
        )}
      </motion.div>

      {/* 안내 문구 */}
      <motion.div
        initial={{ opacity: 0 }}
        animate={{ opacity: 1 }}
        transition={{ delay: 0.3 }}
        className="bg-toss-gray-50 rounded-toss p-4"
      >
        <div className="flex items-start gap-3">
          <span className="text-xl">ℹ️</span>
          <div className="text-sm text-toss-gray-600">
            <p className="font-medium text-toss-gray-900 mb-1">접근 로그 안내</p>
            <p>
              개인정보보호법에 따라 상담사가 개인정보에 접근한 모든 이력이 기록됩니다.
              비정상적인 접근 패턴이 감지될 경우 즉시 보안 담당자에게 문의하세요.
            </p>
          </div>
        </div>
      </motion.div>
    </div>
  );
}
