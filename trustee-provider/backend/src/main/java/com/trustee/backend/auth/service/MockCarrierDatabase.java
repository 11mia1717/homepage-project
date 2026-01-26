package com.trustee.backend.auth.service;

import org.springframework.stereotype.Component;
import com.trustee.backend.auth.entity.CarrierUser;
import com.trustee.backend.auth.repository.CarrierUserRepository;
import java.util.Optional;

@Component
public class MockCarrierDatabase {

    private final CarrierUserRepository carrierUserRepository;

    public MockCarrierDatabase(CarrierUserRepository carrierUserRepository) {
        this.carrierUserRepository = carrierUserRepository;
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
        
        Optional<CarrierUser> userOpt = carrierUserRepository.findByPhoneNumber(cleanPhone);

        if (userOpt.isEmpty()) {
            System.out.println("[TRUSTEE-DB] User not found for phone: " + phoneNumber);
            return false;
        }

        CarrierUser user = userOpt.get();
        boolean isNameMatch = user.getName().equals(name);
        boolean isCarrierMatch = carrier != null && user.getCarrier().equals(carrier);

        System.out
                .println("[TRUSTEE-DB] Verifying for " + phoneNumber + ": NameMatch=" + isNameMatch + ", CarrierMatch="
                        + isCarrierMatch + " (Input: " + carrier + ", Found: " + user.getCarrier() + ")");

        return isNameMatch && isCarrierMatch;
    }
}
