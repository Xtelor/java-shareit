package ru.practicum.shareit.mappers;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import static org.assertj.core.api.Assertions.assertThat;

class UserMapperTest {

    @Test
    void mapToUserDto_shouldReturnNull_whenUserIsNull() {
        assertThat(UserMapper.mapToUserDto(null)).isNull();
    }

    @Test
    void mapToUserDto_shouldMapFieldsCorrectly() {
        User user = User.builder()
                .id(1L)
                .name("Имя")
                .email("test@test.ru")
                .build();

        UserDto dto = UserMapper.mapToUserDto(user);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getName()).isEqualTo("Имя");
        assertThat(dto.getEmail()).isEqualTo("test@test.ru");
    }

    @Test
    void mapToUser_shouldReturnNull_whenDtoIsNull() {
        assertThat(UserMapper.mapToUser(null)).isNull();
    }

    @Test
    void mapToUser_shouldMapFieldsCorrectly() {
        UserDto dto = UserDto.builder()
                .id(2L)
                .name("Пользователь")
                .email("user@test.ru")
                .build();

        User user = UserMapper.mapToUser(dto);

        assertThat(user.getId()).isEqualTo(2L);
        assertThat(user.getName()).isEqualTo("Пользователь");
        assertThat(user.getEmail()).isEqualTo("user@test.ru");
    }

    @Test
    void updateUserFromDto_shouldUpdateOnlyNonBlankFields() {
        User user = User.builder()
                .id(1L)
                .name("Старое имя")
                .email("old@test.ru")
                .build();

        UserDto dto = UserDto.builder()
                .name("Новое имя")
                .email("new@test.ru")
                .build();

        UserMapper.updateUserFromDto(dto, user);

        assertThat(user.getName()).isEqualTo("Новое имя");
        assertThat(user.getEmail()).isEqualTo("new@test.ru");
    }

    @Test
    void updateUserFromDto_shouldNotUpdateWhenValuesNullOrBlank() {
        User user = User.builder()
                .id(1L)
                .name("Имя")
                .email("mail@test.ru")
                .build();

        UserDto dto = UserDto.builder()
                .name("   ")
                .email(null)
                .build();

        UserMapper.updateUserFromDto(dto, user);

        assertThat(user.getName()).isEqualTo("Имя");
        assertThat(user.getEmail()).isEqualTo("mail@test.ru");
    }
}
