package ru.practicum.shareit.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.ItemClient;
import ru.practicum.shareit.item.dto.CommentRequestDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static ru.practicum.shareit.Headers.USER_ID;

@SpringBootTest
@AutoConfigureMockMvc
class ItemControllerTest {

    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper mapper;
    @MockBean private ItemClient itemClient;

    // Успешное создание вещи
    @Test
    void shouldCreateItem_whenValidData() throws Exception {
        ItemDto dto = ItemDto.builder()
                .name("Дрель")
                .description("Аккумуляторная")
                .available(true)
                .build();

        when(itemClient.addItem(eq(1L), any(ItemDto.class)))
                .thenReturn(ResponseEntity.ok().body("Item created"));

        mvc.perform(post("/items")
                        .header(USER_ID, 1L)
                        .content(mapper.writeValueAsString(dto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    // Ошибка: name пустой при создании
    @Test
    void shouldReturn400_whenNameIsBlankOnCreate() throws Exception {
        ItemDto dto = ItemDto.builder()
                .name("   ")
                .description("Хорошая")
                .available(true)
                .build();

        mvc.perform(post("/items")
                        .header(USER_ID, 1L)
                        .content(mapper.writeValueAsString(dto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    // Ошибка: description пустой при создании
    @Test
    void shouldReturn400_whenDescriptionIsBlankOnCreate() throws Exception {
        ItemDto dto = ItemDto.builder()
                .name("Дрель")
                .description("")
                .available(true)
                .build();

        mvc.perform(post("/items")
                        .header(USER_ID, 1L)
                        .content(mapper.writeValueAsString(dto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    // Ошибка: available null при создании
    @Test
    void shouldReturn400_whenAvailableIsNullOnCreate() throws Exception {
        ItemDto dto = ItemDto.builder()
                .name("Дрель")
                .description("Хорошая")
                .build(); // available = null

        mvc.perform(post("/items")
                        .header(USER_ID, 1L)
                        .content(mapper.writeValueAsString(dto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    // Успешное обновление вещи (только name и description)
    @Test
    void shouldUpdateItem_whenValidData() throws Exception {
        ItemDto updateDto = ItemDto.builder()
                .name("Новая дрель")
                .description("Профессиональная")
                .build();

        when(itemClient.updateItem(eq(1L), eq(5L), any(ItemDto.class)))
                .thenReturn(ResponseEntity.ok().body("Updated"));

        mvc.perform(patch("/items/5")
                        .header(USER_ID, 1L)
                        .content(mapper.writeValueAsString(updateDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    // При обновлении available можно не передавать — не ошибка
    @Test
    void shouldAllowUpdateWithoutAvailable() throws Exception {
        ItemDto updateDto = ItemDto.builder()
                .name("Только имя")
                .build();

        when(itemClient.updateItem(eq(1L), eq(5L), any(ItemDto.class)))
                .thenReturn(ResponseEntity.ok().body("Updated"));

        mvc.perform(patch("/items/5")
                        .header(USER_ID, 1L)
                        .content(mapper.writeValueAsString(updateDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    // Получение вещи по ID
    @Test
    void shouldGetItemById() throws Exception {
        when(itemClient.getItemById(eq(1L), eq(5L)))
                .thenReturn(ResponseEntity.ok().body("Item details"));

        mvc.perform(get("/items/5")
                        .header(USER_ID, 1L))
                .andExpect(status().isOk());
    }

    // Получение всех вещей владельца
    @Test
    void shouldGetItemsByOwner() throws Exception {
        when(itemClient.getItemsByOwner(eq(1L), eq(0), eq(10)))
                .thenReturn(ResponseEntity.ok().body(List.of()));

        mvc.perform(get("/items")
                        .header(USER_ID, 1L))
                .andExpect(status().isOk());
    }

    // Поиск вещей — пустой текст
    @Test
    void shouldReturnEmptyList_whenSearchTextIsBlank() throws Exception {
        mvc.perform(get("/items/search")
                        .param("text", "   "))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    // Поиск вещей — нормальный запрос
    @Test
    void shouldSearchItems() throws Exception {
        when(itemClient.search(eq("дрель"), eq(0), eq(10)))
                .thenReturn(ResponseEntity.ok().body(List.of("Found")));

        mvc.perform(get("/items/search")
                        .param("text", "дрель"))
                .andExpect(status().isOk());
    }

    // Добавление комментария — успешно
    @Test
    void shouldAddComment_whenValidData() throws Exception {
        CommentRequestDto dto = CommentRequestDto.builder()
                .text("Отличная вещь!")
                .build();

        when(itemClient.addComment(eq(1L), eq(5L), any(CommentRequestDto.class)))
                .thenReturn(ResponseEntity.ok().body("Comment added"));

        mvc.perform(post("/items/5/comment")
                        .header(USER_ID, 1L)
                        .content(mapper.writeValueAsString(dto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    // Добавление комментария — пустой текст
    @Test
    void shouldReturn400_whenCommentTextIsBlank() throws Exception {
        CommentRequestDto dto = CommentRequestDto.builder()
                .text("   ")
                .build();

        mvc.perform(post("/items/5/comment")
                        .header(USER_ID, 1L)
                        .content(mapper.writeValueAsString(dto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}