const express = require('express');
const cors = require('cors');
const bodyParser = require('body-parser');

// 위탁사 백엔드 (포트 8085)
const entrustingApp = express();
entrustingApp.use(cors());
entrustingApp.use(bodyParser.json());

// 간단한 메모리 저장소
let users = [];
let accounts = [];

// 로그인
entrustingApp.post('/api/v1/auth/login', (req, res) => {
  const { username, password } = req.body;
  const user = users.find(u => u.username === username && u.password === password);
  
  if (user) {
    res.json({
      name: user.name,
      phoneNumber: user.phoneNumber
    });
  } else {
    res.status(401).send('아이디 또는 비밀번호를 확인해 주세요.');
  }
});

// 회원가입
entrustingApp.post('/api/v1/auth/register', (req, res) => {
  const { name, username, password, phoneNumber, tokenId, termsAgreement } = req.body;
  
  // 중복 체크
  if (users.find(u => u.username === username)) {
    return res.status(400).send('이미 사용 중인 아이디입니다.');
  }
  
  users.push({ name, username, password, phoneNumber, verified: true });
  res.send('회원가입이 완료되었습니다.');
});

// 계좌 목록
entrustingApp.get('/api/v1/accounts', (req, res) => {
  res.json(accounts);
});

// 계좌 생성
entrustingApp.post('/api/v1/accounts', (req, res) => {
  const newAccount = {
    id: accounts.length + 1,
    accountNumber: `110-001-${String(accounts.length + 1).padStart(6, '0')}`,
    balance: 10000, // 가입 축하금
    createdAt: new Date().toISOString()
  };
  accounts.push(newAccount);
  res.json(newAccount);
});

entrustingApp.listen(8085, () => {
  console.log('Mock 위탁사 백엔드가 포트 8085에서 실행 중...');
});

// 수탁사 백엔드 (포트 8086)
const trusteeApp = express();
trusteeApp.use(cors());
trusteeApp.use(bodyParser.json());

let authTokens = {};

// 본인인증 초기화
trusteeApp.post('/api/v1/auth/init', (req, res) => {
  const tokenId = Math.random().toString(36).substring(7);
  const otp = Math.floor(100000 + Math.random() * 900000).toString();
  
  authTokens[tokenId] = {
    ...req.body,
    otp,
    status: 'PENDING'
  };
  
  res.json({ tokenId, otp }); // 테스트 모드이므로 OTP 반환
});

// OTP 확인
trusteeApp.patch('/api/v1/auth/confirm', (req, res) => {
  const { tokenId, otp } = req.body;
  
  if (authTokens[tokenId] && authTokens[tokenId].otp === otp) {
    authTokens[tokenId].status = 'COMPLETED';
    res.status(204).send();
  } else {
    res.status(400).send('인증번호가 일치하지 않습니다.');
  }
});

// 토큰 상태 확인
trusteeApp.get('/api/v1/auth/status/:tokenId', (req, res) => {
  const { tokenId } = req.params;
  const token = authTokens[tokenId];
  
  if (token) {
    res.json({
      tokenId,
      status: token.status,
      name: token.name
    });
  } else {
    res.status(404).send('토큰을 찾을 수 없습니다.');
  }
});

trusteeApp.listen(8086, () => {
  console.log('Mock 수탁사 백엔드가 포트 8086에서 실행 중...');
});

console.log('Mock 백엔드 서버들이 시작되었습니다.');
console.log('위탁사: http://localhost:8085');
console.log('수탁사: http://localhost:8086');