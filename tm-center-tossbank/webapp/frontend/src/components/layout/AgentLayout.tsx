import { Outlet, Link, useLocation, useNavigate } from 'react-router-dom';
import { motion } from 'framer-motion';
import { useAuthStore } from '../../stores/authStore';
import { useEffect } from 'react';

export default function AgentLayout() {
  const location = useLocation();
  const navigate = useNavigate();
  const { user, isAuthenticated, logout } = useAuthStore();
  
  // 개발 환경에서는 자동 로그인 (테스트용)
  useEffect(() => {
    if (!isAuthenticated) {
      // 테스트용 자동 로그인
      useAuthStore.getState().login({
        id: 1,
        username: 'agent001',
        name: '김상담',
        email: 'agent001@tossbank.com',
        role: 'AGENT',
        department: 'TM센터'
      });
    }
  }, [isAuthenticated]);

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const navItems = [
    { path: '/agent', label: '대시보드', icon: '📊' },
    { path: '/agent/call', label: '상담하기', icon: '📞' },
  ];

  return (
    <div className="min-h-screen bg-toss-gray-50 flex">
      {/* 사이드바 */}
      <aside className="w-64 bg-white border-r border-toss-gray-200 flex flex-col">
        {/* 로고 */}
        <div className="h-16 flex items-center px-6 border-b border-toss-gray-200">
          <Link to="/agent" className="flex items-center gap-2">
            <div className="w-8 h-8 bg-toss-blue-500 rounded-lg flex items-center justify-center">
              <svg className="w-5 h-5 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 5a2 2 0 012-2h3.28a1 1 0 01.948.684l1.498 4.493a1 1 0 01-.502 1.21l-2.257 1.13a11.042 11.042 0 005.516 5.516l1.13-2.257a1 1 0 011.21-.502l4.493 1.498a1 1 0 01.684.949V19a2 2 0 01-2 2h-1C9.716 21 3 14.284 3 6V5z" />
              </svg>
            </div>
            <div>
              <span className="text-lg font-bold text-toss-gray-900">TM Center</span>
              <span className="block text-xs text-toss-gray-500">상담사 전용</span>
            </div>
          </Link>
        </div>
        
        {/* 네비게이션 */}
        <nav className="flex-1 p-4">
          <ul className="space-y-1">
            {navItems.map((item) => (
              <li key={item.path}>
                <Link
                  to={item.path}
                  className={`flex items-center gap-3 px-4 py-3 rounded-toss-sm text-sm font-medium transition-colors ${
                    location.pathname === item.path
                      ? 'bg-toss-blue-50 text-toss-blue-500'
                      : 'text-toss-gray-700 hover:bg-toss-gray-100'
                  }`}
                >
                  <span>{item.icon}</span>
                  {item.label}
                </Link>
              </li>
            ))}
          </ul>
        </nav>
        
        {/* 사용자 정보 */}
        <div className="p-4 border-t border-toss-gray-200">
          <div className="flex items-center gap-3 px-4 py-3">
            <div className="w-10 h-10 bg-toss-gray-200 rounded-full flex items-center justify-center">
              <span className="text-lg">👤</span>
            </div>
            <div className="flex-1">
              <p className="text-sm font-medium text-toss-gray-900">{user?.name || '상담사'}</p>
              <p className="text-xs text-toss-gray-500">{user?.department || 'TM센터'}</p>
            </div>
          </div>
          <button
            onClick={handleLogout}
            className="w-full mt-2 px-4 py-2 text-sm text-toss-gray-600 hover:bg-toss-gray-100 rounded-toss-sm transition-colors"
          >
            로그아웃
          </button>
        </div>
      </aside>
      
      {/* 메인 컨텐츠 */}
      <div className="flex-1 flex flex-col">
        {/* 헤더 */}
        <header className="h-16 bg-white border-b border-toss-gray-200 flex items-center px-6">
          <h1 className="text-lg font-semibold text-toss-gray-900">
            {navItems.find(item => location.pathname.startsWith(item.path))?.label || '상담사 페이지'}
          </h1>
        </header>
        
        {/* 페이지 컨텐츠 */}
        <main className="flex-1 p-6 overflow-auto">
          <motion.div
            key={location.pathname}
            initial={{ opacity: 0, y: 10 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.2 }}
          >
            <Outlet />
          </motion.div>
        </main>
      </div>
    </div>
  );
}
