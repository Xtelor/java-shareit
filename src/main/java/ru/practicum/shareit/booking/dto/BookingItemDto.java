package ru.practicum.shareit.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
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
