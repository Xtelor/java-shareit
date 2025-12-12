package ru.practicum.shareit.booking.controller;

import jakarta.validation.Valid;
import jakarta.validation.ValidationException;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.BookingClient;
import ru.practicum.shareit.booking.dto.BookItemRequestDto;
import ru.practicum.shareit.booking.dto.BookingState;

import java.util.Map;

import static ru.practicum.shareit.Headers.USER_ID;

@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
@Validated
public class BookingController {

    private final BookingClient bookingClient;

    @PostMapping
    public ResponseEntity<Object> bookItem(@RequestHeader(USER_ID) @Positive Long userId,
                                           @RequestBody @Valid BookItemRequestDto requestDto) {

        if (requestDto.getStart() != null && requestDto.getEnd() != null
                && !requestDto.getEnd().isAfter(requestDto.getStart())) {
            throw new ValidationException("Время окончания бронирования должно быть после начала.");
        }

        return bookingClient.bookItem(userId, requestDto);
    }

    @PatchMapping("/{bookingId}")
    public ResponseEntity<Object> approve(@RequestHeader(USER_ID) @Positive Long ownerId,
                                          @PathVariable @Positive Long bookingId,
                                          @RequestParam Boolean approved) {
        return bookingClient.approve(ownerId, bookingId, approved);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<Object> getBooking(@RequestHeader(USER_ID) @Positive Long userId,
                                             @PathVariable @Positive Long bookingId) {
        return bookingClient.getBooking(userId, bookingId);
    }

    @GetMapping
    public ResponseEntity<Object> getBookings(@RequestHeader(USER_ID) @Positive Long userId,
                                              @RequestParam(defaultValue = "ALL") String state,
                                              @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
                                              @RequestParam(defaultValue = "10") @Positive Integer size) {

        BookingState bookingState = BookingState.from(state)
                .orElseThrow(() -> new ValidationException("Неизвестный статус: " + state));

        return bookingClient.getBookings(userId, bookingState, from, size);
    }

    @GetMapping("/owner")
    public ResponseEntity<Object> getBookingsForOwner(@RequestHeader(USER_ID) Long ownerId,
                                                      @RequestParam(defaultValue = "ALL") String state,
                                                      @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
                                                      @RequestParam(defaultValue = "10") @Positive Integer size) {

        BookingState bookingState = BookingState.from(state)
                .orElseThrow(() -> new ValidationException("Неизвестный статус: " + state));

        return bookingClient.getBookingsForOwner(ownerId, bookingState, from, size);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<Map<String, String>> handleValidationException(ValidationException e) {
        return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }
}