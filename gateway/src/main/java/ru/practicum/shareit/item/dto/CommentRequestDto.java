package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentRequestDto {
    // Текст комментария
    @NotBlank(message = "Текст комментария не может быть пустым.")
    private String text;
}