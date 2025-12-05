package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.BookingMapper;
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
import ru.practicum.shareit.item.dto.ItemWithBookingsDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;


    // Добавление вещи
    @Override
    @Transactional
    public ItemDto addItem(ItemDto dto, Long ownerId) {
        // Проверка существования пользователя
        User owner = findUserById(ownerId);

        Item item = ItemMapper.mapToItem(dto);
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
    public ItemWithBookingsDto getItemByIdWithBookings(Long userId, Long itemId) {
        Item item = findItemById(itemId);

        // Получение комментариев
        List<Comment> comments = commentRepository.findByItemId(itemId,
                Sort.by(Sort.Direction.DESC, "created"));
        List<CommentResponseDto> commentDtos = CommentMapper.toCommentResponseDtoList(comments);

        ItemWithBookingsDto dto = ItemMapper.toItemWithBookingsDto(item, commentDtos);

        if (item.getOwner() != null && Objects.equals(item.getOwner().getId(), userId)) {
            addBookingsToItem(dto, itemId);
        }

        return dto;
    }

    // Обновление вещи
    @Override
    @Transactional
    public ItemDto updateItem(Long itemId, ItemDto dto, Long ownerId) {
        findUserById(ownerId); // Проверяем, что пользователь-редактор существует
        Item existingItem = findItemById(itemId);

        // Проверка на владельца вещи
        if (existingItem.getOwner() == null || !existingItem.getOwner().getId().equals(ownerId)) {
            throw new ForbiddenException("Редактировать вещь может только её владелец.");
        }

        ItemMapper.updateItemFromDto(dto, existingItem);
        existingItem = itemRepository.save(existingItem);

        return ItemMapper.mapToItemDto(existingItem);
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
    public List<ItemWithBookingsDto> getItemsByOwner(Long ownerId) {
        // Проверка существования пользователя
        findUserById(ownerId);

        return itemRepository.findByOwnerId(ownerId).stream()
                .map(item -> {
                    // 1. Комментарии
                    List<Comment> comments = commentRepository.findByItemId(item.getId(),
                            Sort.by(Sort.Direction.DESC, "created"));
                    List<CommentResponseDto> commentDtos = CommentMapper.toCommentResponseDtoList(comments);

                    // 2. DTO
                    ItemWithBookingsDto dto = ItemMapper.toItemWithBookingsDto(item, commentDtos);

                    // 3. Бронирования
                    addBookingsToItem(dto, item.getId());
                    return dto;
                })
                .sorted(Comparator.comparing(ItemWithBookingsDto::getId))
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
        // Валидация пользователя, вещи и комментария
        User user = findUserById(userId);
        Item item = findItemById(itemId);
        validateComment(userId, itemId);

        Comment comment = CommentMapper.toComment(dto, item, user);
        return CommentMapper.toCommentResponseDto(commentRepository.save(comment));
    }

    // Метод проверки возможности добавления комментария
    private void validateComment(Long userId, Long itemId) {
        LocalDateTime now = LocalDateTime.now();
        boolean hasBooking = bookingRepository.existsByBooker_IdAndItem_IdAndEndDateIsBeforeAndStatus(
                userId, itemId, now, BookingStatus.APPROVED);

        if (!hasBooking) {
            throw new ValidationException("Вы не можете оставить комментарий к этой вещи, " +
                    "так как не брали её в аренду или аренда ещё не завершена.");
        }
    }

    // Вспомогательный метод для добавления бронирований к вещи
    private void addBookingsToItem(ItemWithBookingsDto dto, Long itemId) {
        LocalDateTime now = LocalDateTime.now();

        // (Исправлено поле сортировки на startDate и добавлены проверки на null)
        Booking lastBooking = bookingRepository
                .findFirstByItemIdAndStartDateLessThanEqualAndStatus(
                        itemId, now, BookingStatus.APPROVED,
                        Sort.by(Sort.Direction.DESC, "startDate"))
                .orElse(null);

        Booking nextBooking = bookingRepository
                .findFirstByItemIdAndStartDateAfterAndStatus(
                        itemId, now, BookingStatus.APPROVED,
                        Sort.by(Sort.Direction.DESC, "startDate"))
                .orElse(null);

        dto.setLastBooking(lastBooking != null ? BookingMapper.toBookingItemDto(lastBooking) : null);
        dto.setNextBooking(nextBooking != null ? BookingMapper.toBookingItemDto(nextBooking) : null);
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
}