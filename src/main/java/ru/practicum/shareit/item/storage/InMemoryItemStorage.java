package ru.practicum.shareit.item.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.model.Item;

import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class InMemoryItemStorage implements ItemStorage {
    private final Map<Long, Item> items = new HashMap<>();

    // Добавление вещи
    @Override
    public Item addItem(Item item) {
        item.setId(getNextId());
        items.put(item.getId(), item);
        return item;
    }

    // Получение вещи по ID
    @Override
    public Item getItem(Long id) {
        return items.get(id);
    }

    // Получение списка всех вещей
    @Override
    public List<Item> getItems() {
        return new ArrayList<>(items.values());
    }

    // Обновление вещи
    @Override
    public void updateItem(Item item) {
        items.put(item.getId(), item);
    }

    // Удаление вещи по ID
    @Override
    public void deleteItem(Long id) {
        items.remove(id);
    }

    // Удаление всех вещей
    @Override
    public void deleteItems() {
        items.clear();
    }

    // Получение списка вещей по ID пользователя-хозяина
    @Override
    public List<Item> getItemsByOwner(Long ownerId) {
        return items.values()
                .stream()
                .filter(item -> Objects.equals(item.getOwner().getId(), ownerId))
                .collect(Collectors.toList());
    }

    // Поиск вещи по запросу
    @Override
    public List<Item> searchForItem(String text) {
        String query = text.toLowerCase();

        return items.values().stream()
                .filter(Item::getAvailable)
                .filter(item -> item.getName().toLowerCase().contains(query) ||
                        item.getDescription().toLowerCase().contains(query)
                )
                .collect(Collectors.toList());

    }

    // Получение уникального ID для следующей вещи
    private Long getNextId() {
        long currentMaxId = items.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0L);
        return ++currentMaxId;
    }
}
