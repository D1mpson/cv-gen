package com.example.cvgenerator.service;

public interface EmailService {
    void sendVerificationEmail(String to, String code);
}