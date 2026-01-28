import axios, { AxiosError } from 'axios';
import type { 
  ApiResponse, 
  ConsentRequest, 
  ConsentResponse, 
  AgreementContent,
  TmTarget,
  CallResultRequest,
  CallResult,
  ConsultationScript,
  DashboardStats,
  AccessLog 
} from '../types';

const api = axios.create({
  baseURL: '/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
});

// 요청 인터셉터 - 상담사 ID 추가
api.interceptors.request.use((config) => {
  const agentId = localStorage.getItem('agentId');
  if (agentId) {
    config.headers['X-Agent-Id'] = agentId;
  }
  return config;
});

// 응답 인터셉터 - 에러 처리
api.interceptors.response.use(
  (response) => response,
  (error: AxiosError<ApiResponse<unknown>>) => {
    const message = error.response?.data?.error?.message || '오류가 발생했습니다.';
    return Promise.reject(new Error(message));
  }
);

// ==================== 마케팅 동의 API ====================

export const consentApi = {
  // 동의서 내용 조회
  getAgreement: async (productName: string): Promise<AgreementContent> => {
    const response = await api.get<ApiResponse<AgreementContent>>(
      `/consent/agreement/${encodeURIComponent(productName)}`
    );
    return response.data.data!;
  },

  // 마케팅 동의 처리
  processConsent: async (data: ConsentRequest): Promise<ConsentResponse> => {
    const response = await api.post<ApiResponse<ConsentResponse>>('/consent', data);
    return response.data.data!;
  },

  // 동의 철회
  withdrawConsent: async (requestId: string, reason?: string): Promise<void> => {
    await api.post('/consent/withdraw', { requestId, reason });
  },

  // 동의 내역 조회
  getConsentHistory: async (phone: string): Promise<ConsentResponse[]> => {
    const response = await api.get<ApiResponse<ConsentResponse[]>>(
      `/consent/history?phone=${encodeURIComponent(phone)}`
    );
    return response.data.data || [];
  },
};

// ==================== TM 상담 API ====================

export const tmApi = {
  // 다음 고객 불러오기
  getNextTarget: async (): Promise<TmTarget | null> => {
    const response = await api.post<ApiResponse<TmTarget | null>>('/tm/next');
    return response.data.data || null;
  },

  // 배정된 고객 목록 조회
  getAssignedTargets: async (page = 0, size = 10): Promise<{ content: TmTarget[]; totalElements: number }> => {
    const response = await api.get<ApiResponse<{ content: TmTarget[]; totalElements: number }>>(
      `/tm/targets?page=${page}&size=${size}`
    );
    return response.data.data || { content: [], totalElements: 0 };
  },

  // 고객 상세 조회
  getTargetDetail: async (targetId: number): Promise<TmTarget> => {
    const response = await api.get<ApiResponse<TmTarget>>(`/tm/targets/${targetId}`);
    return response.data.data!;
  },

  // 상담 시작
  startCall: async (targetId: number): Promise<void> => {
    await api.post(`/tm/targets/${targetId}/start`);
  },

  // 상담 결과 저장
  saveCallResult: async (data: CallResultRequest): Promise<CallResult> => {
    const response = await api.post<ApiResponse<CallResult>>('/tm/results', data);
    return response.data.data!;
  },

  // 상담 이력 조회
  getCallHistory: async (targetId: number): Promise<CallResult[]> => {
    const response = await api.get<ApiResponse<CallResult[]>>(`/tm/targets/${targetId}/history`);
    return response.data.data || [];
  },

  // 재통화 대상 조회
  getCallbackTargets: async (): Promise<TmTarget[]> => {
    const response = await api.get<ApiResponse<TmTarget[]>>('/tm/callbacks');
    return response.data.data || [];
  },

  // 상담 스크립트 조회
  getScript: async (): Promise<ConsultationScript> => {
    const response = await api.get<ApiResponse<ConsultationScript>>('/tm/script');
    return response.data.data!;
  },
};

// ==================== 관리자 API ====================

export const adminApi = {
  // 대시보드 통계
  getDashboardStats: async (): Promise<DashboardStats> => {
    const response = await api.get<ApiResponse<DashboardStats>>('/admin/dashboard');
    return response.data.data!;
  },

  // 접근 로그 조회
  getAccessLogs: async (
    start: string, 
    end: string, 
    page = 0, 
    size = 20
  ): Promise<{ content: AccessLog[]; totalElements: number }> => {
    const response = await api.get<ApiResponse<{ content: AccessLog[]; totalElements: number }>>(
      `/admin/access-logs?start=${start}&end=${end}&page=${page}&size=${size}`
    );
    return response.data.data || { content: [], totalElements: 0 };
  },

  // 사용자별 접근 로그
  getAgentAccessLogs: async (
    agentId: number, 
    page = 0, 
    size = 20
  ): Promise<{ content: AccessLog[]; totalElements: number }> => {
    const response = await api.get<ApiResponse<{ content: AccessLog[]; totalElements: number }>>(
      `/admin/access-logs/agent/${agentId}?page=${page}&size=${size}`
    );
    return response.data.data || { content: [], totalElements: 0 };
  },

  // 접근 유형별 통계
  getAccessTypeStats: async (days = 7): Promise<Record<string, number>> => {
    const response = await api.get<ApiResponse<Record<string, number>>>(
      `/admin/access-stats/type?days=${days}`
    );
    return response.data.data || {};
  },

  // 사용자별 접근 통계
  getAgentAccessStats: async (days = 7): Promise<Array<{ agentId: number; agentName: string; accessCount: number }>> => {
    const response = await api.get<ApiResponse<Array<{ agentId: number; agentName: string; accessCount: number }>>>(
      `/admin/access-stats/agent?days=${days}`
    );
    return response.data.data || [];
  },

  // 수동 파기 실행
  executeDestroy: async (): Promise<{ destroyedCount: number; message: string }> => {
    const response = await api.post<ApiResponse<{ destroyedCount: number; message: string }>>('/admin/destroy');
    return response.data.data!;
  },

  // 파기 예정 건수 조회
  getPendingDestroyCount: async (): Promise<{ pendingCount: number; nextScheduledAt: string }> => {
    const response = await api.get<ApiResponse<{ pendingCount: number; nextScheduledAt: string }>>('/admin/destroy/pending');
    return response.data.data!;
  },
};

export default api;
