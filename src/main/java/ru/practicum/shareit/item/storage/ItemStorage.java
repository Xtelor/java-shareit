package ru.practicum.shareit.item.storage;

import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemStorage {
    // Добавление вещи
    Item addItem(Item item);

    // Получение вещи по ID
    Item getItem(Long id);

    // Получение списка всех вещей
    List<Item> getItems();

    // Обновление вещи
    void updateItem(Item newItem);

    // Удаление вещи по ID
    void deleteItem(Long id);

    // Удаление всех вещей
    void deleteItems();

    // Получение списка всех вещей пользователя
    List<Item> getItemsByOwner(Long ownerId);

    // Поиск вещи по запросу
    List<Item> searchForItem(String text);
}
