package com.example.cvgenerator.config;

import com.example.cvgenerator.model.Template;
import com.example.cvgenerator.model.User;
import com.example.cvgenerator.service.TemplateService;
import com.example.cvgenerator.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.time.LocalDate;

@Component
@Profile("prod")
public class ProdDataInitializer {

    private static final Logger logger = LoggerFactory.getLogger(ProdDataInitializer.class);

    private final TemplateService templateService;
    private final UserService userService;

    @Autowired
    public ProdDataInitializer(TemplateService templateService, UserService userService) {
        this.templateService = templateService;
        this.userService = userService;
    }

    @PostConstruct
    public void initializeData() {
        logger.info("ProdDataInitializer: Ініціалізація даних для production середовища");

        // Перевірка чи є шаблони
        logger.info("ProdDataInitializer: Перевірка існуючих шаблонів");
        if (templateService.getAllTemplates().isEmpty()) {
            logger.info("ProdDataInitializer: Створення стандартних шаблонів");
            createDefaultTemplates();
        } else {
            logger.info("ProdDataInitializer: Шаблони вже існують, пропускаємо ініціалізацію");
        }

        // Перевірка чи є адміністратор
        logger.info("ProdDataInitializer: Перевірка наявності адміністратора");
        if (userService.findByRole("ROLE_ADMIN").isEmpty()) {
            logger.info("ProdDataInitializer: Створення адміністратора");
            createAdmin();
        } else {
            logger.info("ProdDataInitializer: Адміністратор вже існує, пропускаємо ініціалізацію");
        }
    }

    private void createDefaultTemplates() {
        // Шаблон 1
        Template template1 = new Template();
        template1.setName("Класичний");
        template1.setDescription("Класичний дизайн CV з чіткою структурою");
        template1.setHtmlPath("cv-1");
        templateService.saveTemplate(template1);

        // Шаблон 2
        Template template2 = new Template();
        template2.setName("Сучасний");
        template2.setDescription("Сучасний мінімалістичний дизайн");
        template2.setHtmlPath("cv-2");
        templateService.saveTemplate(template2);

        // Шаблон 3
        Template template3 = new Template();
        template3.setName("Креативний");
        template3.setDescription("Креативний дизайн для творчих професій");
        template3.setHtmlPath("cv-3");
        templateService.saveTemplate(template3);

        logger.info("ProdDataInitializer: Створено 3 стандартні шаблони");
    }

    private void createAdmin() {
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
        logger.info("ProdDataInitializer: Створено адміністратора (email: admin@system.com)");
    }
}