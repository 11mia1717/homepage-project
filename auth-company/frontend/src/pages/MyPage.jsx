import React from 'react';

function MyPage() {
  return (
    <div className="min-h-screen bg-gray-900 text-gray-100 p-8">
      <div className="max-w-4xl mx-auto bg-gray-800 p-8 rounded-lg shadow-lg">
        <h2 className="text-4xl font-extrabold text-white mb-6">마이페이지</h2>
        <p className="text-lg text-gray-300 mb-8">
          이 페이지는 마이페이지입니다. 개인 정보 조회 및 설정 기능이 여기에 구현될 예정입니다.
        </p>
        <div className="bg-gray-700 p-6 rounded-lg shadow-md">
          <h3 className="text-2xl font-bold text-white mb-4">내 정보</h3>
          <p className="text-gray-400">현재 사용자 정보가 표시됩니다.</p>
          {/* 예시: 사용자 정보 */}
          <ul className="mt-4 space-y-2">
            <li className="text-gray-300">
              <span className="font-semibold">이름:</span> 홍길동
            </li>
            <li className="text-gray-300">
              <span className="font-semibold">이메일:</span> hong.gildong@example.com
            </li>
            {/* ... 더 많은 정보 */}
          </ul>
          <button className="mt-6 px-6 py-3 bg-blue-600 hover:bg-blue-700 rounded-md text-white font-semibold transition duration-300">
            정보 수정
          </button>
        </div>
      </div>
    </div>
  );
}

export default MyPage;