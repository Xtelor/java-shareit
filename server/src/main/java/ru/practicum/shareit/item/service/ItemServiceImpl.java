package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingItemDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exceptions.ForbiddenException;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.item.CommentMapper;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.dto.CommentRequestDto;
import ru.practicum.shareit.item.dto.CommentResponseDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private final ItemRequestRepository itemRequestRepository;

    // Добавление вещи
    @Override
    @Transactional
    public ItemDto addItem(ItemDto dto, Long ownerId) {
        // Проверка существования пользователя
        User owner = findUserById(ownerId);

        ItemRequest request = null;

        // Проверка существования и получение запроса
        if (dto.getRequestId() != null) {
            request = findItemRequestById(dto.getRequestId());
        }

        Item item = ItemMapper.mapToItem(dto, request);
        item.setOwner(owner);

        Item createdItem = itemRepository.save(item);
        return ItemMapper.mapToItemDto(createdItem);
    }

    // Получение вещи по ID
    @Override
    public ItemDto getItemById(Long id) {
        Item item = findItemById(id);
        return ItemMapper.mapToItemDto(item);
    }

    // Получение вещи по ID c бронированием
    @Override
    public ItemDto getItemByIdWithBookings(Long userId, Long itemId) {
        // Валидация и получение вещи
        Item item = findItemById(itemId);
        ItemDto dto = ItemMapper.mapToItemDto(item);

        // Загрузка комментариев и бронирований
        loadCommentsAndBookings(dto, itemId, userId);

        return dto;
    }

    // Обновление вещи
    @Override
    @Transactional
    public ItemDto updateItem(Long itemId, ItemDto dto, Long ownerId) {
        // Проверяем, что пользователь-редактор существует
        findUserById(ownerId);
        Item existingItem = findItemById(itemId);

        // Проверка на владельца вещи
        if (existingItem.getOwner() == null || !Objects.equals(existingItem.getOwner().getId(), ownerId)) {
            throw new ForbiddenException("Редактировать вещь может только её владелец.");
        }

        ItemMapper.updateItemFromDto(dto, existingItem);
        Item saved = itemRepository.save(existingItem);

        return ItemMapper.mapToItemDto(saved);
    }

    // Удаление вещи по ID
    @Override
    @Transactional
    public void deleteItem(Long id) {
        // Проверка существования вещи
        findItemById(id);

        itemRepository.deleteById(id);
    }

    // Удаление всех вещей
    @Override
    @Transactional
    public void deleteAllItems() {
        itemRepository.deleteAllInBatch();
    }

    // Получение списка вещей по ID их хозяина с бронированиями
    @Override
    public List<ItemDto> getItemsByOwner(Long ownerId) {
        // Проверка существования владельца
        findUserById(ownerId);

        return itemRepository.findByOwnerId(ownerId).stream()
                .map(ItemMapper::mapToItemDto)
                .peek(dto -> loadCommentsAndBookings(dto, dto.getId(), ownerId))
                .sorted(Comparator.comparing(ItemDto::getId))
                .collect(Collectors.toList());
    }

    // Поиск
    @Override
    public List<ItemDto> searchForItem(String text) {
        if (text.isBlank()) {
            return List.of();
        }

        List<Item> items = itemRepository.search(text);

        return items.stream()
                .map(ItemMapper::mapToItemDto)
                .collect(Collectors.toList());
    }

    // Добавление комментария
    @Override
    @Transactional
    public CommentResponseDto addComment(Long userId, Long itemId, CommentRequestDto dto) {
        // Валидация и получение пользователя, вещи
        User author = findUserById(userId);
        Item item = findItemById(itemId);

        LocalDateTime now = LocalDateTime.now();

        boolean hasCompletedApprovedBooking =
                bookingRepository.existsByBookerIdAndItemIdAndStatusAndEndDateBefore(
                        userId,
                        itemId,
                        BookingStatus.APPROVED,
                        now
                );

        if (!hasCompletedApprovedBooking) {
            throw new ValidationException(
                    "Комментарий может оставить только пользователь, завершивший бронирование."
            );
        }

        Comment comment = Comment.builder()
                .text(dto.getText())
                .item(item)
                .author(author)
                .created(now)
                .build();

        Comment saved = commentRepository.save(comment);
        return CommentMapper.toCommentResponseDto(saved);
    }

    // Проверка вещи на существование - возвращает вещь, если она существует
    private Item findItemById(Long id) {
        return itemRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Вещь с id = " + id + " не найдена."));
    }

    // Проверка пользователя на существование - возвращает пользователя, если он существует
    private User findUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + id + " не найден."));
    }

    // Проверка запроса на существование - возвращает запрос, если он существует
    private ItemRequest findItemRequestById(Long id) {
        return itemRequestRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Запрос не найден."));
    }

    // Вспомогательные методы для загрузки комментариев и бронирований
    private void loadCommentsAndBookings(ItemDto dto, Long itemId, Long viewerId) {
        // Комментарии
        List<Comment> comments = commentRepository.findByItemId(
                itemId,
                Sort.by(Sort.Direction.DESC, "created")
        );
        dto.setComments(CommentMapper.toCommentResponseDtoList(comments));

        // Бронирования
        Item item = itemRepository.getReferenceById(itemId); // или findById, если боишься прокси
        if (item.getOwner() != null && Objects.equals(viewerId, item.getOwner().getId())) {
            LocalDateTime now = LocalDateTime.now();

            bookingRepository.findFirstByItemIdAndStartDateLessThanEqualAndStatus(
                            itemId, now, BookingStatus.APPROVED,
                            Sort.by(Sort.Direction.DESC, "startDate"))
                    .ifPresent(booking -> dto.setLastBooking(toBookingItemDto(booking)));

            bookingRepository.findFirstByItemIdAndStartDateAfterAndStatus(
                            itemId, now, BookingStatus.APPROVED,
                            Sort.by(Sort.Direction.ASC, "startDate"))
                    .ifPresent(booking -> dto.setNextBooking(toBookingItemDto(booking)));
        }
    }

    private BookingItemDto toBookingItemDto(Booking b) {
        return BookingItemDto.builder()
                .id(b.getId())
                .bookerId(b.getBooker().getId())
                .start(b.getStartDate())
                .end(b.getEndDate())
                .build();
    }
}