package ru.practicum.shareit.booking;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.practicum.shareit.booking.dto.BookItemRequestDto;
import ru.practicum.shareit.booking.dto.BookingState;
import ru.practicum.shareit.client.BaseClient;

@Service
public class BookingClient extends BaseClient {
    private static final String API_PREFIX = "/bookings";

    public BookingClient(RestTemplate rest) {
        super(rest);
    }

    // Бронирования текущего пользователя
    public ResponseEntity<Object> getBookings(long userId,
                                              BookingState state,
                                              Integer from,
                                              Integer size) {
        Map<String, Object> parameters = Map.of(
                "state", state.name(),
                "from", from,
                "size", size
        );

        return get(API_PREFIX + "?state={state}&from={from}&size={size}", userId, parameters);
    }

    public ResponseEntity<Object> bookItem(long userId,
                                           BookItemRequestDto requestDto) {

        return post(API_PREFIX, userId, requestDto);
    }

    public ResponseEntity<Object> getBooking(long userId, Long bookingId) {

        return get(API_PREFIX + "/" + bookingId, userId);
    }

    // Бронирования вещей пользователя(хозяина вещей)
    public ResponseEntity<Object> getBookingsForOwner(long ownerId,
                                                      BookingState state,
                                                      Integer from,
                                                      Integer size) {
        Map<String, Object> parameters = Map.of(
                "state", state.name(),
                "from", from,
                "size", size
        );

        return get(API_PREFIX + "/owner?state={state}&from={from}&size={size}", ownerId, parameters);
    }

    // Подтверждение/отклонение бронирования
    public ResponseEntity<Object> approve(long ownerId,
                                          long bookingId,
                                          boolean approved) {
        Map<String, Object> parameters = Map.of("approved", approved);

        return patch(API_PREFIX + "/" + bookingId + "?approved={approved}", ownerId, parameters, null);
    }
}