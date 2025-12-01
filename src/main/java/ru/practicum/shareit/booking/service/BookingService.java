package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.model.BookingState;

import java.util.List;

public interface BookingService {
    // Добавление нового запроса на бронирование
    BookingResponseDto create(Long userId, BookingRequestDto bookingRequestDto);

    // Подтверждение/отклонение запроса на бронирование
    BookingResponseDto approve(Long userId, Long bookingId, Boolean approved);

    // Получение данных о конкретном бронировании
    BookingResponseDto getById(Long userId, Long bookingId);

    // Получение списка всех бронирований текущего пользователя
    List<BookingResponseDto> getAllByBooker(Long userId, BookingState state);

    // Получение списка бронирований для хозяина вещи
    List<BookingResponseDto> getAllByOwner(Long userId, BookingState state);
}
