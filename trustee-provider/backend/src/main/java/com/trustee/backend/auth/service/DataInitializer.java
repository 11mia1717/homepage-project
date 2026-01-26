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
                new CarrierUser("김중수", "01095119924", "SKT"),
                new CarrierUser("방수진", "01087176882", "KT"),
                new CarrierUser("김은수", "01051337437", "LGU+"),
                new CarrierUser("이광진", "01030659593", "ALDDLE"),
                new CarrierUser("임혜진", "01037315819", "SKT"),
                new CarrierUser("전용준", "01050470664", "KT"),
                new CarrierUser("김유진", "01092877379", "LGU+"),
                new CarrierUser("장민아", "01049328977", "SKT"),
                new CarrierUser("이승원", "01092128221", "KT"),
                new CarrierUser("홍길동", "01000000000", "SKT")
            );

            carrierUserRepository.saveAll(initialUsers);
            System.out.println("[TRUSTEE-DB] 10 Mock Carrier Users inserted successfully.");
        } else {
            System.out.println("[TRUSTEE-DB] CarrierUser data already exists. Skipping initialization.");
        }
    }
}
