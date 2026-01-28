import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { motion } from 'framer-motion';
import { tmApi } from '../../services/api';
import type { TmTarget } from '../../types';

export default function AgentDashboard() {
  const [targets, setTargets] = useState<TmTarget[]>([]);
  const [callbacks, setCallbacks] = useState<TmTarget[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    setIsLoading(true);
    try {
      const [targetsData, callbacksData] = await Promise.all([
        tmApi.getAssignedTargets(0, 5),
        tmApi.getCallbackTargets(),
      ]);
      setTargets(targetsData.content);
      setCallbacks(callbacksData);
    } catch (error) {
      console.error('데이터 로딩 실패:', error);
    } finally {
      setIsLoading(false);
    }
  };

  const getStatusBadge = (status: string) => {
    const badges: Record<string, { label: string; className: string }> = {
      WAITING: { label: '대기', className: 'badge-gray' },
      ASSIGNED: { label: '배정됨', className: 'badge-blue' },
      IN_CALL: { label: '통화중', className: 'badge-yellow' },
      COMPLETED: { label: '완료', className: 'badge-green' },
      CALLBACK: { label: '재통화', className: 'badge-yellow' },
      WITHDRAWN: { label: '철회', className: 'badge-red' },
    };
    const badge = badges[status] || { label: status, className: 'badge-gray' };
    return <span className={badge.className}>{badge.label}</span>;
  };

  const stats = [
    { label: '배정된 고객', value: targets.length, icon: '👥', color: 'bg-blue-50 text-blue-600' },
    { label: '재통화 대기', value: callbacks.length, icon: '📞', color: 'bg-yellow-50 text-yellow-600' },
    { label: '오늘 완료', value: 0, icon: '✅', color: 'bg-green-50 text-green-600' },
    { label: '성공률', value: '0%', icon: '📊', color: 'bg-purple-50 text-purple-600' },
  ];

  return (
    <div className="space-y-6">
      {/* 빠른 시작 */}
      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        className="card bg-gradient-to-r from-toss-blue-500 to-toss-blue-600 text-white"
      >
        <div className="flex items-center justify-between">
          <div>
            <h2 className="text-xl font-bold mb-2">상담 시작하기</h2>
            <p className="text-blue-100">다음 고객을 불러와서 상담을 시작하세요</p>
          </div>
          <Link
            to="/agent/call"
            className="px-6 py-3 bg-white text-toss-blue-500 font-semibold rounded-toss-sm hover:bg-blue-50 transition-colors"
          >
            상담하기
          </Link>
        </div>
      </motion.div>

      {/* 통계 카드 */}
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
        {stats.map((stat, index) => (
          <motion.div
            key={stat.label}
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: index * 0.1 }}
            className="card"
          >
            <div className={`w-10 h-10 ${stat.color} rounded-toss-sm flex items-center justify-center text-lg mb-3`}>
              {stat.icon}
            </div>
            <p className="text-sm text-toss-gray-500">{stat.label}</p>
            <p className="text-2xl font-bold text-toss-gray-900">{stat.value}</p>
          </motion.div>
        ))}
      </div>

      {/* 배정된 고객 목록 */}
      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ delay: 0.2 }}
        className="card"
      >
        <div className="flex items-center justify-between mb-4">
          <h3 className="text-lg font-semibold text-toss-gray-900">배정된 고객</h3>
          <Link to="/agent/call" className="text-sm text-toss-blue-500 hover:text-toss-blue-600">
            전체 보기 →
          </Link>
        </div>

        {isLoading ? (
          <div className="space-y-3">
            {[1, 2, 3].map((i) => (
              <div key={i} className="h-16 skeleton rounded-toss-sm" />
            ))}
          </div>
        ) : targets.length === 0 ? (
          <div className="text-center py-8">
            <p className="text-toss-gray-500">배정된 고객이 없습니다</p>
            <Link
              to="/agent/call"
              className="inline-block mt-3 text-toss-blue-500 hover:text-toss-blue-600"
            >
              다음 고객 불러오기
            </Link>
          </div>
        ) : (
          <div className="space-y-3">
            {targets.map((target) => (
              <Link
                key={target.id}
                to={`/agent/call/${target.id}`}
                className="flex items-center justify-between p-4 bg-toss-gray-50 rounded-toss-sm hover:bg-toss-gray-100 transition-colors"
              >
                <div className="flex items-center gap-3">
                  <div className="w-10 h-10 bg-white rounded-full flex items-center justify-center text-lg">
                    👤
                  </div>
                  <div>
                    <p className="font-medium text-toss-gray-900">{target.customerName}</p>
                    <p className="text-sm text-toss-gray-500">{target.productName}</p>
                  </div>
                </div>
                <div className="flex items-center gap-3">
                  {getStatusBadge(target.status)}
                  <svg className="w-5 h-5 text-toss-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
                  </svg>
                </div>
              </Link>
            ))}
          </div>
        )}
      </motion.div>

      {/* 재통화 대기 목록 */}
      {callbacks.length > 0 && (
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.3 }}
          className="card"
        >
          <div className="flex items-center gap-2 mb-4">
            <span className="text-xl">📞</span>
            <h3 className="text-lg font-semibold text-toss-gray-900">재통화 대기</h3>
            <span className="badge-yellow">{callbacks.length}</span>
          </div>

          <div className="space-y-3">
            {callbacks.slice(0, 3).map((target) => (
              <Link
                key={target.id}
                to={`/agent/call/${target.id}`}
                className="flex items-center justify-between p-4 bg-yellow-50 rounded-toss-sm hover:bg-yellow-100 transition-colors"
              >
                <div>
                  <p className="font-medium text-toss-gray-900">{target.customerName}</p>
                  <p className="text-sm text-toss-gray-500">{target.productName}</p>
                </div>
                <span className="text-sm text-yellow-600 font-medium">재통화 필요</span>
              </Link>
            ))}
          </div>
        </motion.div>
      )}

      {/* 상담 스크립트 안내 */}
      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ delay: 0.4 }}
        className="card bg-toss-gray-50 border border-toss-gray-200"
      >
        <div className="flex items-start gap-3">
          <div className="w-10 h-10 bg-toss-blue-100 rounded-full flex items-center justify-center">
            <span className="text-lg">📋</span>
          </div>
          <div>
            <h4 className="font-semibold text-toss-gray-900 mb-2">상담 시 필수 안내 사항</h4>
            <ul className="space-y-2 text-sm text-toss-gray-600">
              <li className="flex items-start gap-2">
                <span className="text-toss-blue-500">1.</span>
                <span>"본 통화는 서비스 품질 향상을 위해 녹음됩니다. 동의하십니까?"</span>
              </li>
              <li className="flex items-start gap-2">
                <span className="text-toss-blue-500">2.</span>
                <span>고객 정보는 동의한 상품 상담 목적으로만 사용</span>
              </li>
              <li className="flex items-start gap-2">
                <span className="text-toss-blue-500">3.</span>
                <span>재통화 시 "이전에 동의하신 마케팅 상담입니다" 재안내</span>
              </li>
            </ul>
          </div>
        </div>
      </motion.div>
    </div>
  );
}
