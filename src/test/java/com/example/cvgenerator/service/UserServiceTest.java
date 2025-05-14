package com.example.cvgenerator.service;

import com.example.cvgenerator.model.User;
import com.example.cvgenerator.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private EmailService emailService; // Додаємо мок для EmailService

    private UserService userService;

    @BeforeEach
    void setUp() {
        // Передаємо мок EmailService в конструктор
        userService = new UserService(userRepository, passwordEncoder, jdbcTemplate, emailService);
    }

    @Test
    void whenSaveUser_thenSuccess() {
        // Підготовка тестових даних
        User testUser = new User();
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setEmail("test@example.com");
        testUser.setPassword("password123");
        testUser.setPhoneNumber("+380123456789");
        testUser.setBirthDate(LocalDate.of(1990, 1, 1));
        testUser.setCityLife("Test City");

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Не потрібно перевіряти виклик emailService.sendVerificationEmail,
        // тому що це відбувається всередині методу saveUser

        // Виконання тесту
        User savedUser = userService.saveUser(testUser);

        // Перевірка результатів
        assertNotNull(savedUser);
        assertEquals("Test", savedUser.getFirstName());
        assertEquals("User", savedUser.getLastName());

        // Перевіряємо, що методи були викликані
        verify(userRepository).existsByEmail("test@example.com");
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(User.class));
        verify(emailService).sendVerificationEmail(eq("test@example.com"), anyString()); // Перевіряємо виклик відправки email
    }

    @Test
    void whenFindByEmail_thenSuccess() {
        // Підготовка тестових даних
        User testUser = new User();
        testUser.setEmail("test@example.com");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        // Виконання тесту
        Optional<User> foundUser = userRepository.findByEmail("test@example.com");

        // Перевірка результатів
        assertTrue(foundUser.isPresent());
        assertEquals("test@example.com", foundUser.get().getEmail());

        // Перевіряємо, що метод був викликаний
        verify(userRepository).findByEmail("test@example.com");
    }

    // Додаємо тести для нових методів
    @Test
    void whenVerifyUserWithValidCode_thenSuccess() {
        // Підготовка тестових даних
        User testUser = new User();
        testUser.setEmail("test@example.com");
        testUser.setVerificationCode("123456");
        testUser.setVerified(false);
        testUser.setVerificationCodeExpiry(LocalDate.now().plusDays(1).atStartOfDay());

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Виконання тесту
        boolean verified = userService.verifyUser("test@example.com", "123456");

        // Перевірка результатів
        assertTrue(verified);
        assertTrue(testUser.isVerified());
        assertNull(testUser.getVerificationCode());

        // Перевіряємо, що методи були викликані
        verify(userRepository).findByEmail("test@example.com");
        verify(userRepository).save(testUser);
    }

    @Test
    void whenResendVerificationCode_thenSuccess() {
        // Підготовка тестових даних
        User testUser = new User();
        testUser.setEmail("test@example.com");
        testUser.setVerified(false);

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        doNothing().when(emailService).sendVerificationEmail(anyString(), anyString());

        // Виконання тесту
        boolean sent = userService.resendVerificationCode("test@example.com");

        // Перевірка результатів
        assertTrue(sent);
        assertNotNull(testUser.getVerificationCode());
        assertNotNull(testUser.getVerificationCodeExpiry());

        // Перевіряємо, що методи були викликані
        verify(userRepository).findByEmail("test@example.com");
        verify(userRepository).save(testUser);
        verify(emailService).sendVerificationEmail(eq("test@example.com"), anyString());
    }
}