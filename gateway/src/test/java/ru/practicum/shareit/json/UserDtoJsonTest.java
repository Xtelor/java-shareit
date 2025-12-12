package ru.practicum.shareit.json;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import ru.practicum.shareit.user.dto.UserDto;

import static org.assertj.core.api.Assertions.assertThat;

// Тесты JSON-сериализации и десериализации для UserDto
@JsonTest
class UserDtoJsonTest {

    @Autowired
    private JacksonTester<UserDto> json;

    // Тест корректной сериализации DTO в JSON
    @Test
    void shouldSerializeCorrectly() throws Exception {
        UserDto dto = UserDto.builder()
                .id(1L)
                .name("Алексей")
                .email("alex@yandex.ru")
                .build();

        String result = json.write(dto).getJson();

        assertThat(result)
                .contains("\"id\":1")
                .contains("\"name\":\"Алексей\"")
                .contains("\"email\":\"alex@yandex.ru\"");
    }

    // Тест корректной десериализации JSON в DTO
    @Test
    void shouldDeserializeCorrectly() throws Exception {
        String content = """
                {
                    "name": "Мария",
                    "email": "maria@gmail.com"
                }
                """;

        UserDto dto = json.parseObject(content);

        assertThat(dto.getName()).isEqualTo("Мария");
        assertThat(dto.getEmail()).isEqualTo("maria@gmail.com");
        assertThat(dto.getId()).isNull();
    }

    // Тест на некорректный email — валидация @Email сработает при @Valid
    @Test
    void shouldAllowInvalidEmailInJson() throws Exception {
        String content = """
                {
                    "name": "Пётр",
                    "email": "неправильный_емейл"
                }
                """;

        UserDto dto = json.parseObject(content);
        assertThat(dto.getEmail()).isEqualTo("неправильный_емейл");
    }
}