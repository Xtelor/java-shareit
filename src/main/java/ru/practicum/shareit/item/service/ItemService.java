package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

public interface ItemService {
    // Добавление вещи
    ItemDto addItem(ItemDto dto, Long ownerId);

    // Получение вещи по ID
    ItemDto getItemById(Long id);

    // Обновление вещи
    ItemDto updateItem(Long itemId, ItemDto dto, Long ownerId);

    // Удаление вещи по ID
    void deleteItem(Long id);

    // Удаление всех вещей
    void deleteAllItems();

    // Получение списка вещей по ID их хозяина
    List<ItemDto> getItemsByOwner(Long ownerId);

    // Поиск
    List<ItemDto> searchForItem(String text);
}
