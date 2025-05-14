package com.example.cvgenerator.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
@Profile("prod")
public class FileStorageConfig {

    private static final Logger logger = LoggerFactory.getLogger(FileStorageConfig.class);

    @PostConstruct
    public void init() {
        try {
            // Створюємо директорії для зберігання файлів
            Path uploadsDir = Paths.get("uploads");
            Path photosDir = Paths.get("uploads/photos");

            if (!Files.exists(uploadsDir)) {
                Files.createDirectories(uploadsDir);
                logger.info("Created directory: {}", uploadsDir);
            }

            if (!Files.exists(photosDir)) {
                Files.createDirectories(photosDir);
                logger.info("Created directory: {}", photosDir);
            }

            // Встановлюємо права на запис
            File uploadsFile = uploadsDir.toFile();
            File photosFile = photosDir.toFile();

            uploadsFile.setWritable(true, false);
            photosFile.setWritable(true, false);

            logger.info("File storage configured successfully");
        } catch (Exception e) {
            logger.error("Failed to initialize file storage: {}", e.getMessage(), e);
        }
    }
}