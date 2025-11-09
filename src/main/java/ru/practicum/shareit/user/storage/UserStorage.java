package ru.practicum.shareit.user.storage;

import ru.practicum.shareit.user.model.User;

import java.util.List;

public interface UserStorage {
    // Добавление пользователя
    User addUser(User user);

    // Получение пользователя по ID
    User getUser(Long id);

    // Получение списка всех пользователей
    List<User> getUsers();

    // Обновление пользователя
    void updateUser(User newUser);

    // Удаление пользователя по ID
    void deleteUser(Long id);

    // Удаление всех пользователей
    void deleteUsers();
}
