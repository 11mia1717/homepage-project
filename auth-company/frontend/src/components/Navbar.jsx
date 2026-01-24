import React from 'react';
import { Link } from 'react-router-dom';

function Navbar() {
  return (
    <nav className="bg-gray-800 p-4 shadow-md">
      <div className="container mx-auto flex justify-between items-center">
        <Link to="/" className="text-white text-2xl font-bold">
          기업 인증 시스템
        </Link>
        <div className="space-x-6">
          <Link to="/auth" className="text-gray-300 hover:text-white transition duration-300 text-lg">
            본인인증
          </Link>
          <Link to="/mypage" className="text-gray-300 hover:text-white transition duration-300 text-lg">
            마이페이지
          </Link>
          <Link to="/admin" className="text-gray-300 hover:text-white transition duration-300 text-lg">
            관리자 패널
          </Link>
        </div>
      </div>
    </nav>
  );
}

export default Navbar;
