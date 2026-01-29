package com.trustee.backend.auth.service;

import com.trustee.backend.auth.entity.CarrierUser;
import com.trustee.backend.auth.repository.CarrierUserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private final CarrierUserRepository carrierUserRepository;

    public DataInitializer(CarrierUserRepository carrierUserRepository) {
        this.carrierUserRepository = carrierUserRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        if (carrierUserRepository.count() == 0) {
            System.out.println("[TRUSTEE-DB] Initializing CarrierUser data...");

            List<CarrierUser> initialUsers = Arrays.asList(
                new CarrierUser("김중수", "01095119924", "SKT", "850101"),
                new CarrierUser("방수진", "01087176882", "KT", "900101"),
                new CarrierUser("김은수", "01051337437", "LGU+", "920101"),
                new CarrierUser("이광진", "01030659693", "ALDDLE", "880101"),
                new CarrierUser("임혜진", "01037315819", "SKT", "940101"),
                new CarrierUser("전용준", "01050470664", "KT", "820101"),
                new CarrierUser("김유진", "01092877379", "LGU+", "950101"),
                new CarrierUser("장민아", "01049328977", "SKT", "910101"),
                new CarrierUser("이승원", "01092128221", "KT", "890101"),
                new CarrierUser("홍길동", "01000000000", "SKT", "000101"),
                // Requested Dummy Data
                new CarrierUser("홍길순", "01000000001", "SKT", "010101"),
                new CarrierUser("고길동", "01000000002", "SKT", "750101"),
                new CarrierUser("차은우", "01011111111", "SKT", "970330")
            );

            carrierUserRepository.saveAll(initialUsers);
            System.out.println("[TRUSTEE-DB] 10 Mock Carrier Users inserted successfully.");
        } else {
            System.out.println("[TRUSTEE-DB] CarrierUser data already exists. Skipping initialization.");
        }
    }
}
