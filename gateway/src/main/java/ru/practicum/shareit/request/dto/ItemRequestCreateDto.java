package ru.practicum.shareit.request.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemRequestCreateDto {
    // Описание запроса
    @NotBlank(message = "Описание запроса не может быть пустым.")
    private String description;
}