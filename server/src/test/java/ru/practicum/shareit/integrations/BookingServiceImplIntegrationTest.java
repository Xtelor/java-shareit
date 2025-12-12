package ru.practicum.shareit.integrations;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exceptions.ForbiddenException;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.jpa.properties.hibernate.id.new_generator_mappings=false"
})
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class BookingServiceImplIntegrationTest {

    private final BookingService bookingService;
    private final UserService userService;
    private final ItemService itemService;

    private final LocalDateTime now = LocalDateTime.now();

    // Создание бронирования — успешно
    @Test
    void create_shouldCreateBookingSuccessfully() {
        UserDto owner = userService.addUser(UserDto.builder()
                .name("Владелец")
                .email("owner@test.ru")
                .build());

        UserDto booker = userService.addUser(UserDto.builder()
                .name("Букер")
                .email("booker@test.ru")
                .build());

        ItemDto item = itemService.addItem(ItemDto.builder()
                .name("Дрель")
                .description("Профессиональная")
                .available(true)
                .build(), owner.getId());

        BookingRequestDto requestDto = BookingRequestDto.builder()
                .itemId(item.getId())
                .start(now.plusDays(1))
                .end(now.plusDays(3))
                .build();

        BookingResponseDto saved = bookingService.create(booker.getId(), requestDto);

        assertThat(saved.getId()).isPositive();
        assertThat(saved.getStatus()).isEqualTo(BookingStatus.WAITING);
        assertThat(saved.getBooker().getId()).isEqualTo(booker.getId());
        assertThat(saved.getItem().getId()).isEqualTo(item.getId());
    }

    // Создание бронирования своей вещи — ForbiddenException
    @Test
    void create_shouldThrowForbiddenException_whenBookingOwnItem() {
        UserDto owner = userService.addUser(UserDto.builder()
                .name("Владелец")
                .email("o@test.ru")
                .build());

        ItemDto item = itemService.addItem(ItemDto.builder()
                .name("Вещь")
                .description("Моя")
                .available(true)
                .build(), owner.getId());

        BookingRequestDto requestDto = BookingRequestDto.builder()
                .itemId(item.getId())
                .start(now.plusDays(1))
                .end(now.plusDays(2))
                .build();

        assertThatThrownBy(() -> bookingService.create(owner.getId(), requestDto))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("Владелец не может забронировать свою вещь.");
    }

    // Создание бронирования на недоступную вещь — ValidationException
    @Test
    void create_shouldThrowValidationException_whenItemNotAvailable() {
        UserDto owner = userService.addUser(UserDto.builder()
                .name("Владелец")
                .email("o@test.ru")
                .build());

        UserDto booker = userService.addUser(UserDto.builder()
                .name("Букер")
                .email("b@test.ru")
                .build());

        ItemDto item = itemService.addItem(ItemDto.builder()
                .name("Занятая вещь")
                .description("Недоступна")
                .available(false)
                .build(), owner.getId());

        BookingRequestDto requestDto = BookingRequestDto.builder()
                .itemId(item.getId())
                .start(now.plusDays(1))
                .end(now.plusDays(2))
                .build();

        assertThatThrownBy(() -> bookingService.create(booker.getId(), requestDto))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Вещь недоступна для бронирования.");
    }

    // Создание бронирования с датой окончания раньше начала — ValidationException
    @Test
    void create_shouldThrowValidationException_whenEndBeforeStart() {
        UserDto owner = userService.addUser(UserDto.builder()
                .name("Владелец")
                .email("o@test.ru")
                .build());

        UserDto booker = userService.addUser(UserDto.builder()
                .name("Букер")
                .email("b@test.ru")
                .build());

        ItemDto item = itemService.addItem(ItemDto.builder()
                .name("Вещь")
                .description("Тест")
                .available(true)
                .build(), owner.getId());

        BookingRequestDto requestDto = BookingRequestDto.builder()
                .itemId(item.getId())
                .start(now.plusDays(2))
                .end(now.plusDays(1))
                .build();

        assertThatThrownBy(() -> bookingService.create(booker.getId(), requestDto))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Дата окончания должна быть после даты начала");
    }

    // Одобрение бронирования — владельцем
    @Test
    void approve_shouldApproveBooking_whenOwner() {
        UserDto owner = userService.addUser(UserDto.builder()
                .name("Владелец")
                .email("o@test.ru")
                .build());

        UserDto booker = userService.addUser(UserDto.builder()
                .name("Букер")
                .email("b@test.ru")
                .build());

        ItemDto item = itemService.addItem(ItemDto.builder()
                .name("Вещь")
                .description("Тест")
                .available(true)
                .build(), owner.getId());

        BookingResponseDto booking = bookingService.create(booker.getId(), BookingRequestDto.builder()
                .itemId(item.getId())
                .start(now.plusDays(1))
                .end(now.plusDays(2))
                .build());

        BookingResponseDto approved = bookingService.approve(owner.getId(), booking.getId(), true);

        assertThat(approved.getStatus()).isEqualTo(BookingStatus.APPROVED);
    }

    // Отклонение бронирования
    @Test
    void approve_shouldRejectBooking_whenApprovedFalse() {
        UserDto owner = userService.addUser(UserDto.builder()
                .name("Владелец")
                .email("o@test.ru")
                .build());

        UserDto booker = userService.addUser(UserDto.builder()
                .name("Букер")
                .email("b@test.ru")
                .build());

        ItemDto item = itemService.addItem(ItemDto.builder()
                .name("Вещь")
                .description("Тест")
                .available(true)
                .build(), owner.getId());

        BookingResponseDto booking = bookingService.create(booker.getId(), BookingRequestDto.builder()
                .itemId(item.getId())
                .start(now.plusDays(1))
                .end(now.plusDays(2))
                .build());

        BookingResponseDto rejected = bookingService.approve(owner.getId(), booking.getId(), false);

        assertThat(rejected.getStatus()).isEqualTo(BookingStatus.REJECTED);
    }

    // Одобрение не своей вещи — ForbiddenException
    @Test
    void approve_shouldThrowForbiddenException_whenNotOwner() {
        UserDto owner = userService.addUser(UserDto.builder()
                .name("Владелец")
                .email("o@test.ru")
                .build());

        UserDto booker = userService.addUser(UserDto.builder()
                .name("Букер")
                .email("b@test.ru")
                .build());

        UserDto stranger = userService.addUser(UserDto.builder()
                .name("Чужой")
                .email("s@test.ru")
                .build());

        ItemDto item = itemService.addItem(ItemDto.builder()
                .name("Вещь")
                .description("Тест")
                .available(true)
                .build(), owner.getId());

        BookingResponseDto booking = bookingService.create(booker.getId(), BookingRequestDto.builder()
                .itemId(item.getId())
                .start(now.plusDays(1))
                .end(now.plusDays(2))
                .build());

        assertThatThrownBy(() -> bookingService.approve(stranger.getId(), booking.getId(), true))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("Только владелец вещи может подтвердить бронирование.");
    }

    // Повторное одобрение — ValidationException
    @Test
    void approve_shouldThrowValidationException_whenAlreadyProcessed() {
        UserDto owner = userService.addUser(UserDto.builder()
                .name("Владелец")
                .email("o@test.ru")
                .build());

        UserDto booker = userService.addUser(UserDto.builder()
                .name("Букер")
                .email("b@test.ru")
                .build());

        ItemDto item = itemService.addItem(ItemDto.builder()
                .name("Вещь")
                .description("Тест")
                .available(true)
                .build(), owner.getId());

        BookingResponseDto booking = bookingService.create(booker.getId(), BookingRequestDto.builder()
                .itemId(item.getId())
                .start(now.plusDays(1))
                .end(now.plusDays(2))
                .build());

        bookingService.approve(owner.getId(), booking.getId(), true);

        assertThatThrownBy(() -> bookingService.approve(owner.getId(), booking.getId(), false))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Бронирование уже обработано.");
    }

    // Получение бронирования — владельцем и бронирующим
    @Test
    void getById_shouldReturnBooking_forOwnerAndBooker() {
        UserDto owner = userService.addUser(UserDto.builder()
                .name("Владелец")
                .email("o@test.ru")
                .build());

        UserDto booker = userService.addUser(UserDto.builder()
                .name("Букер")
                .email("b@test.ru")
                .build());

        ItemDto item = itemService.addItem(ItemDto.builder()
                .name("Вещь")
                .description("Тест")
                .available(true)
                .build(), owner.getId());

        BookingResponseDto booking = bookingService.create(booker.getId(), BookingRequestDto.builder()
                .itemId(item.getId())
                .start(now.plusDays(1))
                .end(now.plusDays(2))
                .build());

        BookingResponseDto fromOwner = bookingService.getById(owner.getId(), booking.getId());
        BookingResponseDto fromBooker = bookingService.getById(booker.getId(), booking.getId());

        assertThat(fromOwner.getId()).isEqualTo(booking.getId());
        assertThat(fromBooker.getId()).isEqualTo(booking.getId());
    }

    // Получение чужого бронирования — ForbiddenException
    @Test
    void getById_shouldThrowForbiddenException_whenNotOwnerAndNotBooker() {
        UserDto owner = userService.addUser(UserDto.builder()
                .name("Владелец")
                .email("o@test.ru")
                .build());

        UserDto booker = userService.addUser(UserDto.builder()
                .name("Букер")
                .email("b@test.ru")
                .build());

        UserDto stranger = userService.addUser(UserDto.builder()
                .name("Чужой")
                .email("s@test.ru")
                .build());

        ItemDto item = itemService.addItem(ItemDto.builder()
                .name("Вещь")
                .description("Тест")
                .available(true)
                .build(), owner.getId());

        BookingResponseDto booking = bookingService.create(booker.getId(), BookingRequestDto.builder()
                .itemId(item.getId())
                .start(now.plusDays(1))
                .end(now.plusDays(2))
                .build());

        assertThatThrownBy(() -> bookingService.getById(stranger.getId(), booking.getId()))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("Доступ запрещён.");
    }

    // getAllByBooker — состояние ALL
    @Test
    void getAllByBooker_withStateALL_shouldReturnAllBookings() {
        UserDto owner = userService.addUser(UserDto.builder()
                .name("Владелец")
                .email("o@test.ru")
                .build());

        UserDto booker = userService.addUser(UserDto.builder()
                .name("Букер")
                .email("b@test.ru")
                .build());

        ItemDto item = itemService.addItem(ItemDto.builder()
                .name("Вещь")
                .description("Тест")
                .available(true)
                .build(), owner.getId());

        bookingService.create(booker.getId(), BookingRequestDto.builder()
                .itemId(item.getId())
                .start(now.plusDays(1))
                .end(now.plusDays(2))
                .build());

        bookingService.create(booker.getId(), BookingRequestDto.builder()
                .itemId(item.getId())
                .start(now.plusDays(5))
                .end(now.plusDays(6))
                .build());

        List<BookingResponseDto> all = bookingService.getAllByBooker(booker.getId(), BookingState.ALL);

        assertThat(all).hasSize(2);
    }

    // getAllByOwner — состояние WAITING
    @Test
    void getAllByOwner_withStateWAITING_shouldReturnOnlyWaitingBookings() {
        UserDto owner = userService.addUser(UserDto.builder()
                .name("Владелец")
                .email("o@test.ru")
                .build());

        UserDto booker = userService.addUser(UserDto.builder()
                .name("Букер")
                .email("b@test.ru")
                .build());

        ItemDto item = itemService.addItem(ItemDto.builder()
                .name("Вещь")
                .description("Тест")
                .available(true)
                .build(), owner.getId());

        bookingService.create(booker.getId(),
                BookingRequestDto.builder()
                        .itemId(item.getId())
                        .start(now.plusDays(1))
                        .end(now.plusDays(2))
                        .build());

        List<BookingResponseDto> waiting = bookingService.getAllByOwner(owner.getId(), BookingState.WAITING);

        assertThat(waiting).hasSize(1);
        assertThat(waiting.getFirst().getStatus()).isEqualTo(BookingStatus.WAITING);
    }

    // getAllByBooker — FUTURE включает и WAITING, и REJECTED с датой в будущем
    @Test
    void getAllByBooker_futureStateIncludesAllFutureBookingsRegardlessOfStatus() {
        UserDto owner = userService.addUser(UserDto.builder()
                .name("Владелец")
                .email("o@test.ru")
                .build());

        UserDto booker = userService.addUser(UserDto.builder()
                .name("Букер")
                .email("b@test.ru")
                .build());

        ItemDto item = itemService.addItem(ItemDto.builder()
                .name("Вещь")
                .description("Тест")
                .available(true)
                .build(), owner.getId());

        LocalDateTime fixedNow = LocalDateTime.now();

        // 1. Прошлое
        bookingService.create(booker.getId(), BookingRequestDto.builder()
                .itemId(item.getId())
                .start(fixedNow.minusDays(10))
                .end(fixedNow.minusDays(8))
                .build());

        // 2. Текущее
        bookingService.create(booker.getId(), BookingRequestDto.builder()
                .itemId(item.getId())
                .start(fixedNow.minusHours(1))
                .end(fixedNow.plusHours(1))
                .build());

        // 3. Будущее + WAITING
        bookingService.create(booker.getId(), BookingRequestDto.builder()
                .itemId(item.getId())
                .start(fixedNow.plusDays(1))
                .end(fixedNow.plusDays(2))
                .build());

        // 4. Будущее + REJECTED
        BookingResponseDto rejected = bookingService.create(booker.getId(), BookingRequestDto.builder()
                .itemId(item.getId())
                .start(fixedNow.plusDays(5))
                .end(fixedNow.plusDays(6))
                .build());

        bookingService.approve(owner.getId(), rejected.getId(), false);

        // 5. Будущее + WAITING (ещё одно)
        bookingService.create(booker.getId(), BookingRequestDto.builder()
                .itemId(item.getId())
                .start(fixedNow.plusDays(10))
                .end(fixedNow.plusDays(11))
                .build());

        assertThat(bookingService.getAllByBooker(booker.getId(), BookingState.PAST)).hasSize(1);
        assertThat(bookingService.getAllByBooker(booker.getId(), BookingState.CURRENT)).hasSize(1);
        assertThat(bookingService.getAllByBooker(booker.getId(), BookingState.FUTURE)).hasSize(3);
        assertThat(bookingService.getAllByBooker(booker.getId(), BookingState.WAITING)).hasSize(4);
        assertThat(bookingService.getAllByBooker(booker.getId(), BookingState.REJECTED)).hasSize(1);
        assertThat(bookingService.getAllByBooker(booker.getId(), BookingState.ALL)).hasSize(5);
    }

    // getAllByOwner — все состояния
    @Test
    void getAllByOwner_shouldFilterByAllStatesCorrectly() {
        UserDto owner = userService.addUser(UserDto.builder()
                .name("Владелец")
                .email("o@test.ru")
                .build());

        UserDto booker = userService.addUser(UserDto.builder()
                .name("Букер")
                .email("b@test.ru")
                .build());

        ItemDto item = itemService.addItem(ItemDto.builder()
                .name("Вещь")
                .description("Тест")
                .available(true)
                .build(), owner.getId());

        bookingService.create(booker.getId(), BookingRequestDto.builder()
                        .itemId(item.getId())
                        .start(now.minusDays(5))
                        .end(now.minusDays(3))
                        .build());

        bookingService.create(booker.getId(), BookingRequestDto.builder()
                        .itemId(item.getId())
                        .start(now.minusHours(1))
                        .end(now.plusHours(1))
                        .build());

        bookingService.create(booker.getId(), BookingRequestDto.builder()
                .itemId(item.getId())
                .start(now.plusDays(1))
                .end(now.plusDays(2))
                .build());

        assertThat(bookingService.getAllByOwner(owner.getId(), BookingState.PAST)).hasSize(1);
        assertThat(bookingService.getAllByOwner(owner.getId(), BookingState.CURRENT)).hasSize(1);
        assertThat(bookingService.getAllByOwner(owner.getId(), BookingState.FUTURE)).hasSize(1);
    }

    // getAllByOwner — состояние REJECTED
    @Test
    void getAllByOwner_withStateREJECTED_shouldReturnOnlyRejected() {
        UserDto owner = userService.addUser(UserDto.builder()
                .name("Владелец")
                .email("o@test.ru")
                .build());

        UserDto booker = userService.addUser(UserDto.builder()
                .name("Букер")
                .email("b@test.ru")
                .build());

        ItemDto item = itemService.addItem(ItemDto.builder()
                .name("Вещь")
                .description("Тест")
                .available(true).build(), owner.getId());

        BookingResponseDto booking = bookingService.create(booker.getId(),
                BookingRequestDto.builder()
                        .itemId(item.getId())
                        .start(now.plusDays(1))
                        .end(now.plusDays(2))
                        .build());

        bookingService.approve(owner.getId(), booking.getId(), false);

        List<BookingResponseDto> rejected = bookingService.getAllByOwner(owner.getId(), BookingState.REJECTED);

        assertThat(rejected).hasSize(1);
        assertThat(rejected.getFirst().getStatus()).isEqualTo(BookingStatus.REJECTED);
    }

    // Создание бронирования — пользователь не существует
    @Test
    void create_shouldThrowNotFoundException_whenUserNotExists() {
        UserDto owner = userService.addUser(UserDto.builder()
                .name("Владелец")
                .email("o@test.ru")
                .build());

        ItemDto item = itemService.addItem(ItemDto.builder()
                .name("Вещь")
                .description("Тест")
                .available(true)
                .build(), owner.getId());

        BookingRequestDto requestDto = BookingRequestDto.builder()
                .itemId(item.getId())
                .start(now.plusDays(1))
                .end(now.plusDays(2))
                .build();

        Long nonExistentUserId = 999L;

        assertThatThrownBy(() -> bookingService.create(nonExistentUserId, requestDto))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Пользователь не найден");
    }

    // Создание бронирования — вещь не существует
    @Test
    void create_shouldThrowNotFoundException_whenItemNotExists() {
        UserDto booker = userService.addUser(UserDto.builder()
                .name("Букер")
                .email("b@test.ru")
                .build());

        BookingRequestDto requestDto = BookingRequestDto.builder()
                .itemId(999L)
                .start(now.plusDays(1))
                .end(now.plusDays(2))
                .build();

        assertThatThrownBy(() -> bookingService.create(booker.getId(), requestDto))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Вещь не найдена");
    }

    // Одобрение бронирования — бронирование не существует
    @Test
    void approve_shouldThrowNotFoundException_whenBookingNotExists() {
        UserDto owner = userService.addUser(UserDto.builder()
                .name("Владелец")
                .email("o@test.ru")
                .build());

        assertThatThrownBy(() -> bookingService.approve(owner.getId(), 999L, true))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Бронирование не найдено");
    }

    // Получение бронирования по ID — бронирование не существует
    @Test
    void getById_shouldThrowNotFoundException_whenBookingNotExists() {
        UserDto owner = userService.addUser(UserDto
                .builder()
                .name("Владелец")
                .email("o@test.ru")
                .build());

        assertThatThrownBy(() -> bookingService.getById(owner.getId(), 999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Бронирование не найдено");
    }

    // getAllByBooker — пользователь не существует
    @Test
    void getAllByBooker_shouldThrowNotFoundException_whenUserNotExists() {
        assertThatThrownBy(() -> bookingService.getAllByBooker(999L, BookingState.ALL))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Пользователь не найден");
    }

    // getAllByOwner — пользователь не существует
    @Test
    void getAllByOwner_shouldThrowNotFoundException_whenUserNotExists() {
        assertThatThrownBy(() -> bookingService.getAllByOwner(999L, BookingState.ALL))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Пользователь не найден");
    }
}