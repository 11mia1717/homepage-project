package com.authcompany.backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Collections;
import java.util.Map;

@RestController
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "Welcome to the Auth Company Backend!";
    }

    @GetMapping("/api/health")
    public Map<String, String> healthCheck() {
        return Collections.singletonMap("status", "OK");
    }
}
