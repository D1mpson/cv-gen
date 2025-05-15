package com.example.cvgenerator.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    private final JavaMailSender mailSender;

    @Autowired
    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public boolean sendVerificationEmail(String to, String code) {
        try {
            logger.info("Підготовка до відправлення листа на адресу: {}", to);

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("cv.gen.mailer@gmail.com");
            message.setTo(to);
            message.setSubject("Підтвердження реєстрації на CV Generator");
            message.setText("Вітаємо! Дякуємо за реєстрацію в CV Generator.\n\n" +
                    "Ваш код підтвердження: " + code +
                    "\n\nКод дійсний протягом 10 хвилин. Введіть його в форму підтвердження, щоб активувати ваш акаунт.\n\n" +
                    "Якщо ви не реєструвалися на нашому сервісі, проігноруйте цей лист.");

            mailSender.send(message);
            logger.info("Електронний лист успішно відправлено на адресу: {}", to);
            return true;
        } catch (MailException e) {
            logger.error("Помилка під час відправки електронного листа на {}: {}", to, e.getMessage(), e);
            return false;
        }
    }
}