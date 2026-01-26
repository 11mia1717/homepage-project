package com.entrusting.backend.user.service;

import com.entrusting.backend.user.entity.Account;
import com.entrusting.backend.user.entity.Transaction;
import com.entrusting.backend.user.entity.User;
import com.entrusting.backend.user.repository.AccountRepository;
import com.entrusting.backend.user.repository.TransactionRepository;
import com.entrusting.backend.user.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final Random random = new Random();

    public AccountService(AccountRepository accountRepository, UserRepository userRepository,
            TransactionRepository transactionRepository, BCryptPasswordEncoder passwordEncoder) {
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public Account createAccount(String username, String accountName, String rawPin) {
        System.out.println("[ENTRUSTING-DEBUG] createAccount Service - PIN length: "
                + (rawPin != null ? rawPin.length() : "NULL"));
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    System.err.println("[ENTRUSTING-DEBUG] User NOT FOUND: " + username);
                    return new IllegalArgumentException("사용자를 찾을 수 없습니다: " + username);
                });

        System.out.println("[ENTRUSTING-DEBUG] User found: " + user.getUsername() + ", Proceeding with creation.");

        // 12자리 계좌번호 생성 (예: 3333-01-123456)
        String accountNumber = String.format("3333-%02d-%06d", random.nextInt(100), random.nextInt(1000000));

        // PIN 해싱 (Salt는 BCrypt에서 내부적으로 관리하지만, 요구사항에 맞춰 시뮬레이션 가능)
        // 여기서는 BCrypt 자체가 Salt를 포함하므로 passwordEncoder 활용
        String salt = UUID.randomUUID().toString().substring(0, 8);
        String hashedPin = passwordEncoder.encode(rawPin + salt);

        System.out.println("[ENTRUSTING-DEBUG] Salt generated and PIN hashed. Checking for existing accounts...");

        // [비즈니스 로직 적용] 첫 번째 계좌에만 가입 축하금 10,000원 지급
        List<Account> existingAccounts = accountRepository.findByUser(user);
        boolean isFirstAccount = existingAccounts.isEmpty();
        BigDecimal initialBalance = isFirstAccount ? new BigDecimal("10000") : BigDecimal.ZERO;

        System.out.println(
                "[ENTRUSTING-DEBUG] Is first account: " + isFirstAccount + ", Initial balance: " + initialBalance);

        // 계좌 정보 저장
        Account newAccount = new Account(accountNumber, accountName, initialBalance, user, hashedPin, salt);
        Account savedAccount = accountRepository.save(newAccount);

        // 첫 번째 계좌인 경우에만 입금 내역 기록
        if (isFirstAccount) {
            Transaction initialDeposit = new Transaction(null, savedAccount, initialBalance, "DEPOSIT", "가입 축하금");
            transactionRepository.save(initialDeposit);
            System.out.println("[ENTRUSTING-DEBUG] Bonus 10,000 won applied to the first account.");
        } else {
            System.out.println("[ENTRUSTING-DEBUG] No bonus applied for subsequent account.");
        }

        return savedAccount;
    }

    @Transactional
    public void transferFunds(String fromAccountNumber, String toAccountNumber, BigDecimal amount, String pin) {
        Account fromAccount = accountRepository.findByAccountNumber(fromAccountNumber)
                .orElseThrow(() -> new IllegalArgumentException("출금 계좌를 찾을 수 없습니다."));

        Account toAccount = accountRepository.findByAccountNumber(toAccountNumber)
                .orElseThrow(() -> new IllegalArgumentException("입금 계좌를 찾을 수 없습니다."));

        // [추가] 비밀번호 오류 횟수 및 상태 확인
        if ("SUSPENDED".equals(fromAccount.getStatus()) || fromAccount.getPinFailCount() >= 5) {
            throw new IllegalArgumentException("비밀번호 5회 오류로 인해 계좌가 정지되었습니다. 고객센터에 문의하세요.");
        }

        if (!passwordEncoder.matches(pin + fromAccount.getSalt(), fromAccount.getAccountPin())) {
            // PIN 불일치 - 카운트 증가
            int newCount = fromAccount.getPinFailCount() + 1;
            fromAccount.setPinFailCount(newCount);
            if (newCount >= 5) {
                fromAccount.setStatus("SUSPENDED");
            }
            accountRepository.save(fromAccount);

            String errorMsg = "계좌 비밀번호가 일치하지 않습니다.";
            if (newCount >= 5) {
                errorMsg = "비밀번호 5회 연속 불일치로 계좌가 정지되었습니다.";
            } else {
                errorMsg += " (현재 " + newCount + "회 연속 오류)";
            }
            throw new IllegalArgumentException(errorMsg);
        }

        // 성공 시 오류 카운트 초기화
        fromAccount.setPinFailCount(0);

        if (fromAccount.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("잔액이 부족합니다.");
        }

        fromAccount.setBalance(fromAccount.getBalance().subtract(amount));
        toAccount.setBalance(toAccount.getBalance().add(amount));

        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);

        Transaction transaction = new Transaction(fromAccount, toAccount, amount, "TRANSFER", "계좌 이체");
        transactionRepository.save(transaction);
    }

    @Transactional(readOnly = true)
    public List<Account> getAccountsByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + username));
        return accountRepository.findByUser(user);
    }
}
