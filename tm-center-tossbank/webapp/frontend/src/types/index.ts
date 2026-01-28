// API 응답 타입
export interface ApiResponse<T> {
  success: boolean;
  message?: string;
  data?: T;
  error?: {
    code: string;
    message: string;
    detail?: string;
  };
  timestamp: string;
}

// 동의 요청 타입
export interface ConsentRequest {
  customerName: string;
  customerPhone: string;
  productName: string;
  agreeThirdPartyProvision: boolean;
  agreeMarketing: boolean;
}

// 동의 응답 타입
export interface ConsentResponse {
  requestId: string;
  productName: string;
  consentPurpose: string;
  consentRecipient: string;
  consentedAt: string;
  retentionUntil: string;
  status: string;
  message: string;
}

// 동의서 내용 타입
export interface AgreementContent {
  productName: string;
  recipient: string;
  purpose: string;
  items: string;
  retentionPeriod: string;
  refusalInfo: string;
}

// TM 타겟 타입
export interface TmTarget {
  id: number;
  externalRef: string;
  customerName: string;
  phone: string;
  productName: string;
  consentPurpose: string;
  retentionUntil: string;
  status: string;
  priority: number;
  assignedAgentName?: string;
  assignedAt?: string;
  destroyed: boolean;
  warningMessage: string;
}

// 상담 결과 요청 타입
export interface CallResultRequest {
  tmTargetId: number;
  recordingAgreed: boolean;
  resultCode: ResultCode;
  resultDetail?: string;
  consultedProduct: string;
  productResult?: ProductResult;
  callbackScheduledAt?: string;
  retryAgreed?: boolean;
  memo?: string;
}

// 상담 결과 응답 타입
export interface CallResult {
  id: number;
  tmTargetId: number;
  customerName: string;
  agentId: number;
  agentName: string;
  callStartedAt: string;
  callEndedAt: string;
  callDuration: number;
  formattedDuration: string;
  recordingAgreed: boolean;
  recorded: boolean;
  recordingFileUrl?: string;
  resultCode: string;
  resultCodeLabel: string;
  resultDetail?: string;
  consultedProduct: string;
  productResult?: string;
  productResultLabel?: string;
  callbackScheduledAt?: string;
  retryAgreed: boolean;
  memo?: string;
  createdAt: string;
}

// 상담 스크립트 타입
export interface ConsultationScript {
  greeting: string;
  recordingNotice: string;
  recordingRequired: boolean;
  retryNotice: string;
  closingMessage: string;
}

// 대시보드 통계 타입
export interface DashboardStats {
  totalRequests: number;
  pendingRequests: number;
  inProgressRequests: number;
  completedRequests: number;
  withdrawnRequests: number;
  todayConsents: number;
  todayCalls: number;
  todayCompletions: number;
  callResultStats: Record<string, number>;
  productStats: Record<string, ProductStats>;
  agentStats: AgentStats[];
  dailyStats: DailyStats[];
}

export interface ProductStats {
  productName: string;
  totalCount: number;
  agreedCount: number;
  pendingCount: number;
  refusedCount: number;
  conversionRate: number;
}

export interface AgentStats {
  agentId: number;
  agentName: string;
  totalCalls: number;
  successCalls: number;
  callbackCalls: number;
  successRate: number;
  avgCallDuration: number;
}

export interface DailyStats {
  date: string;
  consents: number;
  calls: number;
  completions: number;
}

// 접근 로그 타입
export interface AccessLog {
  id: number;
  agentId: number;
  agentName: string;
  targetId: number;
  targetType: string;
  accessType: string;
  accessPurpose: string;
  ipAddress: string;
  userAgent?: string;
  accessedAt: string;
}

// 사용자 타입
export interface User {
  id: number;
  username: string;
  name: string;
  email?: string;
  role: 'ADMIN' | 'MANAGER' | 'AGENT';
  department?: string;
}

// 상담 결과 코드
export type ResultCode = 
  | 'SUCCESS' 
  | 'NO_ANSWER' 
  | 'BUSY' 
  | 'CALLBACK' 
  | 'REFUSED' 
  | 'WRONG_NUMBER' 
  | 'OTHER';

// 상품 결과
export type ProductResult = 'AGREED' | 'PENDING' | 'REFUSED';

// 타겟 상태
export type TargetStatus = 
  | 'WAITING' 
  | 'ASSIGNED' 
  | 'IN_CALL' 
  | 'COMPLETED' 
  | 'CALLBACK' 
  | 'WITHDRAWN';
