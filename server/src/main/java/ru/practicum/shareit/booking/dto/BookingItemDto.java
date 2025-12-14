package ru.practicum.shareit.booking.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class BookingItemDto {
    // ID
    private Long id;

    // ID бронирующего пользователя
    private Long bookerId;

    // Дата начала
    private LocalDateTime start;

    // Дата окончания
    private LocalDateTime end;
}
