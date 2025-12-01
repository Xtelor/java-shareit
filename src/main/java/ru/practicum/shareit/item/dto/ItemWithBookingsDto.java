package ru.practicum.shareit.item.dto;

import lombok.*;
import ru.practicum.shareit.booking.dto.BookingItemDto;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemWithBookingsDto {
    // ID вещи
    private Long id;

    // Название
    private String name;

    // Описание
    private String description;

    // Доступность вещи
    private Boolean available;

    // Предыдущее бронирование
    private BookingItemDto lastBooking;

    // Следующее бронирование
    private BookingItemDto nextBooking;

    // Комментарии
    @Builder.Default
    private List<CommentResponseDto> comments = new ArrayList<>();
}