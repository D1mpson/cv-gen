package com.example.cvgenerator.config;

import com.example.cvgenerator.model.Template;
import com.example.cvgenerator.model.User;
import com.example.cvgenerator.service.TemplateService;
import com.example.cvgenerator.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.time.LocalDate;

@Component
public class DataInitializer {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    private final TemplateService templateService;
    private final UserService userService;

    @Autowired
    public DataInitializer(TemplateService templateService, UserService userService) {
        this.templateService = templateService;
        this.userService = userService;
        logger.info("DataInitializer: Конструктор викликано, залежності впроваджено");
    }

    @PostConstruct
    public void initializeData() {
        try {
            logger.info("DataInitializer: Метод @PostConstruct викликано");

            // Створюю 3 стандартні шаблони, якщо шаблони відсутні в БД
            if (templateService.getAllTemplates() == null || templateService.getAllTemplates().isEmpty()) {
                createDefaultTemplates();
            }

            // Якщо користувачів немає, створюю Адміністратора
            if (userService.getAllUsers() == null || userService.getAllUsers().isEmpty()) {
                createAdmin();
            }

            logger.info("DataInitializer: Ініціалізація даних завершена");
        } catch (Exception e) {
            logger.error("Помилка при ініціалізації даних: {}", e.getMessage(), e);
        }
    }

    private void createDefaultTemplates() {
        // ... код створення шаблонів ...
        logger.info("Створено стандартні шаблони");
    }

    private void createAdmin() {
        if (userService.findByRole("ROLE_ADMIN").isEmpty()) {
            User admin = new User();
            admin.setFirstName("Admin");
            admin.setLastName("System");
            admin.setEmail("admin@system.com");
            admin.setPassword("admin123");
            admin.setPhoneNumber("+380000000000");
            admin.setBirthDate(LocalDate.now().minusYears(18));
            admin.setCityLife("City");
            admin.setRole("ROLE_ADMIN");
            admin.setVerified(true);
            userService.saveUser(admin);
            logger.info("Створено адміністратора (email: admin@system.com)");
        }
    }
}