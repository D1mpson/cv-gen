package com.example.cvgenerator.config;

import org.springframework.context.annotation.Configuration;

/**
 * Конфігурація MVC
 */
@Configuration
public class MvcConfig {
    // Видаляємо або коментуємо метод mvcHandlerMappingIntrospector,
    // щоб уникнути конфліктів з автоконфігурацією Spring
}