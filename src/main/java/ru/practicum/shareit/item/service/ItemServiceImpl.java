package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemStorage;
import ru.practicum.shareit.user.storage.UserStorage;
import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemStorage itemStorage;
    private final UserStorage userStorage;

    // Добавление вещи
    @Override
    public ItemDto addItem(ItemDto dto, Long ownerId) {
        // Проверка существования пользователя
        User owner = findUserById(ownerId);

        Item item = ItemMapper.mapToItem(dto);
        item.setOwner(owner);

        Item createdItem = itemStorage.addItem(item);
        return ItemMapper.mapToItemDto(createdItem);
    }

    // Получение вещи по ID
    @Override
    public ItemDto getItemById(Long id) {
        Item item = findItemById(id);

        return ItemMapper.mapToItemDto(item);
    }

    // Обновление вещи
    @Override
    public ItemDto updateItem(Long itemId, ItemDto dto, Long ownerId) {
        findUserById(ownerId); // Проверяем, что пользователь-редактор существует
        Item existingItem = findItemById(itemId);

        // Проверка владельца пользователя
        if (!Objects.equals(existingItem.getOwner().getId(), ownerId)) {
            throw new ValidationException("Редактировать вещь может только её владелец.");
        }

        // Обновление полей
        if (dto.getName() != null && !dto.getName().isBlank()) {
            existingItem.setName(dto.getName());
        }
        if (dto.getDescription() != null && !dto.getDescription().isBlank()) {
            existingItem.setDescription(dto.getDescription());
        }
        if (dto.getAvailable() != null) {
            existingItem.setAvailable(dto.getAvailable());
        }

        itemStorage.updateItem(existingItem);
        return ItemMapper.mapToItemDto(existingItem);
    }

    // Удаление вещи по ID
    @Override
    public void deleteItem(Long id) {
        // Проверка существования вещи
        findItemById(id);
        itemStorage.deleteItem(id);
    }

    // Удаление всех вещей
    @Override
    public void deleteAllItems() {
        itemStorage.deleteItems();
    }

    // Получение списка вещей пользователя
    @Override
    public List<ItemDto> getItemsByOwner(Long ownerId) {
        findUserById(ownerId);

        return itemStorage.getItemsByOwner(ownerId).stream()
                .map(ItemMapper::mapToItemDto)
                .collect(Collectors.toList());
    }

    // Поиск
    @Override
    public List<ItemDto> searchForItem(String text) {
        if (text.isBlank()) {
            return List.of();
        }

        List<Item> items = itemStorage.searchForItem(text);

        return items.stream()
                .map(ItemMapper::mapToItemDto)
                .collect(Collectors.toList());
    }

    // Проверка вещи на существование - возвращает вещь, если она существует
    private Item findItemById(Long id) {
        Item item = itemStorage.getItem(id);

        if (item == null) {
            throw new NotFoundException("Вещь с id = " + id + " не найдена.");
        }

        return item;
    }

    // Проверка пользователя на существование - возвращает пользователя, если он существует
    private User findUserById(Long id) {
        User user = userStorage.getUser(id);

        if (user == null) {
            throw new NotFoundException("Пользователь с id = " + id + " не найден.");
        }

        return user;
    }
}
