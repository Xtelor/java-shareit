package ru.practicum.shareit.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class BookingRepositoryTest {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    private final LocalDateTime now = LocalDateTime.now();
    private final Sort sortByStartDesc = Sort.by(Sort.Direction.DESC, "startDate");

    // Все бронирования арендатора — сортировка по дате начала (убывание)
    @Test
    void findByBookerId_returnsAllBookings_sortedByStartDesc() {
        User booker = userRepository.save(User.builder()
                .name("Букер")
                .email("booker@test.ru")
                .build());

        User owner = userRepository.save(User.builder()
                .name("Владелец")
                .email("owner@test.ru")
                .build());

        Item item = itemRepository.save(Item.builder()
                .name("Дрель")
                .description("Профессиональная")
                .available(true)
                .owner(owner)
                .build());

        Booking future = bookingRepository.save(Booking.builder()
                .startDate(now.plusDays(10))
                .endDate(now.plusDays(12))
                .item(item)
                .booker(booker)
                .status(BookingStatus.WAITING)
                .build());

        Booking near = bookingRepository.save(Booking.builder()
                .startDate(now.plusDays(1))
                .endDate(now.plusDays(3))
                .item(item)
                .booker(booker)
                .status(BookingStatus.APPROVED)
                .build());

        List<Booking> result = bookingRepository.findByBookerId(booker.getId(), sortByStartDesc);

        assertThat(result).hasSize(2);
        assertThat(result.get(0)).isEqualTo(future);
        assertThat(result.get(1)).isEqualTo(near);
    }

    // Текущие бронирования арендатора
    @Test
    void findCurrentByBookerId_returnsOnlyCurrentBookings() {
        User booker = userRepository.save(User.builder()
                .name("Арендатор")
                .email("rent@test.ru")
                .build());

        User owner = userRepository.save(User.builder()
                .name("Владелец")
                .email("own@test.ru")
                .build());

        Item item = itemRepository.save(Item.builder()
                .name("Вещь")
                .description("Тест")
                .available(true)
                .owner(owner)
                .build());

        bookingRepository.save(Booking.builder()
                .startDate(now.minusDays(5))
                .endDate(now.minusDays(1))
                .item(item)
                .booker(booker)
                .status(BookingStatus.APPROVED)
                .build());

        Booking current = bookingRepository.save(Booking.builder()
                .startDate(now.minusHours(2))
                .endDate(now.plusHours(3))
                .item(item)
                .booker(booker)
                .status(BookingStatus.APPROVED)
                .build());

        List<Booking> result = bookingRepository
                .findCurrentByBookerId(booker.getId(), now, sortByStartDesc);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst()).isEqualTo(current);
    }

    // Прошлые бронирования арендатора
    @Test
    void findByBookerIdAndEndDateIsBefore_returnsOnlyPastBookings() {
        User booker = userRepository.save(User.builder()
                .name("Букер")
                .email("past@test.ru")
                .build());

        User owner = userRepository.save(User.builder()
                .name("Владелец")
                .email("o@test.ru")
                .build());

        Item item = itemRepository.save(Item.builder()
                .name("Пила")
                .description("Циркулярная")
                .available(true)
                .owner(owner)
                .build());

        Booking past1 = bookingRepository.save(Booking.builder()
                .startDate(now.minusDays(10))
                .endDate(now.minusDays(8))
                .item(item)
                .booker(booker)
                .status(BookingStatus.APPROVED)
                .build());

        Booking past2 = bookingRepository.save(Booking.builder()
                .startDate(now.minusDays(5))
                .endDate(now.minusDays(2))
                .item(item)
                .booker(booker)
                .status(BookingStatus.REJECTED)
                .build());

        List<Booking> result = bookingRepository
                .findByBookerIdAndEndDateIsBefore(booker.getId(), now, sortByStartDesc);

        assertThat(result).hasSize(2);
        assertThat(result.get(0)).isEqualTo(past2);
        assertThat(result.get(1)).isEqualTo(past1);
    }

    // Все бронирования по владельцу вещи
    @Test
    void findByItemOwnerId_returnsBookingsForOwnerItems() {
        User owner = userRepository.save(User.builder()
                .name("Владелец")
                .email("owner2@test.ru")
                .build());

        User booker = userRepository.save(User.builder()
                .name("Букер")
                .email("b@test.ru")
                .build());

        Item item = itemRepository.save(Item.builder()
                .name("Молоток")
                .description("Тяжёлый")
                .available(true)
                .owner(owner)
                .build());

        Booking b1 = bookingRepository.save(Booking.builder()
                .startDate(now.plusDays(5))
                .endDate(now.plusDays(7))
                .item(item)
                .booker(booker)
                .status(BookingStatus.APPROVED)
                .build());

        Booking b2 = bookingRepository.save(Booking.builder()
                .startDate(now.plusDays(1))
                .endDate(now.plusDays(2))
                .item(item)
                .booker(booker)
                .status(BookingStatus.WAITING)
                .build());

        List<Booking> result = bookingRepository.findByItemOwnerId(owner.getId(), sortByStartDesc);

        assertThat(result).hasSize(2);
        assertThat(result.get(0)).isEqualTo(b1);
        assertThat(result.get(1)).isEqualTo(b2);
    }

    // Последнее одобренное бронирование вещи (до текущего момента)
    @Test
    void findFirstByItemIdAndStartDateLessThanEqualAndStatus_returnsLastApprovedBooking() {
        User owner = userRepository.save(User.builder()
                .name("Владелец")
                .email("o3@test.ru")
                .build());

        User booker = userRepository.save(User.builder()
                .name("Букер")
                .email("b3@test.ru")
                .build());

        Item item = itemRepository.save(Item.builder()
                .name("Лестница")
                .description("5м")
                .available(true)
                .owner(owner)
                .build());

        bookingRepository.save(Booking.builder()
                .startDate(now.minusDays(20))
                .endDate(now.minusDays(15))
                .item(item)
                .booker(booker)
                .status(BookingStatus.REJECTED)
                .build());

        Booking lastApproved = bookingRepository.save(Booking.builder()
                .startDate(now.minusDays(10))
                .endDate(now.minusDays(5))
                .item(item)
                .booker(booker)
                .status(BookingStatus.APPROVED)
                .build());

        var result = bookingRepository
                .findFirstByItemIdAndStartDateLessThanEqualAndStatus(item.getId(),
                        now,
                        BookingStatus.APPROVED,
                        Sort.by(Sort.Direction.DESC, "startDate"));

        assertThat(result).contains(lastApproved);
    }

    // Проверка: было ли одобренное бронирование у пользователя на эту вещь
    @Test
    void existsByBookerIdAndItemIdAndStatus_returnsTrue_whenApprovedBookingExists() {
        User owner = userRepository.save(User.builder()
                .name("Владелец")
                .email("o4@test.ru")
                .build());

        User booker = userRepository.save(User.builder()
                .name("Букер")
                .email("b4@test.ru")
                .build());

        Item item = itemRepository.save(Item.builder()
                .name("Вещь")
                .description("Тест")
                .available(true)
                .owner(owner)
                .build());

        bookingRepository.save(Booking.builder()
                .startDate(now.minusDays(10))
                .endDate(now.minusDays(5))
                .item(item)
                .booker(booker)
                .status(BookingStatus.APPROVED)
                .build());

        boolean exists = bookingRepository.existsByBookerIdAndItemIdAndStatusAndEndDateBefore(
                booker.getId(), item.getId(), BookingStatus.APPROVED, LocalDateTime.now());

        assertThat(exists).isTrue();
    }
}