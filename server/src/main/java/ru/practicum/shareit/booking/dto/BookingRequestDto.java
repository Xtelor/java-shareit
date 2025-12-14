package ru.practicum.shareit.booking.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class BookingRequestDto {
    // ID вещи
    private Long itemId;

    // Дата начала брони
    private LocalDateTime start;

    // Дата окончания брони
    private LocalDateTime end;
}
