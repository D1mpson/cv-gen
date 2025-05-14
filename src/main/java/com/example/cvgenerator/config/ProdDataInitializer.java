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
import java.util.List;

@Component
@Profile("prod")
public class ProdDataInitializer {

    private final TemplateService templateService;
    private final UserService userService;
    private final Logger logger = LoggerFactory.getLogger(ProdDataInitializer.class);

    @Autowired
    public ProdDataInitializer(TemplateService templateService, UserService userService) {
        this.templateService = templateService;
        this.userService = userService;
        logger.info("ProdDataInitializer: Конструктор викликано, залежності впроваджено");
    }

    @PostConstruct
    public void initializeData() {
        try {
            logger.info("ProdDataInitializer: Перевірка існуючих шаблонів");
            List<Template> existingTemplates = templateService.getAllTemplates();

            if (existingTemplates.isEmpty()) {
                logger.info("ProdDataInitializer: Створення стандартних шаблонів");
                createDefaultTemplates();
            } else {
                logger.info("ProdDataInitializer: Шаблони вже існують, пропускаємо ініціалізацію");
            }

            logger.info("ProdDataInitializer: Перевірка існування адміністратора");
            List<User> admins = userService.findByRole("ROLE_ADMIN");

            if (admins.isEmpty()) {
                logger.info("ProdDataInitializer: Створення адміністратора");
                createAdmin();
            } else {
                logger.info("ProdDataInitializer: Адміністратор вже існує, пропускаємо ініціалізацію");
            }

            logger.info("ProdDataInitializer: Ініціалізація даних завершена");
        } catch (Exception e) {
            logger.error("ProdDataInitializer: Помилка під час ініціалізації даних", e);
        }
    }

    private void createDefaultTemplates() {
        // Код створення шаблонів (скопіюйте з вашого DataInitializer)
        // Наприклад:
        Template template1 = new Template();
        template1.setName("Класичний");
        template1.setDescription("Класичний дизайн CV для будь-якої професії");
        template1.setHtmlPath("cv-1");
        template1.setPreviewImagePath("template1.jpg");
        templateService.saveTemplate(template1);

        Template template2 = new Template();
        template2.setName("Сучасний");
        template2.setDescription("Сучасний дизайн CV з акцентом на професійні навички");
        template2.setHtmlPath("cv-2");
        template2.setPreviewImagePath("template2.jpg");
        templateService.saveTemplate(template2);

        Template template3 = new Template();
        template3.setName("Креативний");
        template3.setDescription("Креативний дизайн CV для творчих професій");
        template3.setHtmlPath("cv-3");
        template3.setPreviewImagePath("template3.jpg");
        templateService.saveTemplate(template3);
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