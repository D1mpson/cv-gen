package com.example.cvgenerator.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class FileConfig implements WebMvcConfigurer {

    private static final Logger logger = LoggerFactory.getLogger(FileConfig.class);

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        logger.info("Налаштування ресурсів для додатку");

        // Налаштування для завантажених фото
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/")
                .setCachePeriod(3600);

        // Налаштування для uploads/photos
        registry.addResourceHandler("/uploads/photos/**")
                .addResourceLocations("file:uploads/photos/")
                .setCachePeriod(3600);

        // Налаштування для статичних ресурсів
        registry.addResourceHandler("/images/**")
                .addResourceLocations("file:src/main/resources/images/", "classpath:/images/")
                .setCachePeriod(3600);
    }
}