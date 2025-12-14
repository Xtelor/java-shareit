package ru.practicum.shareit.booking.dto;

import java.time.LocalDateTime;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookItemRequestDto {
    // ID вещи
    @NotNull(message = "ID вещи должен быть указан.")
    private long itemId;

    // Дата начала
    private LocalDateTime start;

    // Дата окончания
    private LocalDateTime end;
}
