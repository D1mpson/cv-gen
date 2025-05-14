package com.example.cvgenerator.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class FileConfig implements WebMvcConfigurer {

    private final FileStorageConfig fileStorageConfig;

    @Autowired
    public FileConfig(FileStorageConfig fileStorageConfig) {
        this.fileStorageConfig = fileStorageConfig;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String uploadDir = fileStorageConfig.getUploadDirectory();

        registry.addResourceHandler("/uploads/photos/**")
                .addResourceLocations("file:" + uploadDir);

        registry.addResourceHandler("/images/**")
                .addResourceLocations("file:src/main/resources/images/")
                .addResourceLocations("classpath:/images/");
    }
}