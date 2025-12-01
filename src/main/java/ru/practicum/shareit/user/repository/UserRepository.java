package ru.practicum.shareit.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.user.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
    // Метод для проверки доступности электронной почты пользователя
    boolean existsByEmail(String email);
}
