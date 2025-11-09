package ru.practicum.shareit.item.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import ru.practicum.shareit.user.model.User;

@Data
@Builder
@EqualsAndHashCode(of = "id")
public class Item {
    // ID вещи
    @NotNull(message = "ID вещи не может быть пустым.")
    @Positive(message = "ID вещи не может быть отрицательным.")
    @Builder.Default
    private Long id = 0L;
    // Название вещи
    @NotBlank(message = "Название вещи не может быть пустым.")
    private String name;
    // Описание вещи
    @NotBlank(message = "Описание вещи не может быть пустым.")
    private String description;
    // Статус вещи(доступна/недоступна)
    @NotBlank(message = "Статус вещи должен быть указан.")
    private Boolean available;
    // Владелец вещи
    @NotBlank(message = "У вещи должен быть владелец.")
    private User owner;
}
