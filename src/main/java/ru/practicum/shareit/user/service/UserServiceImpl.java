package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exceptions.ConflictException;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    // Создание пользователя
    @Override
    @Transactional
    public UserDto addUser(UserDto dto) {
        // Проверка уникальности электронной почты
        validateEmail(dto.getEmail());

        User user = UserMapper.mapToUser(dto);
        User createdUser = userRepository.save(user);

        return UserMapper.mapToUserDto(createdUser);
    }

    // Получение списка всех пользователей
    @Override
    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
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
    @Transactional
    public UserDto updateUser(Long id, UserDto dto) {
        User existingUser = findUserById(id);

        // Валидация электронной почты
        if (dto.getEmail() != null && !dto.getEmail().isBlank()) {
            if (!Objects.equals(existingUser.getEmail(), dto.getEmail())) {
                validateEmail(dto.getEmail());
            }
        }

        UserMapper.updateUserFromDto(dto, existingUser);
        existingUser = userRepository.save(existingUser);

        return UserMapper.mapToUserDto(existingUser);
    }

    // Удаление пользователя по ID
    @Override
    @Transactional
    public void deleteUser(Long id) {
        // Проверка существования пользователя
        findUserById(id);
        userRepository.deleteById(id);
    }

    // Удаление всех пользователей
    @Override
    @Transactional
    public void deleteAllUsers() {
        userRepository.deleteAllInBatch();
    }

    // Метод для проверки доступности электронной почты
    private void validateEmail(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new ConflictException("Эта электронная почта уже используется.");
        }
    }

    // Проверка пользователя на существование - возвращает пользователя, если он существует
    private User findUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + id + " не найден."));
    }
}
