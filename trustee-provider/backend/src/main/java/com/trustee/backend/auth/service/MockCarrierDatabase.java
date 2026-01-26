package com.trustee.backend.auth.service;

import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;

@Component
public class MockCarrierDatabase {

    private static class CarrierRecord {
        String name;
        String carrier;

        CarrierRecord(String name, String carrier) {
            this.name = name;
            this.carrier = carrier;
        }
    }

    private final Map<String, CarrierRecord> carrierDatabase = new HashMap<>();

    public MockCarrierDatabase() {
        // 김중수 (010-9511-9924) - SKT
        carrierDatabase.put("01095119924", new CarrierRecord("김중수", "SKT"));
        // 방수진 (010-8717-6882) - KT
        carrierDatabase.put("01087176882", new CarrierRecord("방수진", "KT"));
        // 김은수 (010-5133-7437) - LGU+
        carrierDatabase.put("01051337437", new CarrierRecord("김은수", "LGU+"));
        // 이광진 (010-3065-9593) - ALDDLE
        carrierDatabase.put("01030659593", new CarrierRecord("이광진", "ALDDLE"));
        // 임혜진 (010-3731-5819) - SKT
        carrierDatabase.put("01037315819", new CarrierRecord("임혜진", "SKT"));
        // 전용준 (010-5047-0664) - KT
        carrierDatabase.put("01050470664", new CarrierRecord("전용준", "KT"));
        // 김유진 (010-9287-7379) - LGU+
        carrierDatabase.put("01092877379", new CarrierRecord("김유진", "LGU+"));
        // 장민아 (010-4932-8977) - SKT
        carrierDatabase.put("01049328977", new CarrierRecord("장민아", "SKT"));
        // 이승원 (010-9212-8221) - KT
        carrierDatabase.put("01092128221", new CarrierRecord("이승원", "KT"));
        // 홍길동 (010-0000-0000) - SKT
        carrierDatabase.put("01000000000", new CarrierRecord("홍길동", "SKT"));
    }

    /**
     * 통신사 명의 확인 (전화번호, 성명, 통신사 일치 여부)
     * 
     * @param phoneNumber 숫자로만 구성된 전화번호
     * @param name        성명
     * @param carrier     통신사 (SKT, KT, LGU+, ALDDLE)
     * @return 일치 여부
     */
    public boolean verifyIdentity(String phoneNumber, String name, String carrier) {
        String cleanPhone = phoneNumber.replaceAll("\\D", "");
        CarrierRecord record = carrierDatabase.get(cleanPhone);

        if (record == null)
            return false;

        boolean isNameMatch = record.name.equals(name);
        boolean isCarrierMatch = carrier != null && record.carrier.equals(carrier);

        System.out
                .println("[TRUSTEE-DB] Verifying for " + phoneNumber + ": NameMatch=" + isNameMatch + ", CarrierMatch="
                        + isCarrierMatch + " (Input: " + carrier + ", Found: " + record.carrier + ")");

        return isNameMatch && isCarrierMatch;
    }
}
