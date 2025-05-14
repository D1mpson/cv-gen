package com.example.cvgenerator.service;

/**
 * Інтерфейс для сервісів відправки електронної пошти
 */
public interface EmailService {
    /**
     * Відправляє лист з кодом верифікації
     * @param to email-адреса отримувача
     * @param code код верифікації
     */
    void sendVerificationEmail(String to, String code);
}