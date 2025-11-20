package ru.practicum.shareit.user.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

@Data
@Builder
@EqualsAndHashCode(of = "id")
@NoArgsConstructor
@AllArgsConstructor
public class User {
    // ID пользователя
    @NotNull(message = "ID не может быть пустым.")
    @Positive(message = "ID не может быть отрицательным.")
    @Builder.Default
    private Long id = 0L;
    // Имя пользователя
    @NotBlank(message = "Имя пользователя не может быть пустым.")
    private String name;
    // Электронная почта пользователя
    @NotBlank(message = "Электронная почта не может быть пустой.")
    @Email(message = "Некорректная электронная почта.")
    private String email;
}
