package ru.practicum.shareit.item.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.HttpHeaders;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.item.dto.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/items")
@Validated
public class ItemController {
    private final ItemService itemService;

    @GetMapping
    public List<ItemWithBookingsDto> getItemsByOwner(@RequestHeader(HttpHeaders.USER_ID) @Positive Long ownerId) {
        return itemService.getItemsByOwner(ownerId);
    }

    @GetMapping("/{itemId}")
    public ItemWithBookingsDto getItemById(@RequestHeader(HttpHeaders.USER_ID) @Positive Long userId,
                               @PathVariable @Positive Long itemId) {
        return itemService.getItemByIdWithBookings(userId, itemId);
    }

    @GetMapping("/search")
    public List<ItemDto> searchItems(@RequestParam String text) {
        return itemService.searchForItem(text);
    }

    @PostMapping
    public ItemDto createItem(@Valid @RequestBody ItemDto dto,
                              @RequestHeader(HttpHeaders.USER_ID) @Positive Long ownerId) {
        return itemService.addItem(dto, ownerId);
    }

    @PatchMapping("/{itemId}")
    public ItemDto updateItem(@PathVariable @Positive Long itemId,
                              @RequestBody ItemDto dto,
                              @RequestHeader(HttpHeaders.USER_ID) @Positive Long ownerId) {
        return itemService.updateItem(itemId, dto, ownerId);
    }

    @PostMapping("/{itemId}/comment")
    public CommentResponseDto addComment(
            @RequestHeader(HttpHeaders.USER_ID) @Positive Long userId,
            @PathVariable Long itemId,
            @Valid @RequestBody CommentRequestDto dto) {
        return itemService.addComment(userId, itemId, dto);
    }

    @DeleteMapping
    public void deleteItems() {
        itemService.deleteAllItems();
    }

    @DeleteMapping("/{itemId}")
    public void deleteItem(@PathVariable @Positive Long itemId) {
        itemService.deleteItem(itemId);
    }
}
