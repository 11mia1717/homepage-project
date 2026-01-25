package com.entrusting.backend.user.controller;

import com.entrusting.backend.user.entity.Account;
import com.entrusting.backend.user.service.AccountService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping("/create")
    public ResponseEntity<?> createAccount(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String accountName = request.get("accountName");

        try {
            System.out.println(
                    "[ENTRUSTING-DEBUG] Create Account Request - User: " + username + ", Name: " + accountName);
            Account account = accountService.createAccount(username, accountName);
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("accountNumber", account.getAccountNumber());
            response.put("balance", account.getBalance());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("[ENTRUSTING-ERROR] Account Creation Failed: " + e.getMessage());
            String errorMessage = (e.getMessage() != null) ? e.getMessage() : e.toString();
            return ResponseEntity.badRequest().body("서버 오류: " + errorMessage);
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
}
