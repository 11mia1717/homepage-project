package com.entrusting.backend.user.controller;

import com.entrusting.backend.user.entity.Account;
import com.entrusting.backend.user.service.AccountService;
import com.entrusting.backend.user.service.S2SAuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/accounts")
public class AccountController {

    private final AccountService accountService;
    private final S2SAuthService s2sAuthService;

    public AccountController(AccountService accountService, S2SAuthService s2sAuthService) {
        this.accountService = accountService;
        this.s2sAuthService = s2sAuthService;
    }

    @PostMapping("/create")
    public ResponseEntity<?> createAccount(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String accountName = request.get("accountName");
        String rawPin = request.get("pin");
        String tokenId = request.get("tokenId");

        try {
            System.out.println(
                    "[ENTRUSTING-DEBUG] Create Account Request - User: [" + username + "], TokenId: [" + tokenId + "]");

            // [Server-to-Server 검증 고도화]
            if (tokenId == null || tokenId.isEmpty()) {
                throw new IllegalArgumentException("본인인증 토큰이 누락되었습니다.");
            }

            Map<String, Object> verification = s2sAuthService.verifyTokenWithTrustee(tokenId);
            if (verification == null || !"COMPLETED".equals(verification.get("status"))) {
                throw new IllegalArgumentException("본인인증이 완료되지 않았거나 유효하지 않은 토큰입니다.");
            }

            // 추가 검증: 인증된 이름/번호가 현재 계정 정보와 일치하는지 확인 (선택 사항이나 보안상 권장)
            // 여기서는 단순 성공 여부만 우선 체크하도록 구성함

            Account account = accountService.createAccount(username, accountName, rawPin);
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("accountNumber", account.getAccountNumber());
            response.put("balance", account.getBalance());
            // [추가] 축하금 지급 여부 반환 (잔액이 0보다 크면 지급된 것으로 간주)
            response.put("bonusApplied", account.getBalance().compareTo(java.math.BigDecimal.ZERO) > 0);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(
                    "[ENTRUSTING-ERROR] Account Creation Failed for user [" + username + "]: " + e.getMessage());
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", e.getMessage() != null ? e.getMessage() : "서버 내부 오류가 발생했습니다.");
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @GetMapping("/list")
    public ResponseEntity<?> getAccounts(@RequestParam String username) {
        try {
            List<Account> accounts = accountService.getAccountsByUsername(username);
            List<Map<String, Object>> responseList = accounts.stream().map(acc -> {
                Map<String, Object> map = new HashMap<>();
                map.put("accountNumber", acc.getAccountNumber());
                map.put("accountName", acc.getAccountName());
                map.put("balance", acc.getBalance());
                return map;
            }).collect(Collectors.toList());
            return ResponseEntity.ok(responseList);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/transfer")
    public ResponseEntity<?> transfer(@RequestBody Map<String, String> request) {
        String from = request.get("fromAccountNumber");
        String to = request.get("toAccountNumber");
        BigDecimal amount = new BigDecimal(request.get("amount"));
        String pin = request.get("pin");

        try {
            accountService.transferFunds(from, to, amount, pin);
            return ResponseEntity.ok(Map.of("status", "success", "message", "이체가 완료되었습니다."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("status", "error", "message", e.getMessage()));
        }
    }
}
