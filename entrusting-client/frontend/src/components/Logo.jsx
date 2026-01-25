import React from 'react';
import { Infinity } from 'lucide-react';

const Logo = ({ className = "" }) => {
  return (
    <div className={`flex items-center space-x-2.5 whitespace-nowrap group ${className}`}>
      <div className="w-10 h-10 bg-gradient-to-br from-[#1A73E8] to-[#0D47A1] rounded-xl flex items-center justify-center shadow-lg shadow-blue-200/50 flex-shrink-0 transition-transform group-hover:scale-105 duration-300">
        <Infinity size={26} className="text-white" />
      </div>
      <span className="text-[21px] font-bold text-[#1A1A1A] tracking-tighter">
        Continue Bank
      </span>
    </div>
  );
};

export default Logo;
