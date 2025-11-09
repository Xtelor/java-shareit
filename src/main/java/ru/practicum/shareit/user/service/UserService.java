package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

public interface UserService {
    // Добавление пользователя
    UserDto addUser(UserDto dto);

    // Получение списка всех пользователей
    List<UserDto> getAllUsers();

    // Получение пользователя по ID
    UserDto getUser(Long id);

    // Обновление пользователя
    UserDto updateUser(Long id, UserDto dto);

    // Удаление пользователя по ID
    void deleteUser(Long id);

    // Удаление всех пользователей
    void deleteAllUsers();
}
