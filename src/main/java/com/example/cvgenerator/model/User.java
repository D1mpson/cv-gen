package com.example.cvgenerator.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Імʼя є обовʼязковим")
    @Column(name = "first_name")  // Додано явне відображення
    private String firstName;

    @NotBlank(message = "Прізвище є обовʼязковим")
    @Column(name = "last_name")  // Додано явне відображення
    private String lastName;

    @NotBlank(message = "Email є обовʼязковим")
    @Email(message = "Некоректний формат email")
    @Column(unique = true)
    private String email;

    @NotBlank(message = "Пароль є обовʼязковим")
    @Size(min = 6, message = "Пароль повинен містити не менше 6 символів")
    private String password;

    @NotBlank(message = "Номер телефону є обовʼязковим")
    @Pattern(regexp = "(\\+380[0-9]{9}|0[0-9]{9})", message = "Некоректний формат номеру телефону")
    @Column(name = "phone_number")  // Додано явне відображення
    private String phoneNumber;

    @NotNull(message = "Дата народження є обовʼязковою")
    @Past(message = "Дата народження повинна бути в минулому")
    @DateTimeFormat(pattern = "dd.MM.yyyy")
    @Column(name = "birth_date")  // Додано явне відображення
    private LocalDate birthDate;

    @NotNull(message = "Місто є обовʼязковим")
    @Column(name = "city_life")  // Додано явне відображення
    private String cityLife;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<CV> cvList = new ArrayList<>();

    private String role = "ROLE_USER";

    @Column(name = "verification_code", length = 6)
    private String verificationCode;

    @Column(name = "verified")
    private Boolean verified = false;

    @Column(name = "verification_code_expiry")
    private LocalDateTime verificationCodeExpiry;

    @SuppressWarnings("unused")
    public boolean isVerified() {
        return verified != null ? verified : false;
    }

    // Додаємо методи логування для відстеження процесу збереження
    @PrePersist
    public void prePersist() {
        System.out.println("Зберігаємо нового користувача з email: " + this.email);
    }

    @PostPersist
    public void postPersist() {
        System.out.println("Користувач збережений з ID: " + this.id);
    }
}