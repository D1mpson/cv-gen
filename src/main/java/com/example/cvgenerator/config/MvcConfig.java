package com.example.cvgenerator.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;

@Configuration
public class MvcConfig {

    @Bean
    @ConditionalOnMissingBean(HandlerMappingIntrospector.class)
    public HandlerMappingIntrospector mvcHandlerMappingIntrospector() {
        return new HandlerMappingIntrospector();
    }
}