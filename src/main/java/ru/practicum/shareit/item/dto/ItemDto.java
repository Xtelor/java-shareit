package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ItemDto {
    // ID вещи
    private Long id;

    // Название вещи
    @NotBlank(message = "Название вещи не может быть пустым.")
    private String name;

    // Описание вещи
    @NotBlank(message = "Описание вещи не может быть пустым.")
    private String description;

    // Статус вещи(доступна/недоступна)
    @NotNull(message = "Статус вещи должен быть указан.")
    private Boolean available;
}
