package ru.practicum.shareit.user.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserDto {
    // ID пользователя
    private Long id;

    // Имя пользователя
    private String name;

    // Электронная почта пользователя
    private String email;
}
