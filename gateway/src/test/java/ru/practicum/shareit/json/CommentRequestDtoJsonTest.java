package ru.practicum.shareit.json;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import ru.practicum.shareit.item.dto.CommentRequestDto;

import static org.assertj.core.api.Assertions.assertThat;

// Тесты JSON-сериализации и десериализации для CommentRequestDto
@JsonTest
class CommentRequestDtoJsonTest {

    @Autowired
    private JacksonTester<CommentRequestDto> json;

    // Тест корректной сериализации DTO в JSON
    @Test
    void shouldSerializeCorrectly() throws Exception {
        CommentRequestDto dto = CommentRequestDto.builder()
                .text("Отличная дрель! Рекомендую!")
                .build();

        String result = json.write(dto).getJson();

        assertThat(result)
                .contains("\"text\":\"Отличная дрель! Рекомендую!\"");
    }

    // Тест корректной десериализации JSON в DTO
    @Test
    void shouldDeserializeCorrectly() throws Exception {
        String content = "{" +
                "\"text\": \"Всё работает, спасибо!\"" +
                "}";

        CommentRequestDto dto = json.parseObject(content);

        assertThat(dto.getText()).isEqualTo("Всё работает, спасибо!");
    }

    // Тест на пустой текст — валидация сработает при @Valid в контроллере
    @Test
    void shouldAllowEmptyTextInJson() throws Exception {
        String content = "{" +
                "\"text\": \"\"" +
                "}";

        CommentRequestDto dto = json.parseObject(content);
        assertThat(dto.getText()).isEmpty();
    }
}