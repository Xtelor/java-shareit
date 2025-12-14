package ru.practicum.shareit.json;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

// Тесты JSON-сериализации и десериализации для ItemDto
@JsonTest
class ItemDtoJsonTest {

    @Autowired
    private JacksonTester<ItemDto> json;

    // Тест корректной сериализации
    @Test
    void shouldSerializeCorrectly() throws Exception {
        ItemDto dto = ItemDto.builder()
                .id(1L)
                .name("Дрель")
                .description("Аккумуляторная, мощная")
                .available(true)
                .requestId(5L)
                .lastBooking(null)
                .nextBooking(null)
                .comments(Collections.emptyList())
                .build();

        String result = json.write(dto).getJson();

        assertThat(result)
                .contains("\"id\":1")
                .contains("\"name\":\"Дрель\"")
                .contains("\"description\":\"Аккумуляторная, мощная\"")
                .contains("\"available\":true")
                .contains("\"requestId\":5")
                .contains("\"lastBooking\":null")
                .contains("\"nextBooking\":null")
                .contains("\"comments\":[]");
    }

    // Тест десериализации
    @Test
    void shouldDeserializeCorrectly() throws Exception {
        String content = "{" +
                "\"name\": \"Шуруповёрт\"," +
                "\"description\": \"Компактный\"," +
                "\"available\": false," +
                "\"requestId\": 10" +
                "}";

        ItemDto dto = json.parseObject(content);

        assertThat(dto.getName()).isEqualTo("Шуруповёрт");
        assertThat(dto.getDescription()).isEqualTo("Компактный");
        assertThat(dto.getAvailable()).isFalse();
        assertThat(dto.getRequestId()).isEqualTo(10L);
        assertThat(dto.getComments()).isNotNull();
    }
}