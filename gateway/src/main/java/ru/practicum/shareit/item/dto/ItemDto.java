package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import ru.practicum.shareit.validation.OnCreate;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemDto {
    // ID вещи
    private Long id;

    // Название вещи
    @NotBlank(groups = OnCreate.class, message = "Название вещи не может быть пустым.")
    private String name;

    // Описание
    @NotBlank(groups = OnCreate.class, message = "Описание вещи не может быть пустым.")
    private String description;

    // Статус
    @NotNull(groups = OnCreate.class, message = "Статус доступности вещи должен быть указан.")
    private Boolean available;

    // ID запроса
    private Long requestId;

    // Предыдущее бронирование
    private Object lastBooking;

    // Следующее бронирование
    private Object nextBooking;

    // Комментарии
    @Builder.Default
    private List<Object> comments = new ArrayList<>();
}