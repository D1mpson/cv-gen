package com.example.cvgenerator.service;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

/**
 * Конфігурація Mail для production середовища
 */
@Configuration
@Profile("prod")
public class MailConfig {

    /**
     * Створюємо фіктивний JavaMailSender для production,
     * щоб MockEmailService міг працювати без відправки реальних листів
     */
    @Bean
    public JavaMailSender javaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost("localhost");
        mailSender.setPort(25);

        // Налаштування для відключення реальної відправки листів
        java.util.Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "false");
        props.put("mail.smtp.starttls.enable", "false");
        props.put("mail.debug", "false");

        return mailSender;
    }
}