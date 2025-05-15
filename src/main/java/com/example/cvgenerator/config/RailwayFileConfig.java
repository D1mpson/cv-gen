package com.example.cvgenerator.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@Configuration
@Profile("prod")
public class RailwayFileConfig {

    private static final Logger logger = LoggerFactory.getLogger(RailwayFileConfig.class);

    @Value("${app.upload.dir:/app/uploads}")
    private String uploadDir;

    @PostConstruct
    public void init() {
        try {
            logger.info("Ініціалізація директорій для завантаження в Railway: {}", uploadDir);

            // Створюємо директорії з обробкою помилок
            try {
                Files.createDirectories(Paths.get(uploadDir));
                logger.info("Створено директорію: {}", uploadDir);
            } catch (Exception e) {
                logger.warn("Не вдалося створити директорію {}: {}", uploadDir, e.getMessage());
                // Не перекидаємо помилку, щоб не переривати запуск додатку
            }

            try {
                Files.createDirectories(Paths.get(uploadDir + "/photos"));
                logger.info("Створено директорію для фото: {}/photos", uploadDir);
            } catch (Exception e) {
                logger.warn("Не вдалося створити директорію {}/photos: {}", uploadDir, e.getMessage());
                // Не перекидаємо помилку, щоб не переривати запуск додатку
            }
        } catch (Exception e) {
            logger.error("Помилка при ініціалізації директорій: {}", e.getMessage());
            // Не перекидаємо помилку, щоб не переривати запуск додатку
        }
    }
}