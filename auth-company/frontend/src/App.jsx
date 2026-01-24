import React from 'react';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import AuthPage from './components/AuthPage'; // 기존 컴포넌트
import ResultPage from './components/ResultPage'; // 기존 컴포넌트
import Navbar from './components/Navbar'; // 새로 생성된 Navbar
import MainPage from './pages/MainPage'; // 새로 생성된 MainPage
import AdminPage from './pages/AdminPage'; // 새로 생성된 AdminPage
import MyPage from './pages/MyPage'; // 새로 생성된 MyPage
import './index.css'; // App.css 대신 index.css 사용 (TailwindCSS 적용)
// import './App.css'; // 기존 App.css는 더 이상 필요 없음

function App() {
  return (
    <BrowserRouter>
      {/* Dark Mode 및 전체 레이아웃을 위한 Tailwind CSS 클래스 적용 */}
      <div className="dark bg-gray-900 text-gray-100 min-h-screen">
        <Navbar /> {/* 내비게이션 바 추가 */}
        <main className="container mx-auto mt-4 p-4"> {/* 중앙 정렬 및 패딩 */}
          <Routes>
            <Route path="/" element={<MainPage />} />
            <Route path="/auth" element={<AuthPage />} />
            <Route path="/result" element={<ResultPage />} />
            <Route path="/admin" element={<AdminPage />} />
            <Route path="/mypage" element={<MyPage />} />
          </Routes>
        </main>
      </div>
    </BrowserRouter>
  );
}

export default App;
