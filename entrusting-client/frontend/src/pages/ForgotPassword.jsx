import React, { useState, useEffect } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import Logo from '../components/Logo';
import { ChevronLeft, CheckCircle2 } from 'lucide-react';

const ForgotPassword = () => {
    const navigate = useNavigate();
    const [searchParams] = useSearchParams();

    const [step, setStep] = useState(1);
    const [username, setUsername] = useState('');
    const [name, setName] = useState('');
    const [phoneNumber, setPhoneNumber] = useState('');
    const [password, setPassword] = useState('');
    const [confirmPassword, setConfirmPassword] = useState('');
    const [isVerified, setIsVerified] = useState(false);
    const [message, setMessage] = useState('');
    const [showSuccess, setShowSuccess] = useState(false);

    useEffect(() => {
        const verified = searchParams.get('verified');
        const sid = searchParams.get('sid');
        if (verified === 'true') {
            setIsVerified(true);
            setStep(2);
            if (sid) setUsername(sid);
        }
    }, [searchParams]);

    const formatPhoneNumber = (val) => {
        const numbers = val.replace(/\D/g, '');
        if (numbers.length <= 3) return numbers;
        if (numbers.length <= 7) return `${numbers.slice(0, 3)}-${numbers.slice(3)}`;
        return `${numbers.slice(0, 3)}-${numbers.slice(3, 7)}-${numbers.slice(7, 11)}`;
    };

    const handleAuthVerification = async () => {
        if (!username || !name || !phoneNumber) {
            setMessage('아이디, 이름, 휴대폰 번호를 모두 입력해 주세요.');
            return;
        }

        try {
            const cleanPhoneNumber = phoneNumber.replace(/\D/g, '');
            // [보성] 위탁사 프론트엔드 프록시(/trustee-api) 이용
            const initResponse = await fetch('/trustee-api/v1/auth/init', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ clientData: cleanPhoneNumber, name: name }),
            });

            const rawText = await initResponse.text();
            let initData = {};
            try {
                if (rawText) initData = JSON.parse(rawText);
            } catch (e) {
                console.error('Invalid JSON:', rawText);
            }

            if (initResponse.ok && initData.tokenId) {
                const currentHostname = window.location.hostname;
                const trusteeAuthPageUrl = new URL(`${import.meta.env.VITE_TRUSTEE_FRONTEND_URL}/verify`);
                trusteeAuthPageUrl.searchParams.append('tokenId', initData.tokenId);
                trusteeAuthPageUrl.searchParams.append('name', name);
                trusteeAuthPageUrl.searchParams.append('phoneNumber', cleanPhoneNumber);

                // 현재 접속 도메인(origin)을 리다이렉트 주소로 사용
                const redirectUrl = new URL(`${window.location.origin}/forgot-password`);
                redirectUrl.searchParams.append('verified', 'true');
                redirectUrl.searchParams.append('sid', username);

                trusteeAuthPageUrl.searchParams.append('redirectUrl', redirectUrl.toString());

                window.location.href = trusteeAuthPageUrl.toString();
            } else {
                setMessage('본인인증 초기화 실패');
            }
        } catch (error) {
            setMessage('오류 발생: ' + error.message);
        }
    };

    const handleResetPassword = async (e) => {
        e.preventDefault();
        if (password !== confirmPassword) {
            setMessage('비밀번호가 일치하지 않습니다.');
            return;
        }

        try {
            const query = new URLSearchParams(window.location.search);
            const verifiedPhone = query.get('phoneNumber');
            const verifiedName = query.get('name');

            const response = await fetch(`/api/v1/auth/reset-password?username=${encodeURIComponent(username)}&newPassword=${encodeURIComponent(password)}&phoneNumber=${encodeURIComponent(verifiedPhone || '')}&name=${encodeURIComponent(verifiedName || '')}`, {
                method: 'POST'
            });

            if (response.ok) {
                setShowSuccess(true);
                setTimeout(() => navigate('/login'), 3000);
            } else {
                const errorMsg = await response.text();
                setMessage(`⚠️ 변경 실패: ${errorMsg}`);
            }
        } catch (error) {
            setMessage(`⚠️ 네트워크 오류: ${error.message}`);
        }
    };

    return (
        <div className="flex flex-col min-h-screen bg-white">
            {/* Success Overlay */}
            {showSuccess && (
                <div className="fixed inset-0 z-[100] bg-white flex flex-col items-center justify-center p-8 animate-in fade-in duration-500">
                    <div className="w-24 h-24 bg-blue-100 rounded-full flex items-center justify-center mb-8 animate-bounce">
                        <CheckCircle2 size={64} className="text-[#1A73E8]" />
                    </div>
                    <h2 className="text-3xl font-extrabold text-gray-900 text-center mb-4 leading-tight">
                        비밀번호 변경이<br />완료되었습니다! 🎉
                    </h2>
                    <p className="text-gray-500 text-lg font-bold text-center leading-relaxed">
                        이제 새로운 비밀번호로 안전하게<br />
                        로그인 서비스를 이용해 보세요.<br />
                        <span className="text-[#1A73E8] inline-block mt-4 animate-pulse">잠시 후 로그인 페이지로 이동합니다.</span>
                    </p>
                </div>
            )}

            {/* Header */}
            <header className="flex items-center h-20 px-6">
                <button onClick={() => navigate('/login')} className="p-2 -ml-2 rounded-full hover:bg-gray-50 transition-colors">
                    <ChevronLeft size={28} className="text-gray-700" />
                </button>
                <div className="flex-1 flex justify-center -ml-10">
                    <Logo />
                </div>
            </header>

            {/* Content */}
            <main className="flex-1 px-8 py-12 flex flex-col max-w-[480px] mx-auto w-full">
                {step === 1 ? (
                    <div className="flex flex-col flex-1">
                        <h1 className="text-[32px] font-semibold text-gray-900 leading-tight tracking-tight mb-12">
                            비밀번호를<br />
                            찾으시나요?
                        </h1>

                        <div className="space-y-8 flex-1">
                            <div>
                                <label className="input-label">아이디 (ID)</label>
                                <input
                                    type="text"
                                    value={username}
                                    onChange={(e) => setUsername(e.target.value)}
                                    placeholder="가입하신 아이디를 입력하세요"
                                    className="input-field"
                                />
                            </div>
                            <div>
                                <label className="input-label">이름</label>
                                <input
                                    type="text"
                                    value={name}
                                    onChange={(e) => setName(e.target.value)}
                                    placeholder="성명을 입력하세요"
                                    className="input-field"
                                />
                            </div>
                            <div>
                                <label className="input-label">휴대폰 번호</label>
                                <input
                                    type="tel"
                                    value={phoneNumber}
                                    onChange={(e) => setPhoneNumber(formatPhoneNumber(e.target.value))}
                                    placeholder="인증받을 번호 입력"
                                    className="input-field"
                                />
                            </div>
                            <p className="text-[16px] text-gray-500 font-bold leading-relaxed px-1">
                                비밀번호를 안전하게 재설정하기 위해<br />
                                본인확인 과정이 필요합니다.
                            </p>
                        </div>

                        <div className="py-12">
                            <button
                                onClick={handleAuthVerification}
                                disabled={!username || !name || phoneNumber.length < 10}
                                className="btn-primary"
                            >
                                본인인증 하기
                            </button>
                        </div>
                    </div>
                ) : (
                    <div className="flex flex-col flex-1">
                        <h1 className="text-[32px] font-semibold text-gray-900 leading-tight tracking-tight mb-12">
                            새로운 비밀번호를<br />
                            입력해 주세요.
                        </h1>

                        <form onSubmit={handleResetPassword} className="space-y-8 flex-1">
                            <div>
                                <label className="input-label">새 비밀번호</label>
                                <input
                                    type="password"
                                    value={password}
                                    onChange={(e) => setPassword(e.target.value)}
                                    placeholder="영문, 숫자, 특수문자 조합"
                                    className="input-field"
                                    required
                                />
                            </div>
                            <div>
                                <label className="input-label">새 비밀번호 확인</label>
                                <input
                                    type="password"
                                    value={confirmPassword}
                                    onChange={(e) => setConfirmPassword(e.target.value)}
                                    placeholder="비밀번호를 한번 더 입력하세요"
                                    className="input-field"
                                    required
                                />
                            </div>
                        </form>

                        <div className="py-12">
                            <button
                                onClick={handleResetPassword}
                                disabled={!password || password !== confirmPassword}
                                className="btn-primary"
                            >
                                비밀번호 변경 완료
                            </button>
                        </div>
                    </div>
                )}

                {message && (
                    <div className={`mt-4 p-5 rounded-[22px] border flex items-center justify-center text-sm font-black animate-in fade-in slide-in-from-top-2 ${message.includes('성공') ? 'bg-emerald-50 border-emerald-100 text-emerald-600' : 'bg-red-50 border-red-100 text-red-500'}`}>
                        {message.includes('성공') ? '✅' : '⚠️'} {message}
                    </div>
                )}
            </main>
        </div>
    );
};

export default ForgotPassword;
