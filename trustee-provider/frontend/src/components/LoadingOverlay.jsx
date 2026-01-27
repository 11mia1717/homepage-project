import React from 'react';
import { ShieldCheck } from 'lucide-react';

const LoadingOverlay = ({ isVisible }) => {
  if (!isVisible) return null;

  return (
    <div className="fixed inset-0 z-[1000] bg-white flex flex-col items-center justify-center animate-in fade-in duration-500">
      <div className="relative">
        {/* Animated Rings */}
        <div className="absolute inset-0 w-24 h-24 border-4 border-red-100 rounded-full animate-ping opacity-20"></div>
        <div className="w-24 h-24 border-4 border-red-500 border-t-transparent rounded-full animate-spin"></div>
        
        {/* Center Logo/Icon */}
        <div className="absolute inset-0 flex items-center justify-center">
          <ShieldCheck size={40} className="text-[#E50914] animate-pulse" />
        </div>
      </div>

      <div className="mt-20 flex flex-col items-center space-y-8">
        <div className="flex items-center gap-[4px] animate-pulse">
          {[
            { c: 'V', t: 'b' }, { c: '-', t: 'b' },
            { c: 'P', t: 'p' }, { c: 'A', t: 'p' }, { c: 'S', t: 'p' }, { c: 'S', t: 'p' }
          ].map((item, i) => (
            item.t === 'b' ? (
              <span key={i} className="text-[#4B5563] text-2xl font-black mx-0.5">{item.c}</span>
            ) : (
              <div key={i} className="w-8 h-10 rounded-[5px] flex items-center justify-center shadow-lg bg-[#FF3B44]">
                <span className="text-white text-xl font-black">{item.c}</span>
              </div>
            )
          ))}
        </div>
        
        <div className="flex flex-col items-center space-y-2">
          <div className="flex items-center space-x-2 text-[#FF3B44] font-bold">
            <div className="flex space-x-1">
              <span className="w-1.5 h-1.5 bg-[#FF3B44] rounded-full animate-bounce [animation-delay:-0.3s]"></span>
              <span className="w-1.5 h-1.5 bg-[#FF3B44] rounded-full animate-bounce [animation-delay:-0.15s]"></span>
              <span className="w-1.5 h-1.5 bg-[#FF3B44] rounded-full animate-bounce"></span>
            </div>
            <span className="text-gray-600 font-black tracking-tight text-lg">Verification PASS</span>
          </div>
          <p className="text-sm text-gray-400 font-medium tracking-tight">
            데이터 위변조 방지를 위해 보안 세션을 구성합니다
          </p>
        </div>
      </div>

      {/* Partners / Footer */}
      <div className="absolute bottom-12 flex items-center gap-6 opacity-30 grayscale">
        <span className="text-xs font-bold tracking-widest text-gray-400">SECURE VERIFICATION SYSTEM</span>
      </div>
    </div>
  );
};

export default LoadingOverlay;
