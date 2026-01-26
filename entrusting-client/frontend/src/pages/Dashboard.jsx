import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import Logo from '../components/Logo';
import {
  Bell,
  Settings,
  Wallet,
  ArrowUpRight,
  Plus,
  CreditCard,
  TrendingUp,
  ShieldCheck,
  ChevronRight,
  LogOut,
  Home,
  Gift,
  LayoutGrid,
  Menu
} from 'lucide-react';

const Dashboard = () => {
  const navigate = useNavigate();
  const [userName, setUserName] = useState('고객');
  const [summary, setSummary] = useState({ totalBalance: 0, accounts: [], recentTransactions: [] });
  const [isLoading, setIsLoading] = useState(true);

  const username = sessionStorage.getItem('logged_in_user');
  const isFirstLoginCheck = sessionStorage.getItem('is_first_login_check') === 'true';

  useEffect(() => {
    // 1. 사용자 이름 설정
    const storedData = sessionStorage.getItem('register_form_data');
    if (storedData) {
      try {
        const parsed = JSON.parse(storedData);
        if (parsed.name) setUserName(parsed.name);
      } catch (e) { }
    }

    // 2. 대시보드 요약 정보 가져오기
    if (username) {
      const fetchDashboardData = async () => {
        try {
          const response = await fetch(`/api/v1/dashboard/summary?username=${username}`);
          if (response.ok) {
            const data = await response.json();
            setSummary(data);

          }
        } catch (err) {
          console.error('Failed to fetch dashboard data:', err);
        } finally {
          setIsLoading(false);
        }
      };
      fetchDashboardData();
    } else {
      navigate('/login');
    }
  }, [username, isFirstLoginCheck, navigate]);

  const handleLogout = () => {
    sessionStorage.clear();
    navigate('/login');
  };

  return (
    <div className="flex flex-col min-h-screen bg-[#F8F9FA]">
      <header className="sticky top-0 z-20 flex items-center justify-between h-16 px-6 bg-white/80 backdrop-blur-md border-b border-gray-100">
        <Logo className="h-7" />
        <div className="flex items-center gap-2">
          <button className="p-2.5 text-gray-500 hover:bg-gray-50 rounded-full transition-colors">
            <Bell size={28} />
          </button>
          <button
            onClick={handleLogout}
            className="p-2.5 text-gray-500 hover:bg-red-50 hover:text-red-500 rounded-full transition-all"
            title="로그아웃"
          >
            <LogOut size={28} />
          </button>
        </div>
      </header>

      <main className="flex-1 px-5 py-6 space-y-10 max-w-[480px] mx-auto w-full">
        <section className="px-1 py-1">
          <div className="bg-[#1A73E8] text-white text-[10px] font-bold px-2.5 py-1 rounded-full mb-4 inline-flex items-center gap-1.5 shadow-md shadow-blue-100">
            <span className="w-1.5 h-1.5 bg-white rounded-full animate-pulse"></span>
            CONTINUE PREMIUM SERVICE
          </div>
          <h2 className="text-[22px] font-semibold text-gray-900 tracking-tight leading-snug">
            {userName}님, 반갑습니다! 👋<br />
            <span className="text-[15px] text-gray-400 font-medium">오늘도 당신의 금융 생활을 응원합니다.</span>
          </h2>
        </section>


        <section className="space-y-6">
          <div className="flex justify-between items-center px-1">
            <div className="flex items-center gap-3">
              <div className="w-11 h-11 bg-blue-50 rounded-2xl flex items-center justify-center text-[#1A73E8]">
                <Wallet size={22} />
              </div>
              <span className="text-gray-900 font-bold text-[20px] tracking-tight">내 계좌</span>
            </div>
            <button
              onClick={() => navigate('/create-account')}
              className="flex items-center gap-1.5 text-[#1A73E8] bg-blue-50 px-4 py-2 rounded-full text-xs font-bold hover:bg-blue-100 transition-colors"
            >
              <Plus size={14} />
              계좌 추가
            </button>
          </div>

          {isLoading ? (
            <div className="space-y-4">
              {[1, 2].map(i => (
                <div key={i} className="bg-white rounded-[32px] p-7 border border-gray-50 animate-pulse">
                  <div className="h-4 w-24 bg-gray-100 rounded mb-4"></div>
                  <div className="h-10 w-48 bg-gray-100 rounded mb-6"></div>
                  <div className="flex gap-3">
                    <div className="flex-1 h-12 bg-gray-100 rounded-2xl"></div>
                    <div className="flex-1 h-12 bg-gray-100 rounded-2xl"></div>
                  </div>
                </div>
              ))}
            </div>
          ) : summary.accounts.length === 0 ? (
            <div className="space-y-6">
              {/* [요청] 계좌가 없는 경우 상시 노출될 10,000원 혜택 배너 - 중앙 정렬 및 디자인 정교화 */}
              <div className="bg-gradient-to-br from-[#1A73E8] to-[#0D47A1] rounded-[20px] p-8 text-white relative overflow-hidden shadow-xl shadow-blue-200 animate-in fade-in zoom-in duration-500">
                <div className="relative z-10 flex flex-col items-center text-center">
                  <div className="flex justify-center mb-4">
                    <div className="bg-amber-400 text-blue-900 px-3 py-1 rounded-full text-[10px] font-bold tracking-widest uppercase shadow-sm">Limited Offer</div>
                  </div>
                  <h3 className="text-[24px] font-medium leading-tight mb-3">
                    지금 첫 계좌를 개설하고<br />
                    <span className="text-yellow-300 font-semibold">10,000원</span> 혜택을 받으세요! 💰
                  </h3>
                  <p className="text-blue-100 text-[14px] font-normal opacity-90 mb-8">
                    신규 가입 고객님께만 드리는 특별한 선물,<br />
                    본인인증 완료 즉시 잔액으로 지급됩니다.
                  </p>
                  <button
                    onClick={() => navigate('/create-account')}
                    className="w-full h-14 bg-white text-[#1A73E8] rounded-xl font-semibold text-[16px] hover:bg-gray-50 active:scale-[0.98] transition-all shadow-lg max-w-[320px]"
                  >
                    10,000원 받고 계좌 만들기
                  </button>
                </div>
                {/* Decorative background circle */}
                <div className="absolute top-0 right-0 w-40 h-40 bg-white/10 rounded-full -mr-16 -mt-16 blur-2xl"></div>
              </div>

              <div className="bg-white rounded-[32px] p-10 border border-dashed border-gray-200 flex flex-col items-center justify-center text-center opacity-60">
                <div className="w-16 h-16 bg-gray-50 rounded-full flex items-center justify-center text-gray-300 mb-4">
                  <Wallet size={28} />
                </div>
                <p className="text-gray-400 font-bold text-[15px]">아직 개설된 계좌가 없습니다.</p>
              </div>
            </div>
          ) : (
            <div className="space-y-4">
              {summary.accounts.map((acc, idx) => (
                <div key={idx} className="bg-white rounded-[32px] p-7 shadow-[0_8px_30px_rgb(0,0,0,0.04)] border border-gray-50 group hover:shadow-xl hover:shadow-blue-500/5 transition-all duration-500">
                  <div className="mb-7">
                    <div className="flex justify-between items-center mb-1">
                      <h3 className="text-gray-500 text-[13px] font-medium">{acc.accountName}</h3>
                      <span className="text-gray-400 text-[13px] font-bold font-mono tracking-wider">{acc.accountNumber}</span>
                    </div>
                    <div className="flex items-baseline gap-1">
                      <span className="text-[34px] font-medium text-[#1A73E8] tracking-tighter">
                        ₩ {Number(acc.balance).toLocaleString()}
                      </span>
                    </div>
                  </div>

                  <div className="flex gap-3">
                    <button className="flex-1 h-[52px] bg-[#1A73E8] text-white rounded-2xl font-bold text-[14px] flex items-center justify-center gap-2 hover:brightness-110 active:scale-[0.98] transition-all">
                      송금하기
                    </button>
                    <button className="flex-1 h-[52px] bg-gray-50 text-gray-700 rounded-2xl font-bold text-[14px] flex items-center justify-center gap-2 hover:bg-gray-100 active:scale-[0.98] transition-all border border-gray-100/50">
                      내역조회
                    </button>
                  </div>
                </div>
              ))}
            </div>
          )}
        </section>

        <section className="mt-14 mb-4">
          <div className="flex justify-between items-center px-1">
            <div className="flex items-center gap-3">
              <div className="w-11 h-11 bg-pink-50 rounded-2xl flex items-center justify-center text-yellow-500 shadow-sm">
                <Gift size={22} />
              </div>
              <span className="text-gray-900 font-bold text-[20px] tracking-tight">혜택보기</span>
            </div>
          </div>
        </section>

        <div className="pt-4"></div>

        <section className="space-y-3">
          {[
            { title: '신용점수 올리고 대출 한도 조회하기', desc: '내 신용점수는 몇 점일까?', tag: 'EVENT' }
          ].map((item, idx) => (
            <div key={idx} className="bg-white/60 px-5 py-4 rounded-[22px] flex justify-between items-center group cursor-pointer hover:bg-white hover:shadow-md transition-all border border-white/50">
              <div className="space-y-0.5">
                <div className="flex items-center gap-2">
                  <p className="text-[12px] font-bold text-gray-700 group-hover:text-[#1A73E8] transition-colors">{item.title}</p>
                  {item.tag && <span className="bg-red-50 text-red-500 text-[8px] font-bold px-1.5 py-0.5 rounded-full uppercase">{item.tag}</span>}
                </div>
                <p className="text-[11px] text-gray-400 font-medium">{item.desc}</p>
              </div>
              <ChevronRight size={14} className="text-gray-300 group-hover:text-gray-400 group-hover:translate-x-0.5 transition-all" />
            </div>
          ))}
        </section>

        {/* [복구] 스페셜 이벤트 배너 */}
        <section className="bg-blue-50/50 border border-blue-100 rounded-lg p-6 text-[#1A73E8] flex justify-between items-center group cursor-pointer relative overflow-hidden transition-all hover:bg-blue-50">
          <div className="relative z-10">
            <p className="text-[#1A73E8]/50 text-[10px] font-bold mb-1 tracking-widest uppercase">Special Event</p>
            <h4 className="text-[15px] font-extrabold">지금 참여하여 혜택 받기 🔥</h4>
            <p className="text-[#1A73E8]/70 text-[12px] font-bold mt-0.5">최대 5% 캐시백 이벤트</p>
          </div>
          <div className="w-10 h-10 bg-white/80 rounded-xl flex items-center justify-center shadow-sm transition-transform group-hover:translate-x-1">
            <Plus size={20} />
          </div>
        </section>

        <div className="pt-8"></div>
      </main>

      <nav className="sticky bottom-4 mx-5 bg-white/95 backdrop-blur-lg border border-gray-100 h-[68px] rounded-[20px] flex items-center justify-around shadow-2xl px-2 mb-4">
        {[
          { icon: <Home size={24} />, label: '홈' },
          { icon: <Gift size={24} />, label: '상품' },
          { icon: <LayoutGrid size={24} />, label: '자산' },
          { icon: <Menu size={24} />, label: '전체' }
        ].map((tab, idx) => (
          <div key={idx} className={`flex flex-col items-center gap-0.5 cursor-pointer transition-all ${idx === 0 ? 'text-[#1A73E8]' : 'text-gray-500 hover:text-gray-800'}`}>
            {tab.icon}
            <span className="text-[11px] font-black">{tab.label}</span>
          </div>
        ))}
      </nav>
    </div>
  );
};

export default Dashboard;
