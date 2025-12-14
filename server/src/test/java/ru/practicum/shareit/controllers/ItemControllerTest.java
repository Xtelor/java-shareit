package ru.practicum.shareit.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exceptions.ForbiddenException;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.item.dto.CommentRequestDto;
import ru.practicum.shareit.item.dto.CommentResponseDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static ru.practicum.shareit.HttpHeaders.USER_ID;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class ItemControllerTest {

    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper mapper;
    @MockBean private ItemService itemService;

    // Тест создания новой вещи
    @Test
    void shouldCreateItem() throws Exception {
        ItemDto dto = ItemDto.builder().name("Дрель").description("Мощная").available(true).build();
        when(itemService.addItem(any(), eq(1L))).thenReturn(dto);

        mvc.perform(post("/items")
                        .header(USER_ID, 1L)
                        .content(mapper.writeValueAsString(dto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Дрель")));
    }

    // Тест получения вещи по ID
    @Test
    void shouldGetItemById() throws Exception {
        ItemDto item = ItemDto.builder().id(1L).name("Дрель").available(true).build();
        when(itemService.getItemByIdWithBookings(1L, 1L)).thenReturn(item);

        mvc.perform(get("/items/1")
                        .header(USER_ID, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)));
    }

    // Тест получения всех вещей владельца
    @Test
    void shouldGetItemsByOwner() throws Exception {
        when(itemService.getItemsByOwner(1L)).thenReturn(List.of(
                ItemDto.builder().id(1L).name("Дрель").build()
        ));

        mvc.perform(get("/items")
                        .header(USER_ID, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    // Тест обновления вещи
    @Test
    void shouldUpdateItem() throws Exception {
        ItemDto updated = ItemDto.builder().name("Супер дрель").build();
        when(itemService.updateItem(eq(1L), any(), eq(1L))).thenReturn(updated);

        mvc.perform(patch("/items/1")
                        .header(USER_ID, 1L)
                        .content(mapper.writeValueAsString(updated))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Супер дрель")));
    }

    // Тест обновления вещи — не владельцем (ForbiddenException)
    @Test
    void shouldReturn400WhenUpdateNotOwner() throws Exception {
        when(itemService.updateItem(eq(1L), any(), eq(2L)))
                .thenThrow(new ForbiddenException("Редактировать может только владелец"));

        mvc.perform(patch("/items/1")
                        .header(USER_ID, 2L)
                        .content("{}")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    // Тест поиска вещей
    @Test
    void shouldSearchItems() throws Exception {
        when(itemService.searchForItem("дрель")).thenReturn(List.of(
                ItemDto.builder().id(1L).name("Дрель Bosch").available(true).build()
        ));

        mvc.perform(get("/items/search?text=дрель"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name", is("Дрель Bosch")));
    }

    // Тест поиска с пустым текстом
    @Test
    void shouldReturnEmptyListWhenSearchTextBlank() throws Exception {
        when(itemService.searchForItem("")).thenReturn(List.of());

        mvc.perform(get("/items/search?text="))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", empty()));
    }

    // Тест добавления комментария
    @Test
    void shouldAddComment() throws Exception {
        CommentRequestDto request = new CommentRequestDto("Отличная вещь!");
        CommentResponseDto response = CommentResponseDto.builder()
                .id(1L).text("Отличная вещь!").authorName("Пётр").created(LocalDateTime.now()).build();

        when(itemService.addComment(2L, 1L, request)).thenReturn(response);

        mvc.perform(post("/items/1/comment")
                        .header(USER_ID, 2L)
                        .content(mapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text", is("Отличная вещь!")));
    }

    // Тест ошибки валидации при добавлении комментария
    @Test
    void shouldReturn400WhenCommentValidationFails() throws Exception {
        when(itemService.addComment(anyLong(), anyLong(), any()))
                .thenThrow(new ValidationException("Нельзя оставить комментарий"));

        mvc.perform(post("/items/1/comment")
                        .header(USER_ID, 2L)
                        .content("{\"text\": \"Коротко\"}")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    // Тест ошибки — вещь не найдена
    @Test
    void shouldReturn404WhenItemNotFound() throws Exception {
        when(itemService.getItemByIdWithBookings(anyLong(), anyLong()))
                .thenThrow(new NotFoundException("Вещь не найдена"));

        mvc.perform(get("/items/999")
                        .header(USER_ID, 1L))
                .andExpect(status().isNotFound());
    }

    // Тест удаления вещи по ID
    @Test
    void shouldDeleteItem() throws Exception {
        doNothing().when(itemService).deleteItem(1L);

        mvc.perform(delete("/items/1")
                        .header(USER_ID, 1L))
                .andExpect(status().isOk());

        verify(itemService, times(1)).deleteItem(1L);
    }

    // Тест удаления всех вещей
    @Test
    void shouldDeleteAllItems() throws Exception {
        doNothing().when(itemService).deleteAllItems();

        mvc.perform(delete("/items")
                        .header(USER_ID, 1L))
                .andExpect(status().isOk());

        verify(itemService, times(1)).deleteAllItems();
    }

    // Тест ошибки валидации при создании вещи (например, available = null)
    @Test
    void shouldReturn400WhenCreateItemValidationFails() throws Exception {
        when(itemService.addItem(any(), anyLong()))
                .thenThrow(new ValidationException("Доступность обязательна"));

        ItemDto invalid = ItemDto.builder().name("Дрель").description("Хорошая").build(); // available = null

        mvc.perform(post("/items")
                        .header(USER_ID, 1L)
                        .content(mapper.writeValueAsString(invalid))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}