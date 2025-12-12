package ru.practicum.shareit.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static ru.practicum.shareit.HttpHeaders.USER_ID;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class ItemRequestControllerTest {

    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper mapper;
    @MockBean private ItemRequestService service;

    // Тест создания запроса на вещь
    @Test
    void shouldCreateRequest() throws Exception {
        ItemRequestCreateDto createDto = ItemRequestCreateDto.builder()
                .description("Нужна дрель на выходные")
                .build();

        ItemRequestDto response = new ItemRequestDto();
        response.setId(1L);
        response.setDescription("Нужна дрель на выходные");
        response.setCreated(LocalDateTime.of(2025, 1, 1, 10, 0));
        response.setItems(List.of());

        when(service.create(eq(1L), any(ItemRequestCreateDto.class))).thenReturn(response);

        mvc.perform(post("/requests")
                        .header(USER_ID, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8")
                        .content(mapper.writeValueAsString(createDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.description").value("Нужна дрель на выходные"))
                .andExpect(jsonPath("$.created").exists());
    }

    // Тест получения своих запросов
    @Test
    void shouldGetOwnRequests() throws Exception {
        ItemRequestDto dto1 = new ItemRequestDto();
        dto1.setId(1L);
        dto1.setDescription("Нужна дрель");
        dto1.setItems(List.of());

        ItemRequestDto dto2 = new ItemRequestDto();
        dto2.setId(2L);
        dto2.setDescription("Нужен шуруповёрт");
        dto2.setItems(List.of());

        when(service.getOwn(1L)).thenReturn(List.of(dto1, dto2));

        mvc.perform(get("/requests")
                        .header(USER_ID, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].description").value("Нужна дрель"))
                .andExpect(jsonPath("$[1].description").value("Нужен шуруповёрт"));
    }

    // Тест получения всех запросов
    @Test
    void shouldGetAllRequests() throws Exception {
        ItemRequestDto dto = new ItemRequestDto();
        dto.setId(3L);
        dto.setDescription("Нужен перфоратор");
        dto.setItems(List.of());

        when(service.getAll(eq(1L), eq(0), eq(10))).thenReturn(List.of(dto));

        mvc.perform(get("/requests/all")
                        .header(USER_ID, 1L)
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].description").value("Нужен перфоратор"));
    }

    // Тест получения запроса по ID
    @Test
    void shouldGetRequestById() throws Exception {
        ItemRequestDto response = new ItemRequestDto();
        response.setId(1L);
        response.setDescription("Нужна дрель");
        response.setItems(List.of());

        when(service.getById(1L, 1L)).thenReturn(response);

        mvc.perform(get("/requests/1")
                        .header(USER_ID, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.description").value("Нужна дрель"));
    }

    // Тест: создание запроса без заголовка X-Sharer-User-Id
    @Test
    void shouldReturnBadRequest_whenUserIdHeaderMissing() throws Exception {
        ItemRequestCreateDto createDto = ItemRequestCreateDto.builder()
                .description("Нужна дрель")
                .build();

        mvc.perform(post("/requests")
                        .content(mapper.writeValueAsString(createDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    // Тест: получение запроса по несуществующему ID
    @Test
    void shouldReturnNotFound_whenRequestIdNotExists() throws Exception {
        when(service.getById(1L, 999L))
                .thenThrow(new NotFoundException("Запрос с id = 999 не найден"));

        mvc.perform(get("/requests/999")
                        .header(USER_ID, 1L))
                .andExpect(status().isNotFound());
    }
}