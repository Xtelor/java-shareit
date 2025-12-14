package ru.practicum.shareit.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.service.BookingService;
import java.time.LocalDateTime;
import java.util.Collections;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static ru.practicum.shareit.HttpHeaders.USER_ID;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class BookingControllerTest {

    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper mapper;
    @MockBean private BookingService bookingService;

    // Тест создания бронирования
    @Test
    void shouldCreateBooking() throws Exception {
        BookingRequestDto request = BookingRequestDto.builder()
                .itemId(1L)
                .start(LocalDateTime.now().plusHours(24))
                .end(LocalDateTime.now().plusHours(48))
                .build();

        BookingResponseDto response = BookingResponseDto.builder()
                .id(1L)
                .status(BookingStatus.WAITING)
                .build();

        when(bookingService.create(anyLong(), any(BookingRequestDto.class))).thenReturn(response);

        mvc.perform(post("/bookings")
                        .header(USER_ID, 2L)
                        .content(mapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("WAITING"));
    }

    // Тест подтверждения бронирования владельцем
    @Test
    void shouldApproveBooking() throws Exception {
        BookingResponseDto response = BookingResponseDto.builder()
                .id(1L)
                .status(BookingStatus.APPROVED)
                .build();

        when(bookingService.approve(1L, 1L, true)).thenReturn(response);

        mvc.perform(patch("/bookings/1?approved=true")
                        .header(USER_ID, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    // Тест отклонения бронирования владельцем
    @Test
    void shouldRejectBooking() throws Exception {
        BookingResponseDto response = BookingResponseDto.builder()
                .id(1L)
                .status(BookingStatus.REJECTED)
                .build();

        when(bookingService.approve(1L, 1L, false)).thenReturn(response);

        mvc.perform(patch("/bookings/1?approved=false")
                        .header(USER_ID, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"));
    }

    // Тест получения бронирования по ID
    @Test
    void shouldGetBookingById() throws Exception {
        BookingResponseDto response = BookingResponseDto.builder()
                .id(1L)
                .status(BookingStatus.APPROVED)
                .build();

        when(bookingService.getById(2L, 1L)).thenReturn(response);

        mvc.perform(get("/bookings/1")
                        .header(USER_ID, 2L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    // Тест получения всех бронирований арендатора — состояние ALL
    @Test
    void shouldGetAllByBookerAll() throws Exception {
        when(bookingService.getAllByBooker(eq(1L), eq(BookingState.ALL)))
                .thenReturn(Collections.emptyList());

        mvc.perform(get("/bookings?state=ALL")
                        .header(USER_ID, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    // Тест получения всех бронирований арендатора — состояние CURRENT
    @Test
    void shouldGetAllByBookerCurrent() throws Exception {
        when(bookingService.getAllByBooker(eq(1L), eq(BookingState.CURRENT)))
                .thenReturn(Collections.emptyList());

        mvc.perform(get("/bookings?state=CURRENT")
                        .header(USER_ID, 1L))
                .andExpect(status().isOk());
    }

    // Тест получения всех бронирований арендатора — состояние PAST
    @Test
    void shouldGetAllByBookerPast() throws Exception {
        when(bookingService.getAllByBooker(eq(1L), eq(BookingState.PAST)))
                .thenReturn(Collections.emptyList());

        mvc.perform(get("/bookings?state=PAST")
                        .header(USER_ID, 1L))
                .andExpect(status().isOk());
    }

    // Тест получения всех бронирований владельца — состояние ALL
    @Test
    void shouldGetAllByOwnerAll() throws Exception {
        when(bookingService.getAllByOwner(eq(1L), eq(BookingState.ALL)))
                .thenReturn(Collections.emptyList());

        mvc.perform(get("/bookings/owner?state=ALL")
                        .header(USER_ID, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    // Тест получения всех бронирований владельца — состояние WAITING
    @Test
    void shouldGetAllByOwnerWaiting() throws Exception {
        when(bookingService.getAllByOwner(eq(1L), eq(BookingState.WAITING)))
                .thenReturn(Collections.emptyList());

        mvc.perform(get("/bookings/owner?state=WAITING")
                        .header(USER_ID, 1L))
                .andExpect(status().isOk());
    }

    // Тест получения всех бронирований владельца — состояние REJECTED
    @Test
    void shouldGetAllByOwnerRejected() throws Exception {
        when(bookingService.getAllByOwner(eq(1L), eq(BookingState.REJECTED)))
                .thenReturn(Collections.emptyList());

        mvc.perform(get("/bookings/owner?state=REJECTED")
                        .header(USER_ID, 1L))
                .andExpect(status().isOk());
    }
}