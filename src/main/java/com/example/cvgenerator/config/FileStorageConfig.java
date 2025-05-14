package com.example.cvgenerator.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FileStorageConfig {

    @Value("${uploads.directory:uploads/photos/}")
    private String uploadDirectory;

    public String getUploadDirectory() {
        return uploadDirectory;
    }
}