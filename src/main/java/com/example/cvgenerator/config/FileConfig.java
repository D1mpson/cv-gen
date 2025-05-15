package com.example.cvgenerator.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class FileConfig implements WebMvcConfigurer {

    @Value("${app.upload.dir:/app/uploads}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        registry.addResourceHandler("/uploads/photos/**")
                .addResourceLocations("file:" + uploadDir + "/photos/");

        registry.addResourceHandler("/images/**")
                .addResourceLocations("file:src/main/resources/images/")
                .addResourceLocations("classpath:/images/");
    }
}