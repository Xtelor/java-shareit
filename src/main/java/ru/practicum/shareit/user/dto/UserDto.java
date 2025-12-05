package ru.practicum.shareit.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserDto {
    // ID пользователя
    private Long id;

    // Имя пользователя
    @NotBlank(message = "Имя пользователя не может быть пустым.")
    private String name;

    // Электронная почта пользователя
    @NotBlank(message = "Электронная почта не может быть пустой.")
    @Email(message = "Некорректная электронная почта.")
    private String email;
}
