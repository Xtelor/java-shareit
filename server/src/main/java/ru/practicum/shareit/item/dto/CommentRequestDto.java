package ru.practicum.shareit.item.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentRequestDto {
    // Текст комментария
    private String text;
}