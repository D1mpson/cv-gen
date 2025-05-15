package com.example.cvgenerator.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

@Configuration
public class FileConfig implements WebMvcConfigurer {

    @Value("${app.upload.dir:/app/uploads}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Переконаємось, що директорія існує
        try {
            File photosDir = new File(uploadDir + "/photos");
            if (!photosDir.exists()) {
                boolean created = photosDir.mkdirs();
                System.out.println("Директорія для фото створена: " + created);
            }
        } catch (Exception e) {
            System.err.println("Помилка при створенні директорії для фото: " + e.getMessage());
        }

        registry.addResourceHandler("/uploads/photos/**")
                .addResourceLocations("file:" + uploadDir + "/photos/");

        registry.addResourceHandler("/images/**")
                .addResourceLocations("classpath:/images/");
    }
}