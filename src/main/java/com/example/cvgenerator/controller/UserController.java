package com.example.cvgenerator.controller;

import com.example.cvgenerator.controller.util.UserHelper;
import com.example.cvgenerator.model.User;
import com.example.cvgenerator.service.CVService;
import com.example.cvgenerator.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;
    private final CVService cvService;
    private final UserHelper userHelper;

    @Autowired
    public UserController(UserService userService, CVService cvService, UserHelper userHelper) {
        this.userService = userService;
        this.cvService = cvService;
        this.userHelper = userHelper;
    }

    // Відображення форми реєстрації
    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    // Обробка форми реєстрації з перевіркою cityLife на сервері
    @PostMapping("/register")
    public String registerUserAccount(@Valid @ModelAttribute("user") User user,
                                      BindingResult result,
                                      RedirectAttributes redirectAttrs) {
        // Перевіряємо чи є помилки валідації
        if (result.hasErrors()) {
            return "register";
        }

        // ВАЖЛИВО: Явно встановлюємо значення для cityLife, якщо воно порожнє
        if (user.getCityLife() == null || user.getCityLife().trim().isEmpty()) {
            user.setCityLife("Не вказано");
        }

        try {
            userService.saveUser(user);
            redirectAttrs.addFlashAttribute("message", "Реєстрація пройшла успішно. Перевірте вашу електронну пошту для підтвердження акаунту.");
            redirectAttrs.addAttribute("email", user.getEmail());
            return "redirect:/verify";
        } catch (Exception e) {
            result.rejectValue("email", "error.user", e.getMessage());
            return "register";
        }
    }

    // Відображення профілю
    @GetMapping("/profile")
    public String showUserProfile(HttpServletRequest request, Model model) {
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            return "redirect:/login";
        }

        model.addAttribute("user", currentUser);
        model.addAttribute("httpServletRequest", request);
        model.addAttribute("cvList", cvService.getAllCVsByUser(currentUser));
        return "profile";
    }

    // Відображення форми редагування
    @GetMapping("/edit-profile")
    public String showEditProfileForm(Model model) {
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            return "redirect:/login";
        }

        User formUser = userHelper.prepareUserForm(currentUser);

        model.addAttribute("user", formUser);
        return "edit-profile";
    }

    // Обробка форми редагування
    @PostMapping("/edit-profile")
    public String updateProfile(@ModelAttribute("user") User formUser,
                                @RequestParam(value = "passwordConfirm", required = false) String passwordConfirm,
                                Model model) {
        try {
            User currentUser = userService.getCurrentUser();
            if (currentUser == null) {
                return "redirect:/login";
            }

            userHelper.updateUserData(currentUser, formUser);

            if (formUser.getPassword() != null && !formUser.getPassword().isEmpty()) {
                if (!formUser.getPassword().equals(passwordConfirm)) {
                    model.addAttribute("user", formUser);
                    model.addAttribute("passwordError", "Паролі не співпадають");
                    return "edit-profile";
                }

                userService.updatePassword(currentUser, formUser.getPassword());
            }

            userService.saveUser(currentUser);

        } catch (Exception e) {
            System.err.println("Помилка при оновленні профілю: " + e.getMessage());
            e.printStackTrace();
            return "redirect:/edit-profile";
        }

        return "redirect:/profile?success";
    }
}