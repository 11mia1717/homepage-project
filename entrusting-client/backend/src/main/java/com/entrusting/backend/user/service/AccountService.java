package com.entrusting.backend.user.service;

import com.entrusting.backend.user.entity.Account;
import com.entrusting.backend.user.entity.User;
import com.entrusting.backend.user.repository.AccountRepository;
import com.entrusting.backend.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Random;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final Random random = new Random();

    public AccountService(AccountRepository accountRepository, UserRepository userRepository) {
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Account createAccount(String username, String accountName) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + username));

        // 랜덤 계좌번호 생성 (예: 110-123-456789)
        String accountNumber = String.format("3333-%02d-%06d", random.nextInt(100), random.nextInt(1000000));

        // 가입 축하금 1000원 설정
        Account newAccount = new Account(accountNumber, accountName, new BigDecimal("1000"), user);
        return accountRepository.save(newAccount);
    }

    @Transactional(readOnly = true)
    public List<Account> getAccountsByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + username));
        return accountRepository.findByUser(user);
    }
}
