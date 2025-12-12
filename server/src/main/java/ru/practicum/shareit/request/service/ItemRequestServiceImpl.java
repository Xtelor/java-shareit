package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.ItemRequestMapper;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemRequestServiceImpl implements ItemRequestService {

    private final ItemRequestRepository requestRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    // Добавление нового запроса
    @Override
    @Transactional
    public ItemRequestDto create(Long userId, ItemRequestCreateDto dto) {
        User requester = getUserOrThrow(userId);

        ItemRequest request = ItemRequestMapper.mapToEntity(dto, requester);
        request = requestRepository.save(request);

        return ItemRequestMapper.mapToDto(request, List.of());
    }

    // Получение списка своих запросов
    @Override
    public List<ItemRequestDto> getOwn(Long userId) {
        // Проверка существования пользователя
        getUserOrThrow(userId);

        // Получение списка запросов
        List<ItemRequest> requests =
                requestRepository.findAllByRequesterIdOrderByCreatedDesc(userId);

        Map<Long, List<Item>> itemsByRequestId =
                getItemsByRequestIds(requests.stream()
                        .map(ItemRequest::getId)
                        .collect(Collectors.toList()));

        return requests.stream()
                .map(request -> ItemRequestMapper.mapToDto(
                        request,
                        itemsByRequestId.getOrDefault(request.getId(), List.of())
                ))
                .collect(Collectors.toList());
    }

    // Получение списка запросов, созданных другими пользователями
    @Override
    public List<ItemRequestDto> getAll(Long userId, int from, int size) {
        // Проверка существования пользователя
        getUserOrThrow(userId);

        Pageable pageable = PageRequest.of(from / size, size,
                Sort.by(Sort.Direction.DESC, "created")
        );

        Page<ItemRequest> page = requestRepository.findAllByRequesterIdNot(userId, pageable);

        // Получение списка запросов
        List<ItemRequest> requests = page.getContent();

        Map<Long, List<Item>> itemsByRequestId =
                getItemsByRequestIds(requests.stream()
                        .map(ItemRequest::getId)
                        .collect(Collectors.toList()));

        return requests.stream()
                .map(request -> ItemRequestMapper.mapToDto(
                        request,
                        itemsByRequestId.getOrDefault(request.getId(), List.of())
                ))
                .collect(Collectors.toList());
    }

    // Получение данных о конкретном запросе
    @Override
    public ItemRequestDto getById(Long userId, Long requestId) {
        // Проверка существования пользователя
        getUserOrThrow(userId);

        // Получение запроса
        ItemRequest request = findItemRequestById(requestId);

        // Получение списка вещей запроса
        List<Item> items = itemRepository.findAllByRequestId(requestId);

        return ItemRequestMapper.mapToDto(request, items);
    }

    // Метод для проверки существования пользователя
    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь " + userId + " не найден"));
    }

    private Map<Long, List<Item>> getItemsByRequestIds(List<Long> requestIds) {
        if (requestIds.isEmpty()) {
            return Map.of();
        }
        return itemRepository.findAllByRequestIdIn(requestIds).stream()
                .collect(Collectors.groupingBy(i -> i.getRequest().getId()));
    }

    // Проверка запроса на существование - возвращает запрос, если он существует
    private ItemRequest findItemRequestById(Long id) {
        return requestRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Запрос " + id + " не найден"));
    }
}
