package com.example.cvgenerator.controller;

import com.example.cvgenerator.model.User;
import com.example.cvgenerator.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
public class VerificationController {

    private final UserService userService;

    @Autowired
    public VerificationController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/verify")
    public String showVerificationForm(@RequestParam(required = false) String email, Model model) {
        model.addAttribute("email", email);
        return "verify";
    }

    @PostMapping("/verify")
    public String verifyEmail(@RequestParam String email,
                              @RequestParam String code,
                              HttpServletRequest request,
                              RedirectAttributes redirectAttributes) {

        System.out.println("Отримано запит на верифікацію для email: " + email);
        boolean verified = userService.verifyUser(email, code);

        if (verified) {
            System.out.println("Верифікація успішна, спроба автоматичного входу");
            try {
                // Отримуємо користувача
                Optional<User> userOptional = userService.findByEmail(email);

                if (userOptional.isPresent()) {
                    User user = userOptional.get();

                    // Створюємо authentication об'єкт без перевірки паролю
                    UserDetails userDetails = userService.loadVerifiedUserByUsername(email);
                    Authentication authentication = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());

                    // Отримуємо HttpSession
                    HttpSession session = request.getSession(true);

                    // Створюємо новий SecurityContext
                    SecurityContext securityContext = new SecurityContextImpl();
                    securityContext.setAuthentication(authentication);

                    // Зберігаємо SecurityContext в сесії
                    session.setAttribute(
                            HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                            securityContext);

                    // Оновлюємо також SecurityContextHolder
                    SecurityContextHolder.setContext(securityContext);

                    System.out.println("Автентифікація встановлена для користувача: " + email);
                    System.out.println("Статус автентифікації: " + SecurityContextHolder.getContext().getAuthentication().isAuthenticated());

                    redirectAttributes.addFlashAttribute("welcomeMessage",
                            "Вітаємо! Ваш email підтверджено, і ви увійшли в систему.");

                    return "redirect:/profile";
                }
            } catch (Exception e) {
                System.err.println("ПОМИЛКА автоматичного входу: " + e.getMessage());
                e.printStackTrace();
            }

            redirectAttributes.addFlashAttribute("success",
                    "Email успішно підтверджено! Тепер ви можете увійти.");
            return "redirect:/login";
        } else {
            redirectAttributes.addFlashAttribute("error",
                    "Невірний код підтвердження або термін його дії закінчився.");
            redirectAttributes.addAttribute("email", email);
            return "redirect:/verify";
        }
    }

    @PostMapping("/resend-code")
    public String resendVerificationCode(@RequestParam String email,
                                         RedirectAttributes redirectAttributes) {

        boolean sent = userService.resendVerificationCode(email);

        if (sent) {
            redirectAttributes.addFlashAttribute("info",
                    "Новий код підтвердження відправлено на вашу електронну пошту.");
        } else {
            redirectAttributes.addFlashAttribute("error",
                    "Не вдалося відправити код. Акаунт з таким email не знайдений або вже підтверджений.");
        }

        redirectAttributes.addAttribute("email", email);
        return "redirect:/verify";
    }
}