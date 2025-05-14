package com.example.cvgenerator.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class HealthCheckController {

    @GetMapping("/api/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        return ResponseEntity.ok(response);
    }

    // Railway перевіряє кореневий шлях за замовчуванням,
    // цей метод буде відповідати на запити, якщо користувач відвідує API напряму
    @GetMapping("/")
    public ResponseEntity<String> root() {
        return ResponseEntity.ok("CV Generator API is running");
    }
}