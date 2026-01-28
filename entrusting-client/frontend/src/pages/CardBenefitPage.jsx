import React from 'react';
import { useNavigate } from 'react-router-dom';
import { ChevronLeft, CreditCard, Gift, Percent, Gem } from 'lucide-react';

const CardBenefitPage = () => {
  const navigate = useNavigate();

  const handleGoBack = () => {
    navigate(-1);
  };

  const handleConsultation = () => {
    alert('상담 신청이 완료되었습니다. 곧 연락드리겠습니다!');
    // 실제 상담 신청 로직 (API 호출 등) 추가 예정
  };

  return (
    <div className="flex flex-col min-h-screen bg-[#F8F9FA]">
      <header className="sticky top-0 z-20 flex items-center h-16 px-6 bg-white/80 backdrop-blur-md border-b border-gray-100">
        <button onClick={handleGoBack} className="absolute left-4 p-2.5 text-gray-500 hover:bg-gray-50 rounded-full transition-colors">
          <ChevronLeft size={28} />
        </button>
        <h1 className="flex-1 text-center text-[18px] font-semibold text-gray-900">루키즈 카드 혜택</h1>
      </header>

      <main className="flex-1 px-5 py-8 space-y-10 max-w-[480px] mx-auto w-full">
        {/* 카드 이미지 섹션 */}
        <section className="flex justify-center mb-8">
          <div className="relative w-[280px] h-[180px] bg-gradient-to-br from-purple-500 to-indigo-600 rounded-xl shadow-xl flex items-center justify-center text-white overflow-hidden p-6 animate-in zoom-in duration-500">
            <div className="absolute inset-0 opacity-10 flex items-center justify-center">
              <CreditCard size={120} />
            </div>
            <div className="relative z-10 text-center">
              <p className="text-white/80 text-[12px] font-medium">Rookies Card</p>
              <h2 className="text-[32px] font-bold tracking-tight">루키즈 카드</h2>
            </div>
          </div>
        </section>

        {/* 주요 혜택 3가지 */}
        <section className="space-y-4 px-2">
          <h3 className="text-[20px] font-bold text-gray-900 text-center mb-6">이런 혜택, 놓치지 마세요!</h3>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            <div className="bg-white rounded-[20px] p-6 text-center shadow-md border border-gray-100 flex flex-col items-center group hover:shadow-lg transition-all duration-300">
              <div className="w-14 h-14 bg-red-100 text-red-500 rounded-full flex items-center justify-center mb-3 group-hover:scale-110 transition-transform">
                <Percent size={28} />
              </div>
              <p className="text-[16px] font-bold text-gray-800">최대 5% 캐시백</p>
              <p className="text-[13px] text-gray-500 mt-1">주요 온라인 쇼핑몰</p>
            </div>
            <div className="bg-white rounded-[20px] p-6 text-center shadow-md border border-gray-100 flex flex-col items-center group hover:shadow-lg transition-all duration-300">
              <div className="w-14 h-14 bg-green-100 text-green-600 rounded-full flex items-center justify-center mb-3 group-hover:scale-110 transition-transform">
                <Gift size={28} />
              </div>
              <p className="text-[16px] font-bold text-gray-800">매월 간식 혜택</p>
              <p className="text-[13px] text-gray-500 mt-1">편의점, 카페 등</p>
            </div>
            <div className="bg-white rounded-[20px] p-6 text-center shadow-md border border-gray-100 flex flex-col items-center group hover:shadow-lg transition-all duration-300">
              <div className="w-14 h-14 bg-yellow-100 text-yellow-600 rounded-full flex items-center justify-center mb-3 group-hover:scale-110 transition-transform">
                <Gem size={28} />
              </div>
              <p className="text-[16px] font-bold text-gray-800">프리미엄 멤버십</p>
              <p className="text-[13px] text-gray-500 mt-1">영화, 문화 생활 할인</p>
            </div>
          </div>
        </section>

        {/* 상담 신청 버튼 */}
        <section className="px-2 mt-8">
          <button
            onClick={handleConsultation}
            className="w-full h-14 bg-[#1A73E8] text-white rounded-xl font-semibold text-[17px] hover:bg-[#1A73E8]/90 active:scale-[0.98] transition-all shadow-lg"
          >
            상담 신청하기
          </button>
        </section>

        {/* 상세 혜택 목록 */}
        <section className="space-y-5 pt-4">
          <h3 className="text-[18px] font-bold text-gray-900 px-2">상세 혜택 안내</h3>
          <div className="bg-white rounded-[22px] p-6 shadow-md border border-gray-100 space-y-4">
            <div>
              <h4 className="font-bold text-gray-800 text-[15px] mb-1">🛍️ 온라인 쇼핑 캐시백</h4>
              <p className="text-gray-600 text-[13px]">쿠팡, 네이버 스마트스토어, G마켓, 옥션 등 주요 온라인 쇼핑몰 이용 시 결제 금액의 최대 5% 캐시백 (월 최대 2만원)</p>
            </div>
            <div>
              <h4 className="font-bold text-gray-800 text-[15px] mb-1">☕ 매일 간식 혜택</h4>
              <p className="text-gray-600 text-[13px]">전국 편의점 (CU, GS25, 세븐일레븐), 카페 (스타벅스, 투썸플레이스) 이용 시 결제 금액의 10% 즉시 할인 (일 1회, 월 최대 1만원)</p>
            </div>
            <div>
              <h4 className="font-bold text-gray-800 text-[15px] mb-1">🎬 문화 생활 할인</h4>
              <p className="text-gray-600 text-[13px]">CGV, 롯데시네마 영화 티켓 5천원 할인 (월 1회), 멜론/지니뮤직 스트리밍 서비스 2천원 할인 (월 1회)</p>
            </div>
            <div>
              <h4 className="font-bold text-gray-800 text-[15px] mb-1">✈️ 해외 이용 수수료 면제</h4>
              <p className="text-gray-600 text-[13px]">해외 온/오프라인 결제 시 발생하는 해외 이용 수수료 전액 면제</p>
            </div>
            <div>
              <h4 className="font-bold text-gray-800 text-[15px] mb-1">🚌 대중교통 할인</h4>
              <p className="text-gray-600 text-[13px]">버스, 지하철 이용 요금 10% 할인 (월 최대 5천원)</p>
            </div>
          </div>
        </section>
      </main>
    </div>
  );
};

export default CardBenefitPage;
