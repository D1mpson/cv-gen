package com.example.cvgenerator.service;

import com.example.cvgenerator.model.User;
import com.example.cvgenerator.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JdbcTemplate jdbcTemplate;
    private final EmailService emailService;

    // Додаємо прапорець для пропуску перевірки верифікації
    private static final ThreadLocal<Boolean> skipVerificationCheck = new ThreadLocal<>();

    public static void setSkipVerificationCheck(boolean skip) {
        skipVerificationCheck.set(skip);
    }

    public static void clearSkipVerificationCheck() {
        skipVerificationCheck.remove();
    }

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                       JdbcTemplate jdbcTemplate, EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jdbcTemplate = jdbcTemplate;
        this.emailService = emailService;
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<User> findByEmailWithJdbc(String email) {
        String sql = "SELECT * FROM users WHERE email = ?";

        try {
            User user = jdbcTemplate.queryForObject(sql, new Object[]{email}, (rs, rowNum) -> {
                User u = new User();
                u.setId(rs.getLong("id"));
                u.setFirstName(rs.getString("first_name"));
                u.setLastName(rs.getString("last_name"));
                u.setEmail(rs.getString("email"));
                u.setPassword(rs.getString("password"));
                u.setPhoneNumber(rs.getString("phone_number"));
                u.setBirthDate(rs.getObject("birth_date", LocalDate.class));
                u.setCityLife(rs.getString("city_life"));
                u.setRole(rs.getString("role"));
                u.setVerified(rs.getBoolean("verified"));
                u.setVerificationCode(rs.getString("verification_code"));

                // Отримуємо дату закінчення терміну дії коду
                java.sql.Timestamp timestamp = rs.getTimestamp("verification_code_expiry");
                if (timestamp != null) {
                    u.setVerificationCodeExpiry(timestamp.toLocalDateTime());
                }

                return u;
            });
            return Optional.ofNullable(user);
        } catch (Exception e) {
            System.out.println("Користувача з email " + email + " не знайдено: " + e.getMessage());
            return Optional.empty();
        }
    }

    public User saveUser(User user) {
        // Якщо це новий користувач (без ID), перевіряємо чи існує такий email
        if (user.getId() == null) {
            if (userRepository.existsByEmail(user.getEmail())) {
                throw new RuntimeException("Користувач з таким email вже існує");
            }

            // Для нового користувача генеруємо код перевірки, якщо це не адміністратор
            if (!"ROLE_ADMIN".equals(user.getRole())) {
                String verificationCode = generateVerificationCode();
                user.setVerificationCode(verificationCode);
                user.setVerified(false);
                user.setVerificationCodeExpiry(LocalDateTime.now().plusMinutes(10));

                // Шифрування пароля
                user.setPassword(passwordEncoder.encode(user.getPassword()));

                // Зберігаємо користувача
                User savedUser = userRepository.save(user);

                // Відправляємо код електронною поштою
                emailService.sendVerificationEmail(user.getEmail(), verificationCode);

                return savedUser;
            } else {
                // Для адміністратора відразу встановлюємо verified = true
                user.setVerified(true);
            }
        }

        // Шифрування пароля, якщо він ще не зашифрований і не пустий
        if (user.getPassword() != null && !user.getPassword().isEmpty()
                && !user.getPassword().startsWith("$2a$")) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }

        return userRepository.save(user);
    }

    // Метод для генерації 6-значного коду
    private String generateVerificationCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000); // Генерує код від 100000 до 999999
        return String.valueOf(code);
    }

    // Метод для перевірки коду верифікації
    public boolean verifyUser(String email, String code) {
        Optional<User> userOptional = userRepository.findByEmail(email);

        if (userOptional.isPresent()) {
            User user = userOptional.get();

            // Перевіряємо чи користувач вже верифікований
            if (user.getVerified()) {
                return true; // Користувач вже верифікований
            }

            // Перевіряємо код та чи не закінчився термін його дії
            if (code.equals(user.getVerificationCode()) &&
                    LocalDateTime.now().isBefore(user.getVerificationCodeExpiry())) {

                user.setVerified(true);
                user.setVerificationCode(null);
                user.setVerificationCodeExpiry(null);
                userRepository.save(user);
                return true;
            }
        }

        return false;
    }

    // Метод для повторної відправки коду
    public boolean resendVerificationCode(String email) {
        Optional<User> userOptional = userRepository.findByEmail(email);

        if (userOptional.isPresent()) {
            User user = userOptional.get();

            // Генерувати новий код, лише якщо користувач ще не верифікований
            if (!user.getVerified()) {
                String newCode = generateVerificationCode();
                user.setVerificationCode(newCode);
                user.setVerificationCodeExpiry(LocalDateTime.now().plusMinutes(10));
                userRepository.save(user);

                emailService.sendVerificationEmail(email, newCode);
                return true;
            }
        }

        return false;
    }

    public void updatePassword(User user, String newPassword) {
        if (newPassword != null && !newPassword.isEmpty()) {
            user.setPassword(passwordEncoder.encode(newPassword));
        }
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    // Метод для отримання поточного авторизованого користувача
    public User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth instanceof AnonymousAuthenticationToken) {
            return null;
        }

        Object principal = auth.getPrincipal();
        String email;

        if (principal instanceof UserDetails) {
            email = ((UserDetails) principal).getUsername();
        } else {
            email = principal.toString();
        }

        return userRepository.findByEmail(email).orElse(null);
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Користувача з email " + email + " не знайдено"));

        // Перевіряємо прапорець пропуску перевірки
        Boolean skip = skipVerificationCheck.get();

        // Перевіряємо верифікацію тільки якщо не встановлено прапорець пропуску
        if (skip == null || !skip) {
            if (!user.getVerified()) {
                throw new DisabledException("Електронна пошта не підтверджена. Будь ласка, підтвердіть ваш email.");
            }
        }

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPassword())
                .roles(user.getRole().replace("ROLE_", ""))
                .build();
    }

    // Метод для завантаження користувача без перевірки верифікації
    public UserDetails loadVerifiedUserByUsername(String email) {
        try {
            // Встановлюємо прапорець пропуску перевірки верифікації
            setSkipVerificationCheck(true);

            // Викликаємо основний метод з пропуском перевірки
            UserDetails userDetails = loadUserByUsername(email);

            return userDetails;
        } finally {
            // Обов'язково скидаємо прапорець після використання
            clearSkipVerificationCheck();
        }
    }

    // Отримання всіх користувачів (для адміністратора)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // Видалення користувача (для адміністратора)
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    public List<User> findByRole(String role) {
        return userRepository.findAll().stream()
                .filter(user -> role.equals(user.getRole()))
                .collect(Collectors.toList());
    }

    // Метод для створення UserDetails без перевірки верифікації
    public UserDetails createUserDetails(User user) {
        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPassword())
                .roles(user.getRole().replace("ROLE_", ""))
                .build();
    }
    public boolean adminExists() {
        return !findByRole("ROLE_ADMIN").isEmpty();
    }
}