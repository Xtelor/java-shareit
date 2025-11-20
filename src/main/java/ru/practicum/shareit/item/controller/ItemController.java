package ru.practicum.shareit.item.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.HttpHeaders;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.item.dto.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/items")
public class ItemController {
    private final ItemService itemService;

    @GetMapping
    public List<ItemDto> getItemsByOwner(@RequestHeader(HttpHeaders.USER_ID) Long ownerId) {
        return itemService.getItemsByOwner(ownerId);
    }

    @GetMapping("/{itemId}")
    public ItemDto getItemById(@PathVariable @Positive Long itemId) {
        return itemService.getItemById(itemId);
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

    @DeleteMapping
    public void deleteItems() {
        itemService.deleteAllItems();
    }

    @DeleteMapping("/{itemId}")
    public void deleteItem(@PathVariable @Positive Long itemId) {
        itemService.deleteItem(itemId);
    }
}
