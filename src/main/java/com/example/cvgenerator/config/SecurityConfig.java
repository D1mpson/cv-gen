package com.example.cvgenerator.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

    @Bean
    public static PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        try {
            logger.info("Налаштування SecurityFilterChain");

            http
                    .authorizeHttpRequests(authorize -> authorize
                            .requestMatchers("/", "/register", "/login", "/help", "/css/**", "/js/**", "/images/**", "/uploads/**", "/error").permitAll()
                            .requestMatchers("/verify", "/verify/**", "/resend-code").permitAll() // Шляхи для верифікації
                            .requestMatchers("/admin/**").hasRole("ADMIN")
                            .anyRequest().authenticated()
                    )
                    .formLogin(form -> form
                            .loginPage("/login")
                            .defaultSuccessUrl("/profile", true)
                            .permitAll()
                    )
                    .logout(logout -> logout
                            .logoutUrl("/logout")
                            .logoutSuccessUrl("/")
                            .invalidateHttpSession(true)
                            .clearAuthentication(true)
                            .permitAll()
                    )
                    .securityContext(securityContext -> securityContext
                            .requireExplicitSave(false) // Автоматично зберігати контекст
                    )
                    // Додаємо обробку виключень
                    .exceptionHandling(exceptions -> exceptions
                            .authenticationEntryPoint((request, response, authException) -> {
                                logger.warn("Неавторизований доступ: {}", request.getRequestURI());
                                if (request.getRequestURI().contains("/admin")) {
                                    response.sendRedirect("/login?admin_error=true");
                                } else {
                                    response.sendRedirect("/login?error=true");
                                }
                            })
                    );

            logger.info("SecurityFilterChain налаштовано успішно");
            return http.build();

        } catch (Exception e) {
            logger.error("Помилка налаштування SecurityFilterChain: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        try {
            return authConfig.getAuthenticationManager();
        } catch (Exception e) {
            logger.error("Помилка створення AuthenticationManager: {}", e.getMessage(), e);
            throw e;
        }
    }
}