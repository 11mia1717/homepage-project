import { Outlet, Link, useLocation } from 'react-router-dom';
import { motion } from 'framer-motion';

export default function Layout() {
  const location = useLocation();
  
  return (
    <div className="min-h-screen bg-toss-gray-50">
      {/* 헤더 */}
      <header className="bg-white border-b border-toss-gray-200 sticky top-0 z-50">
        <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex items-center justify-between h-16">
            {/* 로고 */}
            <Link to="/" className="flex items-center gap-2">
              <div className="w-8 h-8 bg-toss-blue-500 rounded-lg flex items-center justify-center">
                <svg className="w-5 h-5 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 5a2 2 0 012-2h3.28a1 1 0 01.948.684l1.498 4.493a1 1 0 01-.502 1.21l-2.257 1.13a11.042 11.042 0 005.516 5.516l1.13-2.257a1 1 0 011.21-.502l4.493 1.498a1 1 0 01.684.949V19a2 2 0 01-2 2h-1C9.716 21 3 14.284 3 6V5z" />
                </svg>
              </div>
              <span className="text-xl font-bold text-toss-gray-900">TM Center</span>
            </Link>
            
            {/* 네비게이션 */}
            <nav className="flex items-center gap-6">
              <Link 
                to="/my-consent" 
                className={`text-sm font-medium transition-colors ${
                  location.pathname === '/my-consent' 
                    ? 'text-toss-blue-500' 
                    : 'text-toss-gray-600 hover:text-toss-gray-900'
                }`}
              >
                내 동의 내역
              </Link>
              <Link 
                to="/agent"
                className="text-sm font-medium text-toss-gray-600 hover:text-toss-gray-900 transition-colors"
              >
                상담사
              </Link>
              <Link 
                to="/admin"
                className="text-sm font-medium text-toss-gray-600 hover:text-toss-gray-900 transition-colors"
              >
                관리자
              </Link>
            </nav>
          </div>
        </div>
      </header>
      
      {/* 메인 컨텐츠 */}
      <main>
        <motion.div
          key={location.pathname}
          initial={{ opacity: 0, y: 10 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.2 }}
        >
          <Outlet />
        </motion.div>
      </main>
      
      {/* 푸터 */}
      <footer className="bg-white border-t border-toss-gray-200 mt-auto">
        <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
          <div className="text-center text-sm text-toss-gray-500">
            <p>© 2024 TM Center. 토스뱅크 콜센터 수탁사 관리 시스템</p>
            <p className="mt-2">
              <Link to="#" className="hover:text-toss-gray-700">개인정보처리방침</Link>
              <span className="mx-2">·</span>
              <Link to="#" className="hover:text-toss-gray-700">이용약관</Link>
            </p>
          </div>
        </div>
      </footer>
    </div>
  );
}
