package ru.practicum.shareit.request.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ItemRequestDto {
    // ID запроса
    private Long id;

    // Описание запроса
    private String description;

    // Дата создания запроса
    private LocalDateTime created;

    // Список запрашиваемых вещей
    private List<ItemForRequestDto> items;
}
