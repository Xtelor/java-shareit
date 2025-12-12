package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.*;
import ru.practicum.shareit.booking.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exceptions.ForbiddenException;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    private static final Sort SORT_BY_START_DESC = Sort.by(Sort.Direction.DESC, "startDate");

    // Добавление нового запроса на бронирование
    @Override
    @Transactional
    public BookingResponseDto create(Long userId, BookingRequestDto dto) {
        User booker = getUserById(userId);
        Item item = getItemById(dto.getItemId());

        // Владелец не может бронировать свою вещь
        if (item.getOwner() == null || Objects.equals(item.getOwner().getId(), userId)) {
            throw new ForbiddenException("Владелец не может забронировать свою вещь.");
        }

        // Проверка доступности вещи
        if (!item.getAvailable()) {
            throw new ValidationException("Вещь недоступна для бронирования.");
        }

        // Валидация дат
        validateDates(dto.getStart(), dto.getEnd());

        Booking booking = BookingMapper.toBooking(dto, item, booker);
        booking.setStatus(BookingStatus.WAITING);

        booking = bookingRepository.save(booking);
        return BookingMapper.toBookingResponseDto(booking);
    }

    // Подтверждение/отклонение запроса на бронирование
    @Override
    @Transactional
    public BookingResponseDto approve(Long userId, Long bookingId, Boolean approved) {
        // Проверка существования бронирования
        Booking booking = getBookingById(bookingId);

        // Только владелец вещи может подтверждать бронирование
        if (!Objects.equals(booking.getItem().getOwner().getId(), userId)) {
            throw new ForbiddenException("Только владелец вещи может подтвердить бронирование.");
        }

        // Проверка бронирования
        if (booking.getStatus() != BookingStatus.WAITING) {
            throw new ValidationException("Бронирование уже обработано.");
        }

        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        booking = bookingRepository.save(booking);

        return BookingMapper.toBookingResponseDto(booking);
    }

    // Получение данных о конкретном бронировании
    @Override
    public BookingResponseDto getById(Long userId, Long bookingId) {
        // Проверка существования бронирования
        Booking booking = getBookingById(bookingId);

        // Доступ только для бронирующего или владельца вещи
        Item item = booking.getItem();
        if (item == null || item.getOwner() == null) {
            throw new ForbiddenException("Доступ запрещён.");
        }

        if (!Objects.equals(booking.getBooker().getId(), userId)
                && !Objects.equals(item.getOwner().getId(), userId)) {
            throw new ForbiddenException("Доступ запрещён.");
        }

        return BookingMapper.toBookingResponseDto(booking);
    }

    // Получение списка всех бронирований текущего пользователя
    @Override
    public List<BookingResponseDto> getAllByBooker(Long userId, BookingState state) {
        // Проверка существования пользователя
        getUserById(userId);

        List<Booking> bookings = findBookingsByBooker(userId, state);

        return bookings.stream()
                .map(BookingMapper::toBookingResponseDto)
                .collect(Collectors.toList());
    }

    // Получение списка бронирований для хозяина вещи
    @Override
    public List<BookingResponseDto> getAllByOwner(Long userId, BookingState state) {
        // Проверка существования пользователя
        getUserById(userId);

        List<Booking> bookings = findBookingsByOwner(userId, state);

        return bookings.stream()
                .map(BookingMapper::toBookingResponseDto)
                .collect(Collectors.toList());
    }

    // Вспомогательные методы для получения списков бронирования
    private List<Booking> findBookingsByBooker(Long userId, BookingState state) {
        LocalDateTime now = LocalDateTime.now();

        return switch (state) {
            case CURRENT -> bookingRepository.findCurrentByBookerId(userId, now, SORT_BY_START_DESC);
            case PAST -> bookingRepository.findByBookerIdAndEndDateIsBefore(userId, now, SORT_BY_START_DESC);
            case FUTURE -> bookingRepository.findByBookerIdAndStartDateIsAfter(userId, now, SORT_BY_START_DESC);
            case WAITING ->
                    bookingRepository.findByBookerIdAndStatus(userId, BookingStatus.WAITING, SORT_BY_START_DESC);
            case REJECTED ->
                    bookingRepository.findByBookerIdAndStatus(userId, BookingStatus.REJECTED, SORT_BY_START_DESC);
            default -> bookingRepository.findByBookerId(userId, SORT_BY_START_DESC);
        };
    }

    private List<Booking> findBookingsByOwner(Long userId, BookingState state) {
        LocalDateTime now = LocalDateTime.now();

        return switch (state) {
            case CURRENT -> bookingRepository.findCurrentByItemOwnerId(userId, now, SORT_BY_START_DESC);
            case PAST -> bookingRepository.findByItemOwnerIdAndEndDateIsBefore(userId, now, SORT_BY_START_DESC);
            case FUTURE -> bookingRepository.findByItemOwnerIdAndStartDateIsAfter(userId, now, SORT_BY_START_DESC);
            case WAITING ->
                    bookingRepository.findByItemOwnerIdAndStatus(userId, BookingStatus.WAITING, SORT_BY_START_DESC);
            case REJECTED ->
                    bookingRepository.findByItemOwnerIdAndStatus(userId, BookingStatus.REJECTED, SORT_BY_START_DESC);
            default -> bookingRepository.findByItemOwnerId(userId, SORT_BY_START_DESC);
        };
    }

    // Валидация дат бронирования
    private void validateDates(LocalDateTime start, LocalDateTime end) {
        if (end.isBefore(start) || Objects.equals(end, start)) {
            throw new ValidationException("Дата окончания должна быть после даты начала");
        }
    }

    // Проверка существования пользователя
    private User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден: " + userId));
    }

    // Проверка существования вещи
    private Item getItemById(Long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь не найдена: " + itemId));
    }

    // Проверка существования запроса на бронирование
    private Booking getBookingById(Long bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование не найдено: " + bookingId));
    }
}