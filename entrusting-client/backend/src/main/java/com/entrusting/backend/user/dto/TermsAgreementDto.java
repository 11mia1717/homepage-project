package com.entrusting.backend.user.dto;

import java.util.Map;

public class TermsAgreementDto {
    private Map<String, Boolean> agreements;
    private Map<String, Boolean> marketingChannels;

    public boolean isAllRequiredAgreed() {
        if (agreements == null) return false;
        // [COMPLIANCE] 금융권 필수 약관 9종 검증
        return agreements.getOrDefault("age", false) &&             // 만 14세 이상
               agreements.getOrDefault("terms", false) &&           // 서비스 이용약관
               agreements.getOrDefault("privacy", false) &&         // 개인정보 수집·이용
               agreements.getOrDefault("uniqueId", false) &&        // 고유식별정보 처리
               agreements.getOrDefault("creditInfo", false) &&      // 신용정보 조회·제공
               agreements.getOrDefault("carrierAuth", false) &&     // V-pass 서비스 이용
               agreements.getOrDefault("vpassProvision", false) &&  // V-pass 정보 제공
               agreements.getOrDefault("electronicFinance", false) && // 전자금융거래 기본
               agreements.getOrDefault("monitoring", false);        // 거래 모니터링/AML
    }

    public Map<String, Boolean> getAgreements() {
        return agreements;
    }

    public void setAgreements(Map<String, Boolean> agreements) {
        this.agreements = agreements;
    }

    public Map<String, Boolean> getMarketingChannels() {
        return marketingChannels;
    }

    public void setMarketingChannels(Map<String, Boolean> marketingChannels) {
        this.marketingChannels = marketingChannels;
    }
}
