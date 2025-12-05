package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentRequestDto {
    // Текст комментария
    @NotBlank(message = "Текст комментария не может быть пустым")
    @Size(max = 1000, message = "Комментарий не может быть длиннее 1000 символов")
    private String text;
}