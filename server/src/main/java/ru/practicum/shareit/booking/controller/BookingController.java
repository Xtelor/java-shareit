package ru.practicum.shareit.booking.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.service.BookingService;
import java.util.List;
import static ru.practicum.shareit.HttpHeaders.USER_ID;

@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
public class BookingController {
    private final BookingService bookingService;

    @PostMapping
    public BookingResponseDto create(@RequestHeader(USER_ID) Long userId,
                                     @RequestBody BookingRequestDto bookingRequestDto) {
        return bookingService.create(userId, bookingRequestDto);
    }

    @PatchMapping("/{bookingId}")
    public BookingResponseDto approve(@RequestHeader(USER_ID) Long userId,
                                      @PathVariable Long bookingId,
                                      @RequestParam Boolean approved) {
        return bookingService.approve(userId, bookingId, approved);
    }

    @GetMapping("/{bookingId}")
    public BookingResponseDto getById(@RequestHeader(USER_ID) Long userId,
                                      @PathVariable Long bookingId) {
        return bookingService.getById(userId, bookingId);
    }

    @GetMapping
    public List<BookingResponseDto> getAllByBooker(@RequestHeader(USER_ID) Long userId,
                                                   @RequestParam(defaultValue = "ALL") BookingState state) {
        return bookingService.getAllByBooker(userId, state);
    }

    @GetMapping("/owner")
    public List<BookingResponseDto> getAllByOwner(@RequestHeader(USER_ID) Long userId,
                                                  @RequestParam(defaultValue = "ALL") BookingState state) {
        return bookingService.getAllByOwner(userId, state);
    }
}