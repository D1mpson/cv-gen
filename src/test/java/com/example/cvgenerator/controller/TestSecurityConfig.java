package com.example.cvgenerator.controller;

import com.example.cvgenerator.config.FileStorageConfig;
import com.example.cvgenerator.service.UserService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@TestConfiguration
public class TestSecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }

    @Bean
    @Primary
    public UserService userService() {
        return mock(UserService.class);
    }

    @Bean
    @Primary
    public FileStorageConfig fileStorageConfig() {
        FileStorageConfig mockConfig = mock(FileStorageConfig.class);
        when(mockConfig.getUploadDirectory()).thenReturn("uploads/photos/");
        return mockConfig;
    }
}