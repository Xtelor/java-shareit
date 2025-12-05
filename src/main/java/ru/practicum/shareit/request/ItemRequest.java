package ru.practicum.shareit.request;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

/**
 * TODO Sprint add-item-requests.
 */

@Data
@Builder
@EqualsAndHashCode(of = "id")
public class ItemRequest {
    private Long id;
    private String description;
    private User requester;
    private LocalDateTime created;
}
