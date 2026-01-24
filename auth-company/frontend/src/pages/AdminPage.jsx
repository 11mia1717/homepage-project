import React from 'react';

function AdminPage() {
  return (
    <div className="min-h-screen bg-gray-900 text-gray-100 p-8">
      <div className="max-w-4xl mx-auto bg-gray-800 p-8 rounded-lg shadow-lg">
        <h2 className="text-4xl font-extrabold text-white mb-6">관리자 패널</h2>
        <p className="text-lg text-gray-300 mb-8">
          이 페이지는 관리자 전용 페이지입니다. (권한 검증 누락 취약점 시뮬레이션)
        </p>
        <div className="bg-gray-700 p-6 rounded-lg shadow-md">
          <h3 className="text-2xl font-bold text-white mb-4">사용자 목록</h3>
          <p className="text-gray-400">사용자 목록 및 시스템 설정 기능이 여기에 구현될 예정입니다.</p>
          {/* 예시: 사용자 데이터 테이블 */}
          <ul className="mt-4 space-y-2">
            <li className="flex items-center justify-between text-gray-300">
              <span>사용자 1</span>
              <button className="px-4 py-2 bg-blue-600 hover:bg-blue-700 rounded-md text-white transition duration-300">상세보기</button>
            </li>
            <li className="flex items-center justify-between text-gray-300">
              <span>사용자 2</span>
              <button className="px-4 py-2 bg-blue-600 hover:bg-blue-700 rounded-md text-white transition duration-300">상세보기</button>
            </li>
            {/* ... 더 많은 사용자 */}
          </ul>
        </div>
      </div>
    </div>
  );
}

export default AdminPage;