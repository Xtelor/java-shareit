package ru.practicum.shareit.request.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;
import java.util.List;
import static ru.practicum.shareit.HttpHeaders.USER_ID;

/**
 * TODO Sprint add-item-requests.
 */
@RestController
@RequestMapping("/requests")
@RequiredArgsConstructor
public class ItemRequestController {
    private final ItemRequestService service;

    @PostMapping
    public ItemRequestDto createRequest(@RequestHeader(USER_ID) Long userId,
                                        @RequestBody ItemRequestCreateDto dto) {
        return service.create(userId, dto);
    }

    @GetMapping
    public List<ItemRequestDto> getOwnRequests(@RequestHeader(USER_ID) Long userId) {
        return service.getOwn(userId);
    }

    @GetMapping("/all")
    public List<ItemRequestDto> getAllRequests(@RequestHeader(USER_ID) Long userId,
                                               @RequestParam int from,
                                               @RequestParam int size) {
        return service.getAll(userId, from, size);
    }

    @GetMapping("/{requestId}")
    public ItemRequestDto getRequestById(@RequestHeader(USER_ID) Long userId,
                                         @PathVariable Long requestId) {
        return service.getById(userId, requestId);
    }
}
