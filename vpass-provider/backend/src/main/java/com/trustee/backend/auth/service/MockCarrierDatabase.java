package com.trustee.backend.auth.service;

import org.springframework.stereotype.Component;
import com.trustee.backend.auth.entity.CarrierUser;
import com.trustee.backend.auth.repository.CarrierUserRepository;
import java.util.Optional;
import java.text.Normalizer; // Added for Unicode normalization
import java.io.FileWriter; // FileWriter import 추가
import java.io.IOException; // IOException import 추가

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

        try (FileWriter fw = new FileWriter("c:/ContinueProject/debug_auth.log", true)) { // FileWriter 사용, 파일 경로 변경
            fw.write("[" + java.time.LocalDateTime.now() + "] --- verifyIdentity Debugging ---\n");
            if (userOpt.isEmpty()) {
                fw.write("  [TRUSTEE-DB] User not found for phone: " + phoneNumber + "\n");
                fw.write("--- End verifyIdentity Debugging ---\n\n");
                return false;
            }

            CarrierUser user = userOpt.get();
            // [Fix] 유니코드 정규화(NFC) 적용하여 한글 자모 분리 현상(Windows/Mac/DB) 방지 및 trim 적용
            String normalizedUserName = Normalizer.normalize(user.getName().trim(), Normalizer.Form.NFC);
            String normalizedInputName = Normalizer.normalize(name.trim(), Normalizer.Form.NFC);
            
            boolean isNameMatch = normalizedUserName.equals(normalizedInputName);
            boolean isCarrierMatch = carrier != null && user.getCarrier().equals(carrier);

            fw.write("  [TRUSTEE-DB] Verifying for " + phoneNumber + ": NameMatch=" + isNameMatch + ", CarrierMatch="
                            + isCarrierMatch + " (Input: " + carrier + ", Found: " + user.getCarrier() + ")\n");
            fw.write("  [TRUSTEE-DB] Normalized Name Comparison - DB: [" + normalizedUserName + "] (Len: " + normalizedUserName.length() + "), Input: [" + normalizedInputName + "] (Len: " + normalizedInputName.length() + ")\n");
            fw.write("--- End verifyIdentity Debugging ---\n\n");

            return isNameMatch && isCarrierMatch;
        } catch (IOException e) { // IOException 처리
            System.err.println("Error writing to debug_auth.log: " + e.getMessage());
            // 에러 발생 시 기존 로직 유지
            if (userOpt.isEmpty()) {
                System.out.println("[TRUSTEE-DB] User not found for phone: " + phoneNumber);
                return false;
            }
            CarrierUser user = userOpt.get();
            String normalizedUserName = Normalizer.normalize(user.getName().trim(), Normalizer.Form.NFC);
            String normalizedInputName = Normalizer.normalize(name.trim(), Normalizer.Form.NFC);
            boolean isNameMatch = normalizedUserName.equals(normalizedInputName);
            boolean isCarrierMatch = carrier != null && user.getCarrier().equals(carrier);
            System.out
                    .println("[TRUSTEE-DB] Verifying for " + phoneNumber + ": NameMatch=" + isNameMatch + ", CarrierMatch="
                            + isCarrierMatch + " (Input: " + carrier + ", Found: " + user.getCarrier() + ")");
            System.out.println("[TRUSTEE-DB] Normalized Name Comparison - DB: [" + normalizedUserName + "] (Len: " + normalizedUserName.length() + "), Input: [" + normalizedInputName + "] (Len: " + normalizedInputName.length() + ")");
            return isNameMatch && isCarrierMatch;
        }
    }

    /**
     * CI (Connecting Information) 생성 (Mock)
     * 실제로는 88byte의 암호화된 값이지만, 여기서는 전화번호 해시를 사용
     */
    public String generateCI(String phoneNumber) {
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(phoneNumber.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return java.util.Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("CI Generation Failed", e);
        }
    }
}
