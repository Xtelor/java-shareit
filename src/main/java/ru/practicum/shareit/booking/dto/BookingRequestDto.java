package ru.practicum.shareit.booking.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;


import java.time.LocalDateTime;

/**
 * TODO Sprint add-bookings.
 */
@Data
@Builder
public class BookingRequestDto {
    // ID вещи
    @NotNull(message = "ID вещи не может быть пустым.")
    private Long itemId;

    // Дата начала брони
    @NotNull(message = "Дата бронирования не может быть пустой.")
    @FutureOrPresent(message = "Дата начала не может быть в прошлом.")
    private LocalDateTime start;

    // Дата окончания брони
    @NotNull(message = "Дата окончания бронирования не может быть пустой.")
    @Future(message = "Дата окончания должна быть в будущем")
    private LocalDateTime end;
}
