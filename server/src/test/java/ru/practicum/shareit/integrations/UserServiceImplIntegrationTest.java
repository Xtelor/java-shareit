package ru.practicum.shareit.integrations;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exceptions.ConflictException;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.jpa.properties.hibernate.id.new_generator_mappings=false"
})
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class UserServiceImplIntegrationTest {

    private final UserService userService;

    // Создание пользователя — должен сохраниться и вернуться с ID
    @Test
    void shouldCreateUser_successfully() {
        UserDto dto = UserDto.builder()
                .name("Алексей")
                .email("alex@test.ru")
                .build();

        UserDto saved = userService.addUser(dto);

        assertThat(saved.getId()).isPositive();
        assertThat(saved.getName()).isEqualTo("Алексей");
        assertThat(saved.getEmail()).isEqualTo("alex@test.ru");
    }

    // Создание пользователя с уже существующим email — должен бросить ConflictException
    @Test
    void shouldThrowConflictException_whenEmailAlreadyExists() {
        UserDto first = UserDto.builder()
                .name("Первый")
                .email("duplicate@test.ru")
                .build();
        userService.addUser(first);

        UserDto second = UserDto.builder()
                .name("Второй")
                .email("duplicate@test.ru")
                .build();

        assertThatThrownBy(() -> userService.addUser(second))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Эта электронная почта уже используется.");
    }

    // Получение всех пользователей — должен вернуть всех сохранённых
    @Test
    void getAllUsers_shouldReturnAllSavedUsers() {
        userService.addUser(UserDto.builder()
                .name("Вася")
                .email("vasya@test.ru")
                .build());

        userService.addUser(UserDto.builder()
                .name("Петя")
                .email("petya@test.ru")
                .build());

        userService.addUser(UserDto.builder()
                .name("Маша")
                .email("masha@test.ru")
                .build());

        List<UserDto> all = userService.getAllUsers();

        assertThat(all).hasSize(3);
        assertThat(all).extracting(UserDto::getName)
                .containsExactlyInAnyOrder("Вася", "Петя", "Маша");
    }

    // Получение пользователя по ID — должен вернуть существующего
    @Test
    void getUser_shouldReturnUser_whenExists() {
        UserDto saved = userService.addUser(UserDto.builder()
                .name("Ольга")
                .email("olga@test.ru")
                .build());

        UserDto found = userService.getUser(saved.getId());

        assertThat(found).isEqualTo(saved);
    }

    // Получение несуществующего пользователя — должен бросить NotFoundException
    @Test
    void getUser_shouldThrowNotFoundException_whenUserNotExists() {
        assertThatThrownBy(() -> userService.getUser(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Пользователь с id = 999 не найден.");
    }

    // Обновление пользователя — должны обновиться только переданные поля
    @Test
    void updateUser_shouldUpdateOnlyProvidedFields() {
        UserDto original = userService.addUser(UserDto.builder()
                .name("Старое имя")
                .email("old@test.ru")
                .build());

        UserDto updateDto = UserDto.builder()
                .name("Новое имя")
                .build();

        UserDto updated = userService.updateUser(original.getId(), updateDto);

        assertThat(updated.getId()).isEqualTo(original.getId());
        assertThat(updated.getName()).isEqualTo("Новое имя");
        assertThat(updated.getEmail()).isEqualTo("old@test.ru");
    }

    // Обновление email на уже занятый — должен бросить ConflictException
    @Test
    void updateUser_shouldThrowConflictException_whenNewEmailIsTaken() {
        userService.addUser(UserDto.builder()
                .name("Первый")
                .email("taken@test.ru")
                .build());

        UserDto userToUpdate = userService.addUser(UserDto.builder()
                .name("Второй")
                .email("second@test.ru")
                .build());

        UserDto badUpdate = UserDto.builder()
                .email("taken@test.ru")
                .build();

        assertThatThrownBy(() -> userService.updateUser(userToUpdate.getId(), badUpdate))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Эта электронная почта уже используется.");
    }

    // Удаление пользователя — он должен исчезнуть
    @Test
    void deleteUser_shouldRemoveUser() {
        UserDto saved = userService.addUser(UserDto.builder()
                .name("Удаляемый")
                .email("delete@test.ru")
                .build());

        userService.deleteUser(saved.getId());

        assertThatThrownBy(() -> userService.getUser(saved.getId()))
                .isInstanceOf(NotFoundException.class);
    }

    // Удаление всех пользователей — база должна стать пустой
    @Test
    void deleteAllUsers_shouldClearDatabase() {
        userService.addUser(UserDto.builder()
                .name("Один")
                .email("1@test.ru")
                .build());

        userService.addUser(UserDto.builder()
                .name("Два")
                .email("2@test.ru")
                .build());

        userService.deleteAllUsers();

        assertThat(userService.getAllUsers()).isEmpty();
    }
}