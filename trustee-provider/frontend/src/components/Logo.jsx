import React from 'react';

const Logo = () => {
  const letters = [
    { char: 'V', type: 'brand' },
    { char: '-', type: 'brand' },
    { char: 'P', type: 'pass' },
    { char: 'A', type: 'pass' },
    { char: 'S', type: 'pass' },
    { char: 'S', type: 'pass' }
  ];
  
  return (
    <div className="flex flex-col items-center group select-none">
      <div className="flex items-center gap-[3px]">
        {letters.map((item, index) => (
          item.type === 'brand' ? (
            <span 
              key={index}
              className="text-[#4B5563] text-[28px] font-[900] leading-none tracking-tighter mx-0.5 transition-transform duration-300 group-hover:scale-110"
            >
              {item.char}
            </span>
          ) : (
            <div 
              key={index}
              className="w-9 h-12 bg-[#FF3B44] rounded-[6px] flex items-center justify-center shadow-[0_4px_12px_rgba(255,59,68,0.2)] transition-all duration-300 group-hover:scale-105 group-hover:-translate-y-1"
              style={{ transitionDelay: `${index * 50}ms` }}
            >
              <span className="text-white text-[24px] font-[900] leading-none tracking-tighter">
                {item.char}
              </span>
            </div>
          )
        ))}
      </div>
      <div className="mt-2.5 flex items-center gap-2">
        <div className="h-[1px] w-4 bg-gray-200"></div>
        <span className="text-[9px] font-black text-[#4B5563] tracking-[0.25em] uppercase">
          Verification PASS
        </span>
        <div className="h-[1px] w-4 bg-gray-300"></div>
      </div>
    </div>
  );
};

export default Logo;
