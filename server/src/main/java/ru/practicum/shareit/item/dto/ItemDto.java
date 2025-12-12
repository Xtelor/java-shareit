package ru.practicum.shareit.item.dto;

import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class ItemDto {
    // ID вещи
    private Long id;

    // Название вещи
    private String name;

    // Описание вещи
    private String description;

    // Статус вещи(доступна/недоступна)
    private Boolean available;

    // ID запрашивающего вещь
    private Long requestId;

    private Object lastBooking;

    private Object nextBooking;

    @Builder.Default
    private List<CommentResponseDto> comments = new ArrayList<>();
}
