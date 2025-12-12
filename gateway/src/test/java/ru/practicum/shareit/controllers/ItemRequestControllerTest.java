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
import ru.practicum.shareit.request.ItemRequestClient;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static ru.practicum.shareit.Headers.USER_ID;

@SpringBootTest
@AutoConfigureMockMvc
class ItemRequestControllerTest {

    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper mapper;
    @MockBean private ItemRequestClient requestClient;

    // Успешное создание запроса
    @Test
    void shouldCreateRequest_whenValidData() throws Exception {
        ItemRequestCreateDto dto = ItemRequestCreateDto.builder()
                .description("Нужна дрель на выходные")
                .build();

        when(requestClient.create(eq(1L), any(ItemRequestCreateDto.class)))
                .thenReturn(ResponseEntity.ok().body("Request created"));

        mvc.perform(post("/requests")
                        .header(USER_ID, 1L)
                        .content(mapper.writeValueAsString(dto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    // Ошибка: описание пустое
    @Test
    void shouldReturn400_whenDescriptionIsBlank() throws Exception {
        ItemRequestCreateDto dto = ItemRequestCreateDto.builder()
                .description("   ")
                .build();

        mvc.perform(post("/requests")
                        .header(USER_ID, 1L)
                        .content(mapper.writeValueAsString(dto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    // Ошибка: описание null
    @Test
    void shouldReturn400_whenDescriptionIsNull() throws Exception {
        ItemRequestCreateDto dto = ItemRequestCreateDto.builder()
                .build(); // description = null

        mvc.perform(post("/requests")
                        .header(USER_ID, 1L)
                        .content(mapper.writeValueAsString(dto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    // Получение своих запросов
    @Test
    void shouldGetOwnRequests() throws Exception {
        when(requestClient.getOwn(eq(1L)))
                .thenReturn(ResponseEntity.ok().body(List.of("My requests")));

        mvc.perform(get("/requests")
                        .header(USER_ID, 1L))
                .andExpect(status().isOk());
    }

    // Получение всех запросов (чужих) с пагинацией
    @Test
    void shouldGetAllRequests() throws Exception {
        when(requestClient.getAll(eq(1L), eq(0), eq(10)))
                .thenReturn(ResponseEntity.ok().body(List.of("All requests")));

        mvc.perform(get("/requests/all")
                        .header(USER_ID, 1L)
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());
    }

    // Получение всех запросов без параметров — дефолтные значения
    @Test
    void shouldGetAllRequests_withoutParams() throws Exception {
        when(requestClient.getAll(eq(1L), eq(0), eq(10)))
                .thenReturn(ResponseEntity.ok().body(List.of()));

        mvc.perform(get("/requests/all")
                        .header(USER_ID, 1L))
                .andExpect(status().isOk());
    }

    // Получение запроса по ID
    @Test
    void shouldGetRequestById() throws Exception {
        when(requestClient.getById(eq(1L), eq(5L)))
                .thenReturn(ResponseEntity.ok().body("Request details"));

        mvc.perform(get("/requests/5")
                        .header(USER_ID, 1L))
                .andExpect(status().isOk());
    }
}