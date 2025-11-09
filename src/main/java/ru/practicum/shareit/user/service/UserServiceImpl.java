package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exceptions.ConflictException;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.storage.UserStorage;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserStorage userStorage;

    // Создание пользователя
    @Override
    public UserDto addUser(UserDto dto) {
        // Проверка уникальности электронной почты
        validateEmail(dto.getEmail());

        User user = UserMapper.mapToUser(dto);
        User createdUser = userStorage.addUser(user);
        return UserMapper.mapToUserDto(createdUser);
    }

    // Получение списка всех пользователей
    @Override
    public List<UserDto> getAllUsers() {
        return userStorage.getUsers().stream()
                .map(UserMapper::mapToUserDto)
                .collect(Collectors.toList());
    }

    // Получение пользователя по его ID
    @Override
    public UserDto getUser(Long id) {
        User user = findUserById(id);
        return UserMapper.mapToUserDto(user);
    }

    // Обновление пользователя
    @Override
    public UserDto updateUser(Long id, UserDto dto) {
        User existingUser = findUserById(id);

        // Обновляем имя, если оно передано
        if (dto.getName() != null && !dto.getName().isBlank()) {
            existingUser.setName(dto.getName());
        }

        // Обновляем email, если он передан/ещё не используется
        if (dto.getEmail() != null && !dto.getEmail().isBlank()) {
            String newEmail = dto.getEmail();
            if (!newEmail.equalsIgnoreCase(existingUser.getEmail())) {
                // Проверка уникальности электронной почты
                validateEmail(newEmail);
                existingUser.setEmail(newEmail);
            }
        }

        userStorage.updateUser(existingUser);
        return UserMapper.mapToUserDto(existingUser);
    }

    // Удаление пользователя по ID
    @Override
    public void deleteUser(Long id) {
        // Проверка существования пользователя
        findUserById(id);
        userStorage.deleteUser(id);
    }

    // Удаление всех пользователей
    @Override
    public void deleteAllUsers() {
        userStorage.deleteUsers();
    }

    // Метод для проверки доступности электронной почты
    private void validateEmail(String email) {
        if (userStorage.getUsers().stream()
                .anyMatch(usr -> Objects.equals(usr.getEmail(), email))
        ) {
            throw new ConflictException("Эта электронная почта уже используется.");
        }
    }

    // Проверка пользователя на существование - возвращает пользователя, если он существует
    private User findUserById(Long id) {
        User user = userStorage.getUser(id);

        if (user == null) {
            throw new NotFoundException("Пользователь с id = " + id + " не найден.");
        }

        return user;
    }
}
