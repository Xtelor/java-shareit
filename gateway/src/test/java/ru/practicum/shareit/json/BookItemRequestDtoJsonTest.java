package ru.practicum.shareit.json;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import ru.practicum.shareit.booking.dto.BookItemRequestDto;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

// Тесты JSON-сериализации и десериализации для BookItemRequestDto
@JsonTest
class BookItemRequestDtoJsonTest {

    @Autowired
    private JacksonTester<BookItemRequestDto> json;

    // Тест корректной сериализации DTO в JSON
    @Test
    void shouldSerializeCorrectly() throws Exception {
        BookItemRequestDto dto = new BookItemRequestDto(
                1L,
                LocalDateTime.of(2025, 12, 31, 10, 0),
                LocalDateTime.of(2026, 1, 5, 12, 0)
        );

        String result = json.write(dto).getJson();

        assertThat(result)
                .contains("\"itemId\":1")
                .contains("\"start\":\"2025-12-31T10:00:00\"")
                .contains("\"end\":\"2026-01-05T12:00:00\"");
    }

    // Тест корректной десериализации JSON в DTO
    @Test
    void shouldDeserializeCorrectly() throws Exception {
        String content = """
                {
                    "itemId": 99,
                    "start": "2025-12-31T10:00:00",
                    "end": "2026-01-05T10:00:00"
                }
                """;

        BookItemRequestDto dto = json.parseObject(content);

        assertThat(dto.getItemId()).isEqualTo(99L);
        assertThat(dto.getStart()).isEqualTo(LocalDateTime.of(2025, 12, 31, 10, 0));
        assertThat(dto.getEnd()).isEqualTo(LocalDateTime.of(2026, 1, 5, 10, 0));
    }
}