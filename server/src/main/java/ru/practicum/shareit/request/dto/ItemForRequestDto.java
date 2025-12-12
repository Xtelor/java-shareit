package ru.practicum.shareit.request.dto;

import lombok.Data;

@Data
public class ItemForRequestDto {
    // ID запрашиваемой вещи
    private Long id;

    // Название вещи
    private String name;

    // ID владельца вещи
    private Long ownerId;
}