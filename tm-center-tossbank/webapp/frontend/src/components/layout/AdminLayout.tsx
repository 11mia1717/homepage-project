import { Outlet, Link, useLocation, useNavigate } from 'react-router-dom';
import { motion } from 'framer-motion';
import { useAuthStore } from '../../stores/authStore';
import { useEffect } from 'react';

export default function AdminLayout() {
  const location = useLocation();
  const navigate = useNavigate();
  const { user, isAuthenticated, logout } = useAuthStore();
  
  // 개발 환경에서는 자동 로그인 (테스트용)
  useEffect(() => {
    if (!isAuthenticated) {
      useAuthStore.getState().login({
        id: 100,
        username: 'admin',
        name: '관리자',
        email: 'admin@tossbank.com',
        role: 'ADMIN',
        department: '시스템관리'
      });
    }
  }, [isAuthenticated]);

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const navItems = [
    { path: '/admin', label: '대시보드', icon: '📊' },
    { path: '/admin/access-logs', label: '접근 로그', icon: '📋' },
    { path: '/admin/destruction', label: '파기 관리', icon: '🗑️' },
  ];

  return (
    <div className="min-h-screen bg-toss-gray-50 flex">
      {/* 사이드바 */}
      <aside className="w-64 bg-toss-gray-900 flex flex-col">
        {/* 로고 */}
        <div className="h-16 flex items-center px-6 border-b border-toss-gray-800">
          <Link to="/admin" className="flex items-center gap-2">
            <div className="w-8 h-8 bg-toss-blue-500 rounded-lg flex items-center justify-center">
              <svg className="w-5 h-5 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z" />
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
              </svg>
            </div>
            <div>
              <span className="text-lg font-bold text-white">TM Center</span>
              <span className="block text-xs text-toss-gray-400">관리자 콘솔</span>
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
                      ? 'bg-toss-blue-500 text-white'
                      : 'text-toss-gray-300 hover:bg-toss-gray-800 hover:text-white'
                  }`}
                >
                  <span>{item.icon}</span>
                  {item.label}
                </Link>
              </li>
            ))}
          </ul>
          
          <div className="mt-8 pt-4 border-t border-toss-gray-800">
            <Link
              to="/"
              className="flex items-center gap-3 px-4 py-3 rounded-toss-sm text-sm font-medium text-toss-gray-400 hover:bg-toss-gray-800 hover:text-white transition-colors"
            >
              <span>🏠</span>
              메인 페이지
            </Link>
          </div>
        </nav>
        
        {/* 사용자 정보 */}
        <div className="p-4 border-t border-toss-gray-800">
          <div className="flex items-center gap-3 px-4 py-3">
            <div className="w-10 h-10 bg-toss-gray-700 rounded-full flex items-center justify-center">
              <span className="text-lg">⚙️</span>
            </div>
            <div className="flex-1">
              <p className="text-sm font-medium text-white">{user?.name || '관리자'}</p>
              <p className="text-xs text-toss-gray-400">{user?.role || 'ADMIN'}</p>
            </div>
          </div>
          <button
            onClick={handleLogout}
            className="w-full mt-2 px-4 py-2 text-sm text-toss-gray-400 hover:bg-toss-gray-800 hover:text-white rounded-toss-sm transition-colors"
          >
            로그아웃
          </button>
        </div>
      </aside>
      
      {/* 메인 컨텐츠 */}
      <div className="flex-1 flex flex-col">
        {/* 헤더 */}
        <header className="h-16 bg-white border-b border-toss-gray-200 flex items-center justify-between px-6">
          <h1 className="text-lg font-semibold text-toss-gray-900">
            {navItems.find(item => item.path === location.pathname)?.label || '관리자 페이지'}
          </h1>
          <div className="flex items-center gap-4">
            <span className="text-sm text-toss-gray-500">
              {new Date().toLocaleDateString('ko-KR', { 
                year: 'numeric', 
                month: 'long', 
                day: 'numeric',
                weekday: 'long'
              })}
            </span>
          </div>
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
