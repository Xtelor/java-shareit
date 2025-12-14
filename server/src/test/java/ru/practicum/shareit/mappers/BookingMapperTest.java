package ru.practicum.shareit.mappers;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.booking.BookingMapper;
import ru.practicum.shareit.booking.dto.BookingItemDto;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BookingMapperTest {

    @Test
    void toBooking_shouldReturnNull_whenDtoIsNull() {
        Item item = new Item();
        item.setId(1L);

        User booker = new User();
        booker.setId(2L);

        Booking result = BookingMapper.toBooking(null, item, booker);

        assertThat(result).isNull();
    }

    @Test
    void toBooking_shouldThrowException_whenItemIsNull() {
        User booker = new User();
        booker.setId(2L);

        BookingRequestDto dto = BookingRequestDto.builder()
                .itemId(1L)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();

        assertThatThrownBy(() -> BookingMapper.toBooking(dto, null, booker))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Item не может быть null");
    }

    @Test
    void toBooking_shouldThrowException_whenBookerIsNull() {
        Item item = new Item();
        item.setId(1L);

        BookingRequestDto dto = BookingRequestDto.builder()
                .itemId(1L)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();

        assertThatThrownBy(() -> BookingMapper.toBooking(dto, item, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Booker не может быть null");
    }

    @Test
    void toBooking_shouldMapFieldsCorrectly() {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(2);

        BookingRequestDto dto = BookingRequestDto.builder()
                .itemId(10L)
                .start(start)
                .end(end)
                .build();

        Item item = new Item();
        item.setId(10L);
        item.setName("Вещь");

        User booker = new User();
        booker.setId(5L);
        booker.setName("Букер");

        Booking booking = BookingMapper.toBooking(dto, item, booker);

        assertThat(booking).isNotNull();
        assertThat(booking.getStartDate()).isEqualTo(start);
        assertThat(booking.getEndDate()).isEqualTo(end);
        assertThat(booking.getItem()).isEqualTo(item);
        assertThat(booking.getBooker()).isEqualTo(booker);
    }

    @Test
    void toBookingResponseDto_shouldReturnNull_whenBookingIsNull() {
        BookingResponseDto result = BookingMapper.toBookingResponseDto(null);
        assertThat(result).isNull();
    }

    @Test
    void toBookingResponseDto_shouldMapFieldsCorrectly() {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(2);

        User booker = new User();
        booker.setId(5L);
        booker.setName("Букер");

        Item item = new Item();
        item.setId(10L);
        item.setName("Вещь");

        Booking booking = new Booking();
        booking.setId(100L);
        booking.setStartDate(start);
        booking.setEndDate(end);
        booking.setBooker(booker);
        booking.setItem(item);
        booking.setStatus(BookingStatus.APPROVED);

        BookingResponseDto dto = BookingMapper.toBookingResponseDto(booking);

        assertThat(dto.getId()).isEqualTo(100L);
        assertThat(dto.getStart()).isEqualTo(start);
        assertThat(dto.getEnd()).isEqualTo(end);
        assertThat(dto.getStatus()).isEqualTo(BookingStatus.APPROVED);
        assertThat(dto.getBooker().getId()).isEqualTo(5L);
        assertThat(dto.getBooker().getName()).isEqualTo("Букер");
        assertThat(dto.getItem().getId()).isEqualTo(10L);
        assertThat(dto.getItem().getName()).isEqualTo("Вещь");
    }

    @Test
    void toBookingItemDto_shouldReturnNull_whenBookingIsNull() {
        BookingItemDto dto = BookingMapper.toBookingItemDto(null);
        assertThat(dto).isNull();
    }

    @Test
    void toBookingItemDto_shouldMapFieldsCorrectly() {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(2);

        User booker = new User();
        booker.setId(7L);

        Booking booking = new Booking();
        booking.setId(200L);
        booking.setStartDate(start);
        booking.setEndDate(end);
        booking.setBooker(booker);

        BookingItemDto dto = BookingMapper.toBookingItemDto(booking);

        assertThat(dto.getId()).isEqualTo(200L);
        assertThat(dto.getBookerId()).isEqualTo(7L);
        assertThat(dto.getStart()).isEqualTo(start);
        assertThat(dto.getEnd()).isEqualTo(end);
    }
}