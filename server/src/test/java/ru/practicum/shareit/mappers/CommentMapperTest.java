package ru.practicum.shareit.mappers;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.CommentMapper;
import ru.practicum.shareit.item.dto.CommentRequestDto;
import ru.practicum.shareit.item.dto.CommentResponseDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CommentMapperTest {

    @Test
    void toComment_shouldMapFieldsCorrectly() {
        CommentRequestDto dto = CommentRequestDto.builder()
                .text("Отличная вещь")
                .build();

        Item item = new Item();
        item.setId(1L);

        User author = new User();
        author.setId(2L);
        author.setName("Автор");

        Comment comment = CommentMapper.toComment(dto, item, author);

        assertThat(comment.getText()).isEqualTo("Отличная вещь");
        assertThat(comment.getItem()).isEqualTo(item);
        assertThat(comment.getAuthor()).isEqualTo(author);
    }

    @Test
    void toCommentResponseDto_shouldMapFieldsCorrectly() {
        User author = new User();
        author.setId(2L);
        author.setName("Автор");

        LocalDateTime created = LocalDateTime.now();

        Comment comment = Comment.builder()
                .id(10L)
                .text("Комментарий")
                .author(author)
                .created(created)
                .build();

        CommentResponseDto dto = CommentMapper.toCommentResponseDto(comment);

        assertThat(dto.getId()).isEqualTo(10L);
        assertThat(dto.getText()).isEqualTo("Комментарий");
        assertThat(dto.getAuthorName()).isEqualTo("Автор");
        assertThat(dto.getCreated()).isEqualTo(created);
    }

    @Test
    void toCommentResponseDtoList_shouldReturnEmptyList_whenNullPassed() {
        List<CommentResponseDto> result = CommentMapper.toCommentResponseDtoList(null);

        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }

    @Test
    void toCommentResponseDtoList_shouldMapListCorrectly() {
        User author1 = new User();
        author1.setId(1L);
        author1.setName("Автор1");

        User author2 = new User();
        author2.setId(2L);
        author2.setName("Автор2");

        Comment c1 = Comment.builder()
                .id(100L)
                .text("Коммент 1")
                .author(author1)
                .created(LocalDateTime.now().minusDays(1))
                .build();

        Comment c2 = Comment.builder()
                .id(200L)
                .text("Коммент 2")
                .author(author2)
                .created(LocalDateTime.now())
                .build();

        List<Comment> comments = List.of(c1, c2);

        List<CommentResponseDto> result = CommentMapper.toCommentResponseDtoList(comments);

        assertThat(result).hasSize(2);

        CommentResponseDto dto1 = result.get(0);
        CommentResponseDto dto2 = result.get(1);

        assertThat(dto1.getId()).isEqualTo(100L);
        assertThat(dto1.getText()).isEqualTo("Коммент 1");
        assertThat(dto1.getAuthorName()).isEqualTo("Автор1");

        assertThat(dto2.getId()).isEqualTo(200L);
        assertThat(dto2.getText()).isEqualTo("Коммент 2");
        assertThat(dto2.getAuthorName()).isEqualTo("Автор2");
    }
}