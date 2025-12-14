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
import ru.practicum.shareit.booking.BookingClient;
import ru.practicum.shareit.booking.dto.BookItemRequestDto;
import ru.practicum.shareit.booking.dto.BookingState;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static ru.practicum.shareit.Headers.USER_ID;

@SpringBootTest
@AutoConfigureMockMvc
class BookingControllerTest {

    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper mapper;
    @MockBean private BookingClient bookingClient;

    // Успешное создание бронирования
    @Test
    void shouldCreateBooking_whenValidData() throws Exception {
        BookItemRequestDto requestDto = BookItemRequestDto.builder()
                .itemId(1L)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(3))
                .build();

        when(bookingClient.bookItem(eq(1L), any(BookItemRequestDto.class)))
                .thenReturn(ResponseEntity.ok().body("Booking created"));

        mvc.perform(post("/bookings")
                        .header(USER_ID, 1L)
                        .content(mapper.writeValueAsString(requestDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    // Ошибка: end раньше start — ручная валидация в контроллере
    @Test
    void shouldReturn400_whenEndBeforeStart() throws Exception {
        BookItemRequestDto invalidDto = BookItemRequestDto.builder()
                .itemId(1L)
                .start(LocalDateTime.now().plusDays(3))
                .end(LocalDateTime.now().plusDays(1))
                .build();

        mvc.perform(post("/bookings")
                        .header(USER_ID, 1L)
                        .content(mapper.writeValueAsString(invalidDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error")
                        .value("Время окончания бронирования должно быть после начала."));
    }

    // Ошибка: start равен end
    @Test
    void shouldReturn400_whenStartEqualsEnd() throws Exception {
        LocalDateTime time = LocalDateTime.now().plusDays(1);

        BookItemRequestDto invalidDto = BookItemRequestDto.builder()
                .itemId(1L)
                .start(time)
                .end(time)
                .build();

        mvc.perform(post("/bookings")
                        .header(USER_ID, 1L)
                        .content(mapper.writeValueAsString(invalidDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error")
                        .value("Время окончания бронирования должно быть после начала."));
    }

    // Ошибка: end в прошлом
    @Test
    void shouldReturn400_whenEndInPast() throws Exception {
        BookItemRequestDto invalidDto = BookItemRequestDto.builder()
                .itemId(1L)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().minusDays(1))
                .build();

        mvc.perform(post("/bookings")
                        .header(USER_ID, 1L)
                        .content(mapper.writeValueAsString(invalidDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error")
                        .value("Время окончания бронирования должно быть после начала."));
    }

    // Получение бронирования по ID
    @Test
    void shouldGetBookingById() throws Exception {
        when(bookingClient.getBooking(eq(1L), eq(5L)))
                .thenReturn(ResponseEntity.ok().body("Booking details"));

        mvc.perform(get("/bookings/5")
                        .header(USER_ID, 1L))
                .andExpect(status().isOk());
    }

    // Получение своих бронирований
    @Test
    void shouldGetOwnBookings() throws Exception {
        when(bookingClient.getBookings(eq(1L), eq(BookingState.ALL), eq(0), eq(10)))
                .thenReturn(ResponseEntity.ok().body(List.of()));

        mvc.perform(get("/bookings")
                        .header(USER_ID, 1L))
                .andExpect(status().isOk());
    }

    // Получение бронирований владельца
    @Test
    void shouldGetOwnerBookings() throws Exception {
        when(bookingClient.getBookingsForOwner(eq(1L), eq(BookingState.ALL), eq(0), eq(10)))
                .thenReturn(ResponseEntity.ok().body(List.of()));

        mvc.perform(get("/bookings/owner")
                        .header(USER_ID, 1L))
                .andExpect(status().isOk());
    }

    // Одобрение бронирования
    @Test
    void shouldApproveBooking() throws Exception {
        when(bookingClient.approve(eq(1L), eq(5L), eq(true)))
                .thenReturn(ResponseEntity.ok().body("Approved"));

        mvc.perform(patch("/bookings/5")
                        .header(USER_ID, 1L)
                        .param("approved", "true"))
                .andExpect(status().isOk());
    }

    // Отклонение бронирования
    @Test
    void shouldRejectBooking() throws Exception {
        when(bookingClient.approve(eq(1L), eq(5L), eq(false)))
                .thenReturn(ResponseEntity.ok().body("Rejected"));

        mvc.perform(patch("/bookings/5")
                        .header(USER_ID, 1L)
                        .param("approved", "false"))
                .andExpect(status().isOk());
    }

    // Ошибка: неверный state
    @Test
    void shouldReturn400_whenStateIsInvalid() throws Exception {
        mvc.perform(get("/bookings")
                        .header(USER_ID, 1L)
                        .param("state", "NEVERNYI"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Неизвестный статус: NEVERNYI"));
    }

    // Ошибка: пустой state
    @Test
    void shouldUseAllState_whenStateIsMissing() throws Exception {
        when(bookingClient.getBookings(eq(1L), eq(BookingState.ALL), eq(0), eq(10)))
                .thenReturn(ResponseEntity.ok().body(List.of()));

        mvc.perform(get("/bookings")
                        .header(USER_ID, 1L))
                .andExpect(status().isOk());
    }
}