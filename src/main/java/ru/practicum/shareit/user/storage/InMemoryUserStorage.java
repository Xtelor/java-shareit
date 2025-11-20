package ru.practicum.shareit.user.storage;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.user.model.User;

import java.util.*;

@Component
public class InMemoryUserStorage implements UserStorage {
    private final Map<Long, User> users = new HashMap<>();

    // Добавление пользователя
    @Override
    public User addUser(User user) {
        user.setId(getNextId());
        users.put(user.getId(), user);
        return user;
    }

    // Получение пользователя по ID
    @Override
    public User getUser(Long id) {
        return users.get(id);
    }

    // Получение списка всех пользователей
    @Override
    public List<User> getUsers() {
        return new ArrayList<>(users.values());
    }

    // Обновление пользователя
    @Override
    public void updateUser(User user) {
        users.put(user.getId(), user);
    }

    // Удаление пользователя по ID
    @Override
    public void deleteUser(Long id) {
        users.remove(id);
    }

    // Удаление всех пользователей
    @Override
    public void deleteUsers() {
        users.clear();
    }

    // Получение уникального ID для следующего пользователя
    private Long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0L);
        return ++currentMaxId;
    }




}
