package com.example.cvgenerator.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Autowired
    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendVerificationEmail(String to, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("noreply@cvgenerator.com");
        message.setTo(to);
        message.setSubject("Підтвердження реєстрації на CV Generator");
        message.setText("Вітаємо! Дякуємо за реєстрацію в CV Generator.\n\n" +
                "Ваш код підтвердження: " + code +
                "\n\nКод дійсний протягом 10 хвилин. Введіть його в форму підтвердження, щоб активувати ваш акаунт.\n\n" +
                "Якщо ви не реєструвалися на нашому сервісі, проігноруйте цей лист.");

        mailSender.send(message);
    }
}