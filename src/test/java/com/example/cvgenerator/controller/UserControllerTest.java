package com.example.cvgenerator.controller;

import com.example.cvgenerator.controller.util.UserHelper;
import com.example.cvgenerator.model.User;
import com.example.cvgenerator.service.CVService;
import com.example.cvgenerator.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import(TestSecurityConfig.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private CVService cvService;

    @MockitoBean
    private UserHelper userHelper;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(5000L);
        testUser.setFirstName("Імʼя");
        testUser.setLastName("Прізвище");
        testUser.setEmail("account@email.domain");
        testUser.setPassword("password");
        testUser.setPhoneNumber("+380123456789");
        testUser.setCityLife("City");
        testUser.setBirthDate(LocalDate.of(1991, 1, 1));
        testUser.setRole("ROLE_USER");
    }

    // 1. Відповідь на запит
    @Test
    void givenRegisterRequest_whenGetRequest_thenReturns_200() throws Exception {
        mockMvc.perform(get("/register"))
                .andExpect(status().isOk());
    }

    // 2. Десеріалізація даних
    @Test
    void givenRegistrationForm_whenPostRequestWithValidData_thenDeserializesCorrectly() throws Exception {
        mockMvc.perform(post("/register")
                        .contentType("application/json")
                        .param("firstName", "Імʼя")
                        .param("lastName", "Прізвище")
                        .param("email", "account@email.domain")
                        .param("password", "password")
                        .param("phoneNumber", "+380123456789")
                        .param("birthDate", "01.01.1991")
                        .param("cityLife", "City"))
                .andExpect(status().is3xxRedirection());

        verify(userService).saveUser(argThat(user ->
                user.getFirstName().equals("Імʼя") &&
                        user.getLastName().equals("Прізвище") &&
                        user.getEmail().equals("account@email.domain") &&
                        user.getPhoneNumber().equals("+380123456789") &&
                        user.getCityLife().equals("City")
        ));
    }

    // 3. Валідація
    @Test
    void givenRegistrationForm_whenPostRequestWithInvalidData_thenShowsValidationErrors() throws Exception {
        mockMvc.perform(post("/register")
                        .contentType("application/json")
                        .param("firstName", "Імʼя")
                        .param("lastName", "Прізвище")
                        .param("email", "невірний email") //       <-----------
//                        .param("email", "email@email.domain") //       <-----------
                        .param("password", "password")
                        .param("phoneNumber", "+380123456789")
                        .param("birthDate", "01.01.1991")
                        .param("cityLife", "City"))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("user", "email"));
    }

    // 4. Бізнес логіка
    @Test
    @WithMockUser(username = "account@email.domain")
    void givenAuthenticatedUser_whenPostRequestToEditProfile_thenExecutesBusinessLogic() throws Exception {
        when(userService.getCurrentUser()).thenReturn(testUser);

        mockMvc.perform(post("/edit-profile")
                        .contentType("application/json")
                        .param("firstName", "Імʼя Оновлено")
                        .param("lastName", "Прізвище Оновлено")
                        .param("phoneNumber", "+380123456789")
                        .param("birthDate", "01.01.1991")
                        .param("cityLife", "City Оновлено")
                        .param("password", "newPassword")
                        .param("passwordConfirm", "newPassword"))
                .andExpect(status().is3xxRedirection());

        verify(userHelper).updateUserData(any(User.class), any(User.class));
        verify(userService).updatePassword(testUser, "newPassword");
        verify(userService).saveUser(testUser);
    }

    // 5. Серіалізація
    @Test
    @WithMockUser(username = "account@email.domain")
    void givenAuthenticatedUser_whenGetProfilePage_thenSerializesUserData() throws Exception {
        when(userService.getCurrentUser()).thenReturn(testUser);

        mockMvc.perform(get("/profile"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("user", testUser));
    }

    // 6. Обробка помилок і виключень
    @Test
    void givenExistingEmail_whenPostRequestToRegister_thenHandlesEmailAlreadyExistsException() throws Exception {
        doThrow(new RuntimeException("Користувач з таким email вже існує"))
                .when(userService).saveUser(any(User.class));

        mockMvc.perform(post("/register")
                        .contentType("application/json")
                        .param("firstName", "Імʼя")
                        .param("lastName", "Прізвище")
                        .param("email", "account@email.domain")
                        .param("password", "password")
                        .param("phoneNumber", "+380123456789")
                        .param("birthDate", "01.01.1991")
                        .param("cityLife", "City"))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("user", "email"));
    }
}