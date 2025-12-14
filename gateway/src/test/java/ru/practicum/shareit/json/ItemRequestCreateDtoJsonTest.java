package ru.practicum.shareit.json;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;

import static org.assertj.core.api.Assertions.assertThat;

// Тесты JSON-сериализации и десериализации для ItemRequestCreateDto
@JsonTest
class ItemRequestCreateDtoJsonTest {

    @Autowired
    private JacksonTester<ItemRequestCreateDto> json;

    // Тест корректной сериализации DTO в JSON
    @Test
    void shouldSerializeCorrectly() throws Exception {
        ItemRequestCreateDto dto = ItemRequestCreateDto.builder()
                .description("Нужна дрель на выходные")
                .build();

        String result = json.write(dto).getJson();

        assertThat(result)
                .contains("\"description\":\"Нужна дрель на выходные\"");
    }

    // Тест корректной десериализации JSON в DTO
    @Test
    void shouldDeserializeCorrectly() throws Exception {
        String content = "{" +
                "\"description\": \"Ищу шуруповёрт с двумя аккумуляторами\"" +
                "}";

        ItemRequestCreateDto dto = json.parseObject(content);

        assertThat(dto.getDescription())
                .isEqualTo("Ищу шуруповёрт с двумя аккумуляторами");
    }

    // Тест на пустое описание — валидация @NotBlank сработает при @Valid в контроллере
    @Test
    void shouldAllowEmptyDescriptionInJson() throws Exception {
        String content = "{" +
                "\"description\": \"\"" +
                "}";

        ItemRequestCreateDto dto = json.parseObject(content);
        assertThat(dto.getDescription()).isEmpty();
    }
}