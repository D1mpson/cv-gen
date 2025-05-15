package com.example.cvgenerator.config;

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

    @Value("${app.upload.dir:${user.dir}/uploads}")
    private String uploadDir;

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(Paths.get(uploadDir));
            Files.createDirectories(Paths.get(uploadDir + "/photos"));
            System.out.println("Створено директорії для завантаження в: " + uploadDir);
        } catch (IOException e) {
            throw new RuntimeException("Не вдалося створити директорії для завантаження", e);
        }
    }
}