import { useState, useEffect } from 'react';
import { motion } from 'framer-motion';
import {
  AreaChart,
  Area,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
  BarChart,
  Bar,
  PieChart,
  Pie,
  Cell,
} from 'recharts';
import { adminApi } from '../../services/api';
import type { DashboardStats } from '../../types';

export default function AdminDashboard() {
  const [stats, setStats] = useState<DashboardStats | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    loadStats();
  }, []);

  const loadStats = async () => {
    setIsLoading(true);
    try {
      const data = await adminApi.getDashboardStats();
      setStats(data);
    } catch (error) {
      console.error('통계 로딩 실패:', error);
      // 데모 데이터 설정
      setStats({
        totalRequests: 156,
        pendingRequests: 23,
        inProgressRequests: 45,
        completedRequests: 78,
        withdrawnRequests: 10,
        todayConsents: 12,
        todayCalls: 34,
        todayCompletions: 28,
        callResultStats: {
          SUCCESS: 78,
          NO_ANSWER: 23,
          CALLBACK: 15,
          REFUSED: 8,
          OTHER: 6,
        },
        productStats: {
          '루키즈 카드': {
            productName: '루키즈 카드',
            totalCount: 80,
            agreedCount: 45,
            pendingCount: 20,
            refusedCount: 15,
            conversionRate: 56.25,
          },
        },
        agentStats: [
          { agentId: 1, agentName: '김상담', totalCalls: 45, successCalls: 30, callbackCalls: 10, successRate: 66.7, avgCallDuration: 180 },
          { agentId: 2, agentName: '이상담', totalCalls: 38, successCalls: 28, callbackCalls: 5, successRate: 73.7, avgCallDuration: 150 },
          { agentId: 3, agentName: '박상담', totalCalls: 42, successCalls: 20, callbackCalls: 12, successRate: 47.6, avgCallDuration: 200 },
        ],
        dailyStats: [
          { date: '01-21', consents: 8, calls: 25, completions: 20 },
          { date: '01-22', consents: 12, calls: 32, completions: 28 },
          { date: '01-23', consents: 6, calls: 28, completions: 22 },
          { date: '01-24', consents: 15, calls: 38, completions: 30 },
          { date: '01-25', consents: 10, calls: 30, completions: 26 },
          { date: '01-26', consents: 14, calls: 35, completions: 32 },
          { date: '01-27', consents: 12, calls: 34, completions: 28 },
        ],
      });
    } finally {
      setIsLoading(false);
    }
  };

  const COLORS = ['#3182F6', '#30C85E', '#FFAA00', '#F04452', '#8B95A1'];

  const statCards = stats ? [
    { label: '전체 요청', value: stats.totalRequests, icon: '📋', color: 'bg-blue-50 text-blue-600' },
    { label: '대기중', value: stats.pendingRequests, icon: '⏳', color: 'bg-yellow-50 text-yellow-600' },
    { label: '진행중', value: stats.inProgressRequests, icon: '📞', color: 'bg-purple-50 text-purple-600' },
    { label: '완료', value: stats.completedRequests, icon: '✅', color: 'bg-green-50 text-green-600' },
  ] : [];

  const todayCards = stats ? [
    { label: '오늘 동의', value: stats.todayConsents, change: '+15%' },
    { label: '오늘 상담', value: stats.todayCalls, change: '+8%' },
    { label: '오늘 완료', value: stats.todayCompletions, change: '+12%' },
  ] : [];

  if (isLoading) {
    return (
      <div className="space-y-6">
        <div className="grid grid-cols-4 gap-4">
          {[1, 2, 3, 4].map((i) => (
            <div key={i} className="card h-24 skeleton" />
          ))}
        </div>
        <div className="grid grid-cols-2 gap-6">
          <div className="card h-80 skeleton" />
          <div className="card h-80 skeleton" />
        </div>
      </div>
    );
  }

  if (!stats) return null;

  const callResultData = Object.entries(stats.callResultStats).map(([name, value]) => ({
    name: {
      SUCCESS: '성공',
      NO_ANSWER: '부재중',
      CALLBACK: '재통화',
      REFUSED: '거부',
      OTHER: '기타',
    }[name] || name,
    value,
  }));

  return (
    <div className="space-y-6">
      {/* 상단 통계 카드 */}
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
        {statCards.map((stat, index) => (
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
            <p className="text-2xl font-bold text-toss-gray-900">{stat.value.toLocaleString()}</p>
          </motion.div>
        ))}
      </div>

      {/* 오늘 통계 */}
      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ delay: 0.2 }}
        className="card bg-gradient-to-r from-toss-blue-500 to-toss-blue-600 text-white"
      >
        <h3 className="text-lg font-semibold mb-4">오늘의 현황</h3>
        <div className="grid grid-cols-3 gap-6">
          {todayCards.map((card) => (
            <div key={card.label}>
              <p className="text-blue-100 text-sm">{card.label}</p>
              <div className="flex items-baseline gap-2">
                <span className="text-3xl font-bold">{card.value}</span>
                <span className="text-sm text-green-300">{card.change}</span>
              </div>
            </div>
          ))}
        </div>
      </motion.div>

      {/* 차트 영역 */}
      <div className="grid lg:grid-cols-2 gap-6">
        {/* 일자별 추이 */}
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.3 }}
          className="card"
        >
          <h3 className="text-lg font-semibold text-toss-gray-900 mb-4">일자별 추이</h3>
          <div className="h-64">
            <ResponsiveContainer width="100%" height="100%">
              <AreaChart data={stats.dailyStats}>
                <CartesianGrid strokeDasharray="3 3" stroke="#E5E8EB" />
                <XAxis dataKey="date" stroke="#8B95A1" fontSize={12} />
                <YAxis stroke="#8B95A1" fontSize={12} />
                <Tooltip
                  contentStyle={{
                    backgroundColor: '#fff',
                    border: 'none',
                    borderRadius: '12px',
                    boxShadow: '0 4px 16px rgba(0,0,0,0.12)',
                  }}
                />
                <Area
                  type="monotone"
                  dataKey="consents"
                  name="동의"
                  stackId="1"
                  stroke="#3182F6"
                  fill="#3182F6"
                  fillOpacity={0.3}
                />
                <Area
                  type="monotone"
                  dataKey="calls"
                  name="상담"
                  stackId="2"
                  stroke="#30C85E"
                  fill="#30C85E"
                  fillOpacity={0.3}
                />
              </AreaChart>
            </ResponsiveContainer>
          </div>
        </motion.div>

        {/* 상담 결과 분포 */}
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.4 }}
          className="card"
        >
          <h3 className="text-lg font-semibold text-toss-gray-900 mb-4">상담 결과 분포</h3>
          <div className="h-64 flex items-center">
            <ResponsiveContainer width="50%" height="100%">
              <PieChart>
                <Pie
                  data={callResultData}
                  cx="50%"
                  cy="50%"
                  innerRadius={50}
                  outerRadius={80}
                  paddingAngle={2}
                  dataKey="value"
                >
                  {callResultData.map((_, index) => (
                    <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                  ))}
                </Pie>
                <Tooltip />
              </PieChart>
            </ResponsiveContainer>
            <div className="flex-1 space-y-2">
              {callResultData.map((item, index) => (
                <div key={item.name} className="flex items-center gap-2">
                  <div
                    className="w-3 h-3 rounded-full"
                    style={{ backgroundColor: COLORS[index % COLORS.length] }}
                  />
                  <span className="text-sm text-toss-gray-600">{item.name}</span>
                  <span className="text-sm font-medium text-toss-gray-900 ml-auto">
                    {item.value}건
                  </span>
                </div>
              ))}
            </div>
          </div>
        </motion.div>
      </div>

      {/* 상담사별 성과 */}
      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ delay: 0.5 }}
        className="card"
      >
        <h3 className="text-lg font-semibold text-toss-gray-900 mb-4">상담사별 성과</h3>
        <div className="overflow-x-auto">
          <table className="w-full">
            <thead>
              <tr className="border-b border-toss-gray-200">
                <th className="text-left py-3 px-4 text-sm font-medium text-toss-gray-500">상담사</th>
                <th className="text-right py-3 px-4 text-sm font-medium text-toss-gray-500">총 상담</th>
                <th className="text-right py-3 px-4 text-sm font-medium text-toss-gray-500">성공</th>
                <th className="text-right py-3 px-4 text-sm font-medium text-toss-gray-500">재통화</th>
                <th className="text-right py-3 px-4 text-sm font-medium text-toss-gray-500">성공률</th>
                <th className="text-right py-3 px-4 text-sm font-medium text-toss-gray-500">평균 통화</th>
              </tr>
            </thead>
            <tbody>
              {stats.agentStats.map((agent, index) => (
                <tr key={agent.agentId} className={index % 2 === 0 ? 'bg-toss-gray-50' : ''}>
                  <td className="py-3 px-4">
                    <div className="flex items-center gap-3">
                      <div className="w-8 h-8 bg-toss-blue-100 rounded-full flex items-center justify-center text-sm">
                        👤
                      </div>
                      <span className="font-medium text-toss-gray-900">{agent.agentName}</span>
                    </div>
                  </td>
                  <td className="text-right py-3 px-4 text-toss-gray-900">{agent.totalCalls}</td>
                  <td className="text-right py-3 px-4 text-toss-green-500 font-medium">{agent.successCalls}</td>
                  <td className="text-right py-3 px-4 text-toss-yellow-500">{agent.callbackCalls}</td>
                  <td className="text-right py-3 px-4">
                    <span className={`font-medium ${
                      agent.successRate >= 60 ? 'text-toss-green-500' : 
                      agent.successRate >= 40 ? 'text-toss-yellow-500' : 'text-toss-red-500'
                    }`}>
                      {agent.successRate.toFixed(1)}%
                    </span>
                  </td>
                  <td className="text-right py-3 px-4 text-toss-gray-600">
                    {Math.floor(agent.avgCallDuration / 60)}분 {agent.avgCallDuration % 60}초
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </motion.div>
    </div>
  );
}
