package ru.practicum.shareit.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    // Сохранение пользователя — должен генерироваться ID
    @Test
    void shouldSaveUserAndAssignId() {
        User user = User.builder()
                .name("Алексей")
                .email("alex@newmail.ru")
                .build();

        User saved = userRepository.save(user);

        assertThat(saved.getId()).isPositive();
        assertThat(saved.getName()).isEqualTo("Алексей");
        assertThat(saved.getEmail()).isEqualTo("alex@newmail.ru");
    }

    // existsByEmail возвращает true, если пользователь с таким email существует
    @Test
    void existsByEmail_shouldReturnTrue_whenUserExists() {
        userRepository.save(User.builder()
                .name("Мария")
                .email("maria@gmail.com")
                .build());

        assertThat(userRepository.existsByEmail("maria@gmail.com")).isTrue();
        assertThat(userRepository.existsByEmail("MaRiA@gMaIl.CoM")).isFalse();
    }

    // existsByEmail возвращает false, если такого email нет
    @Test
    void existsByEmail_shouldReturnFalse_whenUserNotExists() {
        assertThat(userRepository.existsByEmail("unknown@domain.com")).isFalse();
        assertThat(userRepository.existsByEmail("")).isFalse();
        assertThat(userRepository.existsByEmail(null)).isFalse();
    }

    // Email чувствителен к регистру — нет приведения к lower case
    @Test
    void emailIsCaseSensitive_noLowerCaseNormalization() {
        userRepository.save(User.builder()
                .name("Иван")
                .email("IvAn@YaNdEx.Ru")
                .build());

        assertThat(userRepository.existsByEmail("IvAn@YaNdEx.Ru")).isTrue();
        assertThat(userRepository.existsByEmail("ivan@yandex.ru")).isFalse();
        assertThat(userRepository.existsByEmail("IVAN@YANDEX.RU")).isFalse();
    }

    // findAll возвращает всех сохранённых пользователей
    @Test
    void findAll_shouldReturnAllSavedUsers() {
        User user1 = userRepository.save(User.builder()
                .name("Василий")
                .email("vasya@yandex.ru")
                .build());

        User user2 = userRepository.save(User.builder()
                .name("Пётр")
                .email("petya@gmail.com")
                .build());

        User user3 = userRepository.save(User.builder()
                .name("Мария")
                .email("maria@mail.ru")
                .build());

        List<User> all = userRepository.findAll();

        assertThat(all).hasSize(3);
        assertThat(all).extracting(User::getName)
                .containsExactlyInAnyOrder("Василий", "Пётр", "Мария");
        assertThat(all).containsExactlyInAnyOrder(user1, user2, user3);
    }

    // findById возвращает пользователя, если он существует
    @Test
    void findById_shouldReturnUser_whenExists() {
        User saved = userRepository.save(User.builder()
                .name("Ольга")
                .email("olga@yandex.ru")
                .build());

        var found = userRepository.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get()).isEqualTo(saved);
    }

    // findById возвращает пустой Optional, если пользователя нет
    @Test
    void findById_shouldReturnEmpty_whenNotExists() {
        var found = userRepository.findById(999L);

        assertThat(found).isEmpty();
    }

    // deleteById удаляет пользователя
    @Test
    void deleteById_shouldRemoveUser() {
        User user = userRepository.save(User.builder()
                .name("Удаляй")
                .email("delete@me.ru")
                .build());

        userRepository.deleteById(user.getId());

        assertThat(userRepository.existsById(user.getId())).isFalse();
        assertThat(userRepository.findAll()).isEmpty();
    }
}