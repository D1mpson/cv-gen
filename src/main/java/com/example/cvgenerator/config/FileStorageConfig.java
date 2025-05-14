package com.example.cvgenerator.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration
public class FileStorageConfig {

    @Value("${uploads.directory:uploads/photos/}")
    private String uploadDirectory;

}