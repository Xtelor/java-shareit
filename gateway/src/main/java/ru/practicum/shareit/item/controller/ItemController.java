package ru.practicum.shareit.item.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.ItemClient;
import ru.practicum.shareit.item.dto.CommentRequestDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.validation.OnCreate;
import ru.practicum.shareit.validation.OnUpdate;
import java.util.List;
import static ru.practicum.shareit.Headers.USER_ID;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
@Validated
public class ItemController {

    private final ItemClient itemClient;

    @PostMapping
    public ResponseEntity<Object> addItem(@RequestHeader(USER_ID) @Positive Long ownerId,
                                          @RequestBody @Validated(OnCreate.class) ItemDto dto) {
        return itemClient.addItem(ownerId, dto);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<Object> updateItem(@RequestHeader(USER_ID) @Positive Long ownerId,
                                             @PathVariable @Positive Long itemId,
                                             @RequestBody  @Validated(OnUpdate.class) ItemDto dto) {
        return itemClient.updateItem(ownerId, itemId, dto);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<Object> getItemById(@RequestHeader(USER_ID) @Positive Long userId,
                                              @PathVariable @Positive Long itemId) {
        return itemClient.getItemById(userId, itemId);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> addComment(@RequestHeader(USER_ID) @Positive Long userId,
                                             @PathVariable @Positive Long itemId,
                                             @RequestBody @Valid CommentRequestDto dto) {
        return itemClient.addComment(userId, itemId, dto);
    }

    @GetMapping
    public ResponseEntity<Object> getItemsByOwner(@RequestHeader(USER_ID) @Positive Long ownerId,
                                                  @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
                                                  @RequestParam(defaultValue = "10") @Positive Integer size) {
        return itemClient.getItemsByOwner(ownerId, from, size);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> search(@RequestParam String text,
                                         @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
                                         @RequestParam(defaultValue = "10") @Positive Integer size) {
        if (text.isBlank()) {
            return ResponseEntity.ok(List.of());
        }
        return itemClient.search(text, from, size);
    }
}
