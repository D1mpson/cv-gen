package com.example.cvgenerator.controller;

import com.example.cvgenerator.controller.util.UserHelper;
import com.example.cvgenerator.model.User;
import com.example.cvgenerator.service.CVService;
import com.example.cvgenerator.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

    @Test
    public void givenRegisterRequest_whenGetRequest_thenReturns_200() throws Exception {
        mockMvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeExists("user"));
    }

    @Test
    public void givenRegistrationForm_whenPostRequestWithValidData_thenDeserializesCorrectly() throws Exception {
        User testUser = createTestUser();

        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("firstName", testUser.getFirstName())
                        .param("lastName", testUser.getLastName())
                        .param("email", testUser.getEmail())
                        .param("password", testUser.getPassword())
                        .param("phoneNumber", testUser.getPhoneNumber())
                        .param("birthDate", testUser.getBirthDate().toString())
                        .param("cityLife", testUser.getCityLife()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/verify?email=test@example.com"));

        verify(userService, times(1)).saveUser(any(User.class));
    }

    @Test
    public void givenRegistrationForm_whenPostRequestWithInvalidData_thenShowsValidationErrors() throws Exception {
        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("firstName", "")
                        .param("lastName", "")
                        .param("email", "invalid-email")
                        .param("password", "123")
                        .param("phoneNumber", "invalid-phone")
                        .param("birthDate", LocalDate.now().plusDays(1).toString())
                        .param("cityLife", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().hasErrors());

        verify(userService, never()).saveUser(any(User.class));
    }

    @Test
    public void givenExistingEmail_whenPostRequestToRegister_thenHandlesEmailAlreadyExistsException() throws Exception {
        User testUser = createTestUser();

        doThrow(new RuntimeException("Користувач з таким email вже існує"))
                .when(userService).saveUser(any(User.class));

        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("firstName", testUser.getFirstName())
                        .param("lastName", testUser.getLastName())
                        .param("email", testUser.getEmail())
                        .param("password", testUser.getPassword())
                        .param("phoneNumber", testUser.getPhoneNumber())
                        .param("birthDate", testUser.getBirthDate().toString())
                        .param("cityLife", testUser.getCityLife()))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeHasFieldErrors("user", "email"));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    public void givenAuthenticatedUser_whenGetProfilePage_thenSerializesUserData() throws Exception {
        User testUser = createTestUser();
        when(userService.getCurrentUser()).thenReturn(testUser);
        when(cvService.getAllCVsByUser(any(User.class))).thenReturn(new ArrayList<>());

        mockMvc.perform(get("/profile"))
                .andExpect(status().isOk())
                .andExpect(view().name("profile"))
                .andExpect(model().attributeExists("user", "cvList"));

        verify(userService, times(1)).getCurrentUser();
        verify(cvService, times(1)).getAllCVsByUser(any(User.class));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    public void givenAuthenticatedUser_whenPostRequestToEditProfile_thenExecutesBusinessLogic() throws Exception {
        User testUser = createTestUser();
        when(userService.getCurrentUser()).thenReturn(testUser);

        mockMvc.perform(post("/edit-profile")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("firstName", "UpdatedName")
                        .param("lastName", "UpdatedLastName")
                        .param("password", "newpassword")
                        .param("passwordConfirm", "newpassword")
                        .param("phoneNumber", "+380987654321")
                        .param("birthDate", testUser.getBirthDate().toString())
                        .param("cityLife", "UpdatedCity"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile?success"));

        verify(userService, times(1)).updatePassword(eq(testUser), eq("newpassword"));
        verify(userService, times(1)).saveUser(any(User.class));
    }

    private User createTestUser() {
        User user = new User();
        user.setId(1L);
        user.setFirstName("Test");
        user.setLastName("User");
        user.setEmail("test@example.com");
        user.setPassword("password123");
        user.setPhoneNumber("+380123456789");
        user.setBirthDate(LocalDate.now().minusYears(20));
        user.setCityLife("Test City");
        user.setVerified(true);
        return user;
    }
}