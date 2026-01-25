import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import Logo from '../components/Logo';

const Login = () => {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [message, setMessage] = useState('');
  const navigate = useNavigate();

  const handleLogin = async (e) => {
    e.preventDefault();
    try {
      const response = await fetch('/api/v1/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ username, password }),
      });
      const data = await response.text();
      if (response.ok) {
        sessionStorage.setItem('logged_in_user', username);
        sessionStorage.setItem('is_first_login_check', 'true'); // 대시보드에서 팝업 띄우기 위한 용도
        setMessage('로그인 성공');
        navigate('/dashboard');
      } else {
        setMessage('로그인 실패: ' + data);
      }
    } catch (error) {
      setMessage('오류 발생: ' + error.message);
    }
  };

  return (
    <div className="flex flex-col min-h-screen bg-white">
      {/* Header */}
      <header className="flex items-center h-20 px-6">
        <Logo />
      </header>

      {/* Content */}
      <main className="flex-1 px-8 py-12 flex flex-col justify-center max-w-[480px] mx-auto w-full">
        <h1 className="text-[32px] font-semibold text-gray-900 leading-tight tracking-tight mb-12">
          반갑습니다! 👋<br />
          로그인을 진행해 주세요.
        </h1>

        <form id="login-form" onSubmit={handleLogin} className="space-y-8">
          <div>
            <label className="input-label">아이디</label>
            <input
              type="text"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              placeholder="아이디를 입력하세요"
              className="input-field"
              required
            />
          </div>

          <div>
            <label className="input-label">비밀번호</label>
            <input
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="비밀번호를 입력하세요"
              className="input-field"
              required
            />
          </div>
        </form>

        {message && (
          <div className="mt-8 p-5 bg-red-50/50 rounded-2xl text-red-500 text-sm font-bold text-center border border-red-100">
            {message}
          </div>
        )}

        <div className="mt-12 flex items-center justify-between text-[16px] font-bold">
          <Link to="/register" className="text-[#1A73E8] hover:underline px-1">회원가입</Link>
          <div className="flex items-center gap-4 text-gray-400">
            <Link to="/find-id" className="hover:text-gray-900 transition-colors">아이디 찾기</Link>
            <div className="w-[1.5px] h-3.5 bg-gray-200"></div>
            <Link to="/forgot-password" size="sm" className="hover:text-gray-900 transition-colors">비밀번호 찾기</Link>
          </div>
        </div>
      </main>

      {/* Bottom Button */}
      <div className="px-8 pb-12 max-w-[480px] mx-auto w-full">
        <button
          type="submit"
          form="login-form"
          className="btn-primary"
        >
          로그인
        </button>
      </div>
    </div>
  );
};

export default Login;
