import React, { useState, useEffect } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import Logo from '../components/Logo';
import { ChevronLeft, ShieldCheck, CheckCircle2, Wallet } from 'lucide-react';

const CreateAccount = () => {
    const navigate = useNavigate();
    const [searchParams] = useSearchParams();
    const [step, setStep] = useState(1);
    const [accountName, setAccountName] = useState('Continue 입출금 통장');
    const [message, setMessage] = useState('');
    const [isLoading, setIsLoading] = useState(false);

    const isVerified = searchParams.get('verified') === 'true';
    const username = sessionStorage.getItem('logged_in_user');

    useEffect(() => {
        if (isVerified) {
            setStep(2);
        }
    }, [isVerified]);

    const handleAuthVerification = async () => {
        try {
            const initResponse = await fetch('/trustee-api/v1/auth/init', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ clientData: 'account-open', name: '계좌개설' }),
            });

            const rawText = await initResponse.text();
            let initData = {};
            try {
                initData = JSON.parse(rawText);
            } catch (e) {
                throw new Error(`서버 응답 오류 (JSON 아님): ${rawText.substring(0, 50)}`);
            }

            if (initResponse.ok && initData.tokenId) {
                const currentHostname = window.location.hostname;
                const trusteeAuthPageUrl = new URL(`http://${currentHostname}:5174/verify`);
                trusteeAuthPageUrl.searchParams.append('tokenId', initData.tokenId);

                // 세션에 저장된 가입 정보를 활용하여 이름과 번호 전달
                const storedData = sessionStorage.getItem('register_form_data');
                if (storedData) {
                    const parsed = JSON.parse(storedData);
                    if (parsed.name) trusteeAuthPageUrl.searchParams.append('name', parsed.name);
                    if (parsed.phoneNumber) trusteeAuthPageUrl.searchParams.append('phoneNumber', parsed.phoneNumber.replace(/\D/g, ''));
                }

                const redirectUrl = new URL(`${window.location.origin}/create-account`);
                redirectUrl.searchParams.append('verified', 'true');
                trusteeAuthPageUrl.searchParams.append('redirectUrl', redirectUrl.toString());

                window.location.href = trusteeAuthPageUrl.toString();
            } else {
                setMessage('본인인증 초기화 실패: ' + (initData.message || '로그를 확인하세요.'));
            }
        } catch (error) {
            setMessage('오류 발생: ' + error.message);
        }
    };

    const handleFinalizeCreation = async () => {
        if (!username) {
            navigate('/login');
            return;
        }

        setIsLoading(true);
        try {
            const response = await fetch('/api/v1/accounts/create', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ username, accountName }),
            });

            if (response.ok) {
                setStep(3);
            } else {
                const errorText = await response.text();
                setMessage('계좌 생성 실패: ' + errorText);
            }
        } catch (error) {
            setMessage('오류 발생: ' + error.message);
        } finally {
            setIsLoading(false);
        }
    };

    return (
        <div className="flex flex-col min-h-screen bg-white">
            <header className="flex items-center h-20 px-6">
                <button onClick={() => navigate('/dashboard')} className="p-2 -ml-2 rounded-full hover:bg-gray-50">
                    <ChevronLeft size={28} className="text-gray-700" />
                </button>
                <div className="flex-1 flex justify-center -ml-10">
                    <Logo />
                </div>
            </header>

            <main className="flex-1 px-8 py-10 flex flex-col max-w-[480px] mx-auto w-full">
                {step === 1 && (
                    <div className="flex flex-col flex-1">
                        <div className="bg-blue-50 w-16 h-16 rounded-2xl flex items-center justify-center mb-8">
                            <ShieldCheck size={32} className="text-[#1A73E8]" />
                        </div>
                        <h1 className="text-[32px] font-semibold text-gray-900 leading-tight tracking-tight mb-4">
                            계좌를 개설하기 위해<br />본인인증을 진행합니다.
                        </h1>
                        <p className="text-gray-500 font-medium leading-relaxed mb-12">
                            안전한 금융 거래를 위해<br />수탁사 본인확인이 필요해요.
                        </p>
                        <div className="mt-auto pb-10">
                            <button onClick={handleAuthVerification} className="btn-primary">
                                본인인증 하기
                            </button>
                        </div>
                    </div>
                )}

                {step === 2 && (
                    <div className="flex flex-col flex-1">
                        <div className="bg-emerald-50 w-16 h-16 rounded-2xl flex items-center justify-center mb-8 text-emerald-600">
                            <CheckCircle2 size={32} />
                        </div>
                        <h1 className="text-[32px] font-semibold text-gray-900 leading-tight tracking-tight mb-4">
                            본인인증 완료!<br />계좌 이름을 정해주세요.
                        </h1>
                        <div className="mt-8 space-y-6 flex-1">
                            <div>
                                <label className="input-label">계좌명</label>
                                <input
                                    type="text"
                                    value={accountName}
                                    onChange={(e) => setAccountName(e.target.value)}
                                    className="input-field"
                                    placeholder="계좌 이름을 입력하세요"
                                />
                            </div>
                        </div>
                        <div className="mt-auto pb-10">
                            <button
                                onClick={handleFinalizeCreation}
                                disabled={isLoading || !accountName}
                                className="btn-primary"
                            >
                                {isLoading ? '개설 중...' : '계좌 개설 완료'}
                            </button>
                        </div>
                    </div>
                )}

                {step === 3 && (
                    <div className="flex flex-col flex-1 items-center justify-center text-center">
                        <div className="w-24 h-24 bg-blue-100 rounded-full flex items-center justify-center mb-8 animate-bounce">
                            <CheckCircle2 size={48} className="text-[#1A73E8]" />
                        </div>
                        <h1 className="text-[28px] font-bold text-gray-900 mb-4">
                            계좌 개설을<br />축하드립니다! 🎉
                        </h1>
                        <p className="text-gray-500 font-medium mb-12">
                            가입 축하금 <span className="text-[#1A73E8] font-bold">1,000원</span>이 입금되었습니다.<br />
                            이제 Continue의 모든 서비스를 이용해 보세요.
                        </p>
                        <button onClick={() => navigate('/dashboard')} className="btn-primary w-full">
                            확인
                        </button>
                    </div>
                )}

                {message && (
                    <div className="mt-4 p-4 bg-red-50 text-red-500 text-sm font-bold rounded-xl text-center">
                        ⚠️ {message}
                    </div>
                )}
            </main>
        </div>
    );
};

export default CreateAccount;
