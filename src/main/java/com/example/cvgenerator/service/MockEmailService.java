package com.example.cvgenerator.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Primary
@Profile("prod")
public class MockEmailService implements EmailService {

    private static final Logger logger = LoggerFactory.getLogger(MockEmailService.class);

    @Override
    public void sendVerificationEmail(String to, String code) {
        logger.info("MOCK: Відправка верифікаційного листа на {} з кодом {}", to, code);
        // Виведемо код в логи для тестування
        logger.info("Verification code for {}: {}", to, code);
    }
}