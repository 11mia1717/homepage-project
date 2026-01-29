import React, { useState } from 'react';
import { X, Gift, Check, Coffee, ChevronRight, CheckCircle2 } from 'lucide-react';
import { QRCodeSVG } from 'qrcode.react';

const StarbucksEventModal = ({ isOpen, onClose }) => {
    const [step, setStep] = useState(1); // 1: Offer, 2: Coupon
    // const [agreed, setAgreed] = useState(false); // Removed compliance state

    if (!isOpen) return null;

    return (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
            <div className="absolute inset-0 bg-black/60 backdrop-blur-sm animate-fade-in" onClick={onClose}></div>
            
            <div className="relative bg-white w-full max-w-sm rounded-[32px] overflow-hidden shadow-2xl animate-in zoom-in-95 duration-300">
                {/* Close Button */}
                <button 
                    onClick={onClose}
                    className="absolute top-4 right-4 p-2 bg-black/5 hover:bg-black/10 rounded-full transition-colors z-10"
                >
                    <X size={20} className="text-gray-500" />
                </button>

                {step === 1 ? (
                    <div className="flex flex-col h-full">
                        {/* Hero Image Area */}
                        <div className="h-44 bg-emerald-600 relative overflow-hidden flex items-center justify-center">
                            <div className="absolute inset-0 bg-[url('https://images.unsplash.com/photo-1509042239860-f550ce710b93?q=80&w=1000&auto=format&fit=crop')] bg-cover bg-center opacity-40 mix-blend-overlay"></div>
                            <div className="relative z-10 text-center text-white p-6">
                                <div className="w-14 h-14 bg-white/20 backdrop-blur-md rounded-2xl flex items-center justify-center mx-auto mb-3 border border-white/30 shadow-lg">
                                    <Gift size={28} className="text-white drop-shadow-md animate-bounce-subtle" />
                                </div>
                                <h2 className="text-xl font-[900] tracking-tight mb-1 drop-shadow-md">
                                    신용카드 연회비<br/><span className="text-yellow-300">100% 무료</span> 혜택!
                                </h2>
                            </div>
                        </div>

                        {/* Content */}
                        <div className="p-8 space-y-6">
                            <div className="text-center space-y-2">
                                <p className="text-gray-900 font-bold text-lg leading-tight">
                                    지금 이벤트 참여하면<br/>
                                    <span className="text-emerald-600">스타벅스 아메리카노</span> 100% 증정!
                                </p>
                            </div>

                            <button
                                onClick={() => setStep(2)}
                                className="w-full h-14 bg-[#1A1A1A] text-white rounded-2xl font-black text-lg hover:bg-black transition-all shadow-xl shadow-gray-200 active:scale-[0.98] flex items-center justify-center gap-2"
                            >
                                혜택 받으러 가기 <ChevronRight size={20} />
                            </button>
                        </div>
                    </div>
                ) : (
                    <div className="flex flex-col h-full bg-[#1e3932] text-white">
                        <div className="p-8 flex flex-col items-center justify-center h-full text-center relative overflow-hidden">
                            {/* Confetti Effect */}
                            <div className="absolute top-0 left-0 w-full h-full overflow-hidden pointer-events-none">
                                <div className="absolute top-10 left-10 w-2 h-2 bg-yellow-400 rounded-full animate-ping"></div>
                                <div className="absolute top-20 right-20 w-3 h-3 bg-red-400 rounded-full animate-ping" style={{animationDelay: '0.5s'}}></div>
                                <div className="absolute bottom-20 left-1/2 w-2 h-2 bg-blue-400 rounded-full animate-ping" style={{animationDelay: '1s'}}></div>
                            </div>

                            <div className="w-16 h-16 bg-white/10 rounded-full flex items-center justify-center mb-6 animate-in zoom-in duration-500">
                                <CheckCircle2 size={32} className="text-emerald-400" />
                            </div>

                            <h2 className="text-2xl font-[900] mb-2 tracking-tight">쿠폰이 발급되었습니다!</h2>
                            <p className="text-emerald-200 text-sm font-medium mb-8">
                                캡처하여 매장에서 사용하세요.
                            </p>

                            <div className="bg-white p-6 rounded-3xl shadow-2xl mb-8 transform hover:scale-105 transition-transform duration-300">
                                <div className="flex items-center gap-3 mb-4 border-b border-gray-100 pb-4">
                                    <div className="w-10 h-10 bg-[#00704A] rounded-full flex items-center justify-center">
                                        <Coffee size={20} className="text-white" />
                                    </div>
                                    <div className="text-left">
                                        <div className="text-[#00704A] font-black text-xs">STARBUCKS</div>
                                        <div className="text-gray-800 font-bold text-sm">아이스 부드러운 디저트 세트</div>
                                    </div>
                                </div>
                                <div className="bg-white p-2 rounded-xl border-2 border-dashed border-gray-200">
                                    <QRCodeSVG value="https://example.com/fake-coupon" size={160} fgColor="#1A1A1A" />
                                </div>
                                <div className="mt-3 text-[10px] font-mono text-gray-400 tracking-widest">
                                    REF: STB-2026-PROMO-A
                                </div>
                            </div>

                            <button 
                                onClick={onClose}
                                className="w-full h-14 bg-white text-[#00704A] rounded-2xl font-black text-lg hover:bg-emerald-50 transition-all shadow-lg active:scale-[0.98]"
                            >
                                메인 화면으로 돌아가기
                            </button>
                        </div>
                    </div>
                )}
            </div>
        </div>
    );
};

export default StarbucksEventModal;
