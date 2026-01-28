import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import TermsAgreement from '../components/TermsAgreement';
import Register from './Register';

/**
 * 회원가입 전체 플로우 관리
 * Step 1: 약관 동의
 * Step 2: 본인인증 (AuthCallback으로 리다이렉트)
 * Step 3: 회원정보 입력 (Register)
 */
const SignupFlow = () => {
  const navigate = useNavigate();
  const [currentStep, setCurrentStep] = useState('terms'); // terms, bridge
  const [termsData, setTermsData] = useState(null);

  const handleTermsComplete = (data) => {
    setTermsData(data);
    sessionStorage.setItem('terms_agreement', JSON.stringify(data));
    setCurrentStep('bridge');
  };

  if (currentStep === 'terms') {
    return <TermsAgreement onComplete={handleTermsComplete} />;
  }

  if (currentStep === 'bridge') {
    return (
      <div className="flex flex-col min-h-screen bg-white animate-in fade-in duration-700">
        <header className="flex items-center h-20 px-6 shrink-0">
          <div className="flex-1 flex justify-center"><Logo /></div>
        </header>

        <main className="flex-1 px-8 py-12 max-w-[480px] mx-auto w-full">
          <div className="mb-12">
            <div className="w-16 h-16 bg-blue-50 rounded-2xl flex items-center justify-center mb-6">
              <ShieldCheck size={32} className="text-[#1A73E8]" />
            </div>
            <h1 className="text-[32px] font-bold text-[#1A1A1A] leading-tight mb-4">
              약관에 모두<br />동의하셨습니다.
            </h1>
            <p className="text-[17px] text-gray-400 font-medium">안전한 가입을 위해 다음 단계를 진행합니다.</p>
          </div>

          <div className="space-y-6">
             <StepItem number="1" title="본인인증(V-pass)" desc="이름과 연락처로 본인임을 확인합니다" active={true} />
             <StepItem number="2" title="회원정보 입력" desc="아이디와 비밀번호를 설정합니다" />
             <StepItem number="3" title="가입 완료" desc="Continue Bank의 회원이 되신걸 환영해요!" />
          </div>
        </main>

        <div className="px-8 pb-12 max-w-[480px] mx-auto w-full">
          <button onClick={() => navigate('/register')} className="btn-primary">본인인증으로 진행하기</button>
        </div>
      </div>
    );
  }

  return <TermsAgreement onComplete={handleTermsComplete} />;
};

const StepItem = ({ number, title, desc, active = false }) => (
  <div className={`flex gap-5 p-5 rounded-[24px] border transition-all ${active ? 'bg-blue-50/50 border-blue-100 shadow-sm' : 'border-gray-50'}`}>
    <div className={`w-10 h-10 rounded-full flex items-center justify-center font-black text-lg shrink-0 ${active ? 'bg-[#1A73E8] text-white' : 'bg-gray-100 text-gray-300'}`}>
      {number}
    </div>
    <div>
      <h3 className={`font-bold text-[17px] ${active ? 'text-[#1A1A1A]' : 'text-gray-300'}`}>{title}</h3>
      <p className={`text-[14px] mt-0.5 font-medium ${active ? 'text-gray-500' : 'text-gray-200'}`}>{desc}</p>
    </div>
  </div>
);

export default SignupFlow;