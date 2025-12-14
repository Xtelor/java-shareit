package ru.practicum.shareit.item.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentResponseDto {
    // ID комментария
    private Long id;

    // Текст комментария
    private String text;

    // Автор комментария
    private String authorName;

    // Дата создания комментария
    private LocalDateTime created;
}