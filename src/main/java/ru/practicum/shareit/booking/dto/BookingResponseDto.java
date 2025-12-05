package ru.practicum.shareit.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.booking.model.BookingStatus;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingResponseDto {
    // ID
    private Long id;

    // Дата начала бронирования
    private LocalDateTime start;

    // Дата окончания бронирования
    private LocalDateTime end;

    // Статус бронирования
    private BookingStatus status;

    // Забронировавший пользователь
    private UserDto booker;

    // Забронированная вещь
    private ItemDto item;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserDto {
        // ID пользователя
        private Long id;

        // Имя пользователя
        private String name;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ItemDto {
        // ID вещи
        private Long id;

        // Название вещи
        private String name;
    }
}
