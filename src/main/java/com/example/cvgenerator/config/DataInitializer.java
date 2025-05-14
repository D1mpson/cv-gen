package com.example.cvgenerator.config;

import com.example.cvgenerator.model.Template;
import com.example.cvgenerator.model.User;
import com.example.cvgenerator.service.TemplateService;
import com.example.cvgenerator.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.time.LocalDate;

@Component
public class DataInitializer {

    private final TemplateService templateService;
    private final UserService userService;

    @Autowired
    public DataInitializer(TemplateService templateService, UserService userService) {
        this.templateService = templateService;
        this.userService = userService;
        System.out.println("DataInitializer: Конструктор викликано, залежності впроваджено");
    }

    @PostConstruct
    public void initializeData() {
        System.out.println("DataInitializer: Метод @PostConstruct викликано");

        // Створюю 3 стандартні шаблони, якщо шаблони відсутні в БД
        if (templateService.getAllTemplates().isEmpty()) {
            createDefaultTemplates();
        }

        // Якщо користувачів немає, створюю Адміністратора
        if (userService.getAllUsers().isEmpty()) {
            createAdmin();
        }

        System.out.println("DataInitializer: Ініціалізація даних завершена");
    }

    private void createDefaultTemplates() {
        // ... код створення шаблонів ...
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
            admin.setVerified(true); // Додайте це - встановіть verified в true для адміністратора
            userService.saveUser(admin);
            System.out.println("Створено адміністратора (email: admin@system.com, пароль: admin123)");
        }
    }
}