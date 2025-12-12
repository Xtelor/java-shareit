package ru.practicum.shareit.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import ru.practicum.shareit.validation.OnCreate;
import ru.practicum.shareit.validation.OnUpdate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDto {
    // ID пользователя
    private Long id;

    // Имя пользователя
    @NotBlank(groups = OnCreate.class, message = "Имя не может быть пустым.")
    private String name;

    // Электронная почта пользователя
    @NotBlank(groups = OnCreate.class, message = "Email не может быть пустым.")
    @Email(groups = {OnCreate.class, OnUpdate.class}, message = "Некорректный формат email.")
    private String email;
}