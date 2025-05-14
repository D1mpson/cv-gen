package com.example.cvgenerator.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

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

    @GetMapping("/")
    public ModelAndView root() {
        // Замість простого повернення імені шаблону, повертаємо ModelAndView
        // Це вказує Spring, що це RestController, який все ще може повертати шаблон
        return new ModelAndView("index");
    }
}