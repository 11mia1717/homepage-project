import { Link } from 'react-router-dom';
import { motion } from 'framer-motion';

export default function HomePage() {
  const products = [
    {
      id: 'rookies-card',
      name: '루키즈 카드',
      description: '취업 준비생을 위한 특별한 혜택',
      icon: '💳',
      color: 'from-blue-500 to-blue-600',
    },
    {
      id: 'toss-savings',
      name: '토스 적금',
      description: '높은 금리와 자유로운 입출금',
      icon: '🏦',
      color: 'from-green-500 to-green-600',
    },
    {
      id: 'travel-insurance',
      name: '여행자 보험',
      description: '안전한 여행을 위한 든든한 보장',
      icon: '✈️',
      color: 'from-purple-500 to-purple-600',
    },
  ];

  return (
    <div>
      {/* 히어로 섹션 */}
      <section className="bg-gradient-to-br from-toss-blue-500 to-toss-blue-600 text-white">
        <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 py-20">
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.5 }}
            className="text-center"
          >
            <h1 className="text-4xl sm:text-5xl font-bold mb-6">
              더 나은 금융 생활을 위한
              <br />
              맞춤 상담 서비스
            </h1>
            <p className="text-lg sm:text-xl text-blue-100 mb-8 max-w-2xl mx-auto">
              토스뱅크의 다양한 금융 상품을 전문 상담사가 친절하게 안내해 드립니다.
              지금 바로 상담을 신청해 보세요.
            </p>
            <div className="flex flex-col sm:flex-row gap-4 justify-center">
              <Link
                to="/consent/루키즈 카드"
                className="inline-flex items-center justify-center px-8 py-4 bg-white text-toss-blue-500 font-semibold rounded-toss-sm hover:bg-blue-50 transition-colors"
              >
                상담 신청하기
              </Link>
              <Link
                to="/my-consent"
                className="inline-flex items-center justify-center px-8 py-4 bg-blue-400 bg-opacity-30 text-white font-semibold rounded-toss-sm hover:bg-opacity-40 transition-colors"
              >
                내 동의 내역 확인
              </Link>
            </div>
          </motion.div>
        </div>
        
        {/* 웨이브 디바이더 */}
        <div className="h-16 bg-toss-gray-50" style={{ 
          clipPath: 'ellipse(70% 100% at 50% 100%)' 
        }} />
      </section>

      {/* 상품 섹션 */}
      <section className="py-16">
        <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8">
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            whileInView={{ opacity: 1, y: 0 }}
            viewport={{ once: true }}
            transition={{ duration: 0.5 }}
            className="text-center mb-12"
          >
            <h2 className="text-3xl font-bold text-toss-gray-900 mb-4">
              상담 가능한 상품
            </h2>
            <p className="text-toss-gray-600">
              관심 있는 상품을 선택하고 전문 상담을 받아보세요
            </p>
          </motion.div>

          <div className="grid md:grid-cols-3 gap-6">
            {products.map((product, index) => (
              <motion.div
                key={product.id}
                initial={{ opacity: 0, y: 20 }}
                whileInView={{ opacity: 1, y: 0 }}
                viewport={{ once: true }}
                transition={{ duration: 0.5, delay: index * 0.1 }}
              >
                <Link
                  to={`/consent/${encodeURIComponent(product.name)}`}
                  className="block card-hover group"
                >
                  <div className={`w-14 h-14 rounded-toss bg-gradient-to-br ${product.color} flex items-center justify-center text-2xl mb-4 group-hover:scale-105 transition-transform`}>
                    {product.icon}
                  </div>
                  <h3 className="text-xl font-semibold text-toss-gray-900 mb-2 group-hover:text-toss-blue-500 transition-colors">
                    {product.name}
                  </h3>
                  <p className="text-toss-gray-600 mb-4">
                    {product.description}
                  </p>
                  <span className="inline-flex items-center text-toss-blue-500 font-medium">
                    상담 신청
                    <svg className="w-4 h-4 ml-1 group-hover:translate-x-1 transition-transform" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
                    </svg>
                  </span>
                </Link>
              </motion.div>
            ))}
          </div>
        </div>
      </section>

      {/* 안내 섹션 */}
      <section className="py-16 bg-white">
        <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8">
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            whileInView={{ opacity: 1, y: 0 }}
            viewport={{ once: true }}
            transition={{ duration: 0.5 }}
            className="text-center mb-12"
          >
            <h2 className="text-3xl font-bold text-toss-gray-900 mb-4">
              상담 진행 절차
            </h2>
          </motion.div>

          <div className="grid md:grid-cols-4 gap-6">
            {[
              { step: 1, title: '상품 선택', desc: '원하는 상품을 선택합니다', icon: '🔍' },
              { step: 2, title: '동의 절차', desc: '개인정보 제공에 동의합니다', icon: '✅' },
              { step: 3, title: '상담 연결', desc: '전문 상담사가 연락드립니다', icon: '📞' },
              { step: 4, title: '가입 완료', desc: '상담 후 상품에 가입합니다', icon: '🎉' },
            ].map((item, index) => (
              <motion.div
                key={item.step}
                initial={{ opacity: 0, y: 20 }}
                whileInView={{ opacity: 1, y: 0 }}
                viewport={{ once: true }}
                transition={{ duration: 0.5, delay: index * 0.1 }}
                className="text-center"
              >
                <div className="w-16 h-16 mx-auto bg-toss-blue-50 rounded-full flex items-center justify-center text-2xl mb-4">
                  {item.icon}
                </div>
                <div className="text-sm font-medium text-toss-blue-500 mb-2">
                  STEP {item.step}
                </div>
                <h3 className="text-lg font-semibold text-toss-gray-900 mb-2">
                  {item.title}
                </h3>
                <p className="text-sm text-toss-gray-600">
                  {item.desc}
                </p>
              </motion.div>
            ))}
          </div>
        </div>
      </section>

      {/* 개인정보 안내 섹션 */}
      <section className="py-16 bg-toss-gray-100">
        <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8">
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            whileInView={{ opacity: 1, y: 0 }}
            viewport={{ once: true }}
            transition={{ duration: 0.5 }}
            className="card"
          >
            <div className="flex items-start gap-4">
              <div className="w-12 h-12 bg-toss-blue-50 rounded-full flex items-center justify-center flex-shrink-0">
                <svg className="w-6 h-6 text-toss-blue-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z" />
                </svg>
              </div>
              <div>
                <h3 className="text-lg font-semibold text-toss-gray-900 mb-2">
                  개인정보 보호 안내
                </h3>
                <p className="text-toss-gray-600 mb-4">
                  고객님의 개인정보는 상담 목적으로만 사용되며, 상담 완료 후 3개월 이내에 
                  자동으로 파기됩니다. 언제든지 동의를 철회하실 수 있습니다.
                </p>
                <ul className="space-y-2 text-sm text-toss-gray-600">
                  <li className="flex items-center gap-2">
                    <svg className="w-4 h-4 text-toss-green-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
                    </svg>
                    제3자 제공 동의 시에만 상담사에게 정보 전달
                  </li>
                  <li className="flex items-center gap-2">
                    <svg className="w-4 h-4 text-toss-green-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
                    </svg>
                    상담 목적 외 사용 절대 금지
                  </li>
                  <li className="flex items-center gap-2">
                    <svg className="w-4 h-4 text-toss-green-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
                    </svg>
                    보유기간 만료 시 자동 파기
                  </li>
                </ul>
              </div>
            </div>
          </motion.div>
        </div>
      </section>
    </div>
  );
}
