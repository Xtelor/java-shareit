package ru.practicum.shareit.item.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.item.dto.*;
import java.util.List;
import static ru.practicum.shareit.HttpHeaders.USER_ID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/items")
public class ItemController {
    private final ItemService itemService;

    @GetMapping
    public List<ItemDto> getItemsByOwner(@RequestHeader(USER_ID) Long ownerId) {
        return itemService.getItemsByOwner(ownerId);
    }

    @GetMapping("/{itemId}")
    public ItemDto getItemById(@RequestHeader(USER_ID) Long userId,
                                           @PathVariable Long itemId) {
        return itemService.getItemByIdWithBookings(userId, itemId);
    }

    @GetMapping("/search")
    public List<ItemDto> searchItems(@RequestParam String text) {
        return itemService.searchForItem(text);
    }

    @PostMapping
    public ItemDto createItem(@RequestBody ItemDto dto,
                              @RequestHeader(USER_ID) Long ownerId) {
        return itemService.addItem(dto, ownerId);
    }

    @PatchMapping("/{itemId}")
    public ItemDto updateItem(@PathVariable Long itemId,
                              @RequestBody ItemDto dto,
                              @RequestHeader(USER_ID) Long ownerId) {
        return itemService.updateItem(itemId, dto, ownerId);
    }

    @PostMapping("/{itemId}/comment")
    public CommentResponseDto addComment(@RequestHeader(USER_ID) Long userId,
                                         @PathVariable Long itemId,
                                         @RequestBody CommentRequestDto dto) {
        return itemService.addComment(userId, itemId, dto);
    }

    @DeleteMapping
    public void deleteItems() {
        itemService.deleteAllItems();
    }

    @DeleteMapping("/{itemId}")
    public void deleteItem(@PathVariable Long itemId) {
        itemService.deleteItem(itemId);
    }
}
