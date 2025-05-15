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

            Files.createDirectories(Paths.get(uploadDir));
            Files.createDirectories(Paths.get(uploadDir + "/photos"));

            logger.info("Створено директорії для завантаження в: {}", uploadDir);
        } catch (IOException e) {
            logger.error("Не вдалося створити директорії для завантаження: {}", e.getMessage(), e);
            throw new RuntimeException("Не вдалося створити директорії для завантаження", e);
        }
    }
}