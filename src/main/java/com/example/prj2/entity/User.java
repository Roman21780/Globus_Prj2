package com.example.prj2.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    @Email(message = "Email должен быть в правильном формате")
    @NotBlank(message = "Email не может быть пустым")
    private String email;

    @Column(nullable = false)
    @NotBlank(message = "Имя не может быть пустым")
    @Size(min = 2, max = 100, message = "Имя должно содержать от 2 до 100 символов")
    private String name;

    @Column(nullable = false)
    @NotNull(message = "Возраст не может быть null")
    @Min(value = 18, message = "Возраст должен быть не менее 18 лет")
    @Max(value = 150, message = "Возраст не должен превышать 150 лет")
    private Integer age;
}
