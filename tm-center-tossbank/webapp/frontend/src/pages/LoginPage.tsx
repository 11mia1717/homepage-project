import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { motion } from 'framer-motion';
import toast from 'react-hot-toast';
import { useAuthStore } from '../stores/authStore';

export default function LoginPage() {
  const navigate = useNavigate();
  const { login } = useAuthStore();
  const [formData, setFormData] = useState({
    username: '',
    password: '',
  });
  const [isLoading, setIsLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!formData.username || !formData.password) {
      toast.error('아이디와 비밀번호를 입력해 주세요.');
      return;
    }

    setIsLoading(true);
    
    // 데모용 로그인 처리
    setTimeout(() => {
      if (formData.username === 'admin') {
        login({
          id: 100,
          username: 'admin',
          name: '관리자',
          email: 'admin@tossbank.com',
          role: 'ADMIN',
          department: '시스템관리',
        });
        toast.success('관리자로 로그인되었습니다.');
        navigate('/admin');
      } else {
        login({
          id: 1,
          username: formData.username,
          name: '김상담',
          email: `${formData.username}@tossbank.com`,
          role: 'AGENT',
          department: 'TM센터',
        });
        toast.success('상담사로 로그인되었습니다.');
        navigate('/agent');
      }
      setIsLoading(false);
    }, 500);
  };

  return (
    <div className="min-h-[calc(100vh-8rem)] flex items-center justify-center py-12">
      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        className="w-full max-w-md px-4"
      >
        <div className="card">
          <div className="text-center mb-8">
            <div className="w-16 h-16 mx-auto bg-toss-blue-500 rounded-toss flex items-center justify-center mb-4">
              <svg className="w-8 h-8 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
              </svg>
            </div>
            <h1 className="text-2xl font-bold text-toss-gray-900 mb-2">
              로그인
            </h1>
            <p className="text-toss-gray-600">
              TM Center 시스템에 로그인하세요
            </p>
          </div>

          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <label className="label-toss">아이디</label>
              <input
                type="text"
                className="input-toss"
                placeholder="아이디를 입력하세요"
                value={formData.username}
                onChange={(e) =>
                  setFormData({ ...formData, username: e.target.value })
                }
              />
            </div>
            <div>
              <label className="label-toss">비밀번호</label>
              <input
                type="password"
                className="input-toss"
                placeholder="비밀번호를 입력하세요"
                value={formData.password}
                onChange={(e) =>
                  setFormData({ ...formData, password: e.target.value })
                }
              />
            </div>

            <button
              type="submit"
              disabled={isLoading}
              className="btn-primary w-full mt-6"
            >
              {isLoading ? '로그인 중...' : '로그인'}
            </button>
          </form>

          <div className="mt-6 pt-6 border-t border-toss-gray-200">
            <p className="text-center text-sm text-toss-gray-500 mb-4">
              테스트 계정
            </p>
            <div className="grid grid-cols-2 gap-3 text-sm">
              <div className="bg-toss-gray-50 rounded-toss-sm p-3">
                <p className="font-medium text-toss-gray-900">상담사</p>
                <p className="text-toss-gray-500">agent001 / password</p>
              </div>
              <div className="bg-toss-gray-50 rounded-toss-sm p-3">
                <p className="font-medium text-toss-gray-900">관리자</p>
                <p className="text-toss-gray-500">admin / password</p>
              </div>
            </div>
          </div>
        </div>
      </motion.div>
    </div>
  );
}
