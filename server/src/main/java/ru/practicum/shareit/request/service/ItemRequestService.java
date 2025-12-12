package ru.practicum.shareit.request.service;

import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.List;

public interface ItemRequestService {
    // Добавление нового запроса
    ItemRequestDto create(Long userId, ItemRequestCreateDto dto);

    // Получение списка своих запросов
    List<ItemRequestDto> getOwn(Long userId);

    // Получение списка запросов, созданных другими пользователями
    List<ItemRequestDto> getAll(Long userId, int from, int size);

    // Получение данных о конкретном запросе
    ItemRequestDto getById(Long userId, Long requestId);
}
