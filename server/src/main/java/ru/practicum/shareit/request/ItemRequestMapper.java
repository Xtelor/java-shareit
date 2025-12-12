package ru.practicum.shareit.request;

import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemForRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.stream.Collectors;

public class ItemRequestMapper {

    public static ItemRequest mapToEntity(ItemRequestCreateDto dto, User requester) {
        return ItemRequest.builder()
                .description(dto.getDescription())
                .requester(requester)
                .build();
    }

    public static ItemRequestDto mapToDto(ItemRequest request, List<Item> items) {
        ItemRequestDto dto = new ItemRequestDto();
        dto.setId(request.getId());
        dto.setDescription(request.getDescription());
        dto.setCreated(request.getCreated());
        dto.setItems(items.stream()
                .map(ItemRequestMapper::mapToItemForRequestDto)
                .collect(Collectors.toList()));
        return dto;
    }

    public static ItemForRequestDto mapToItemForRequestDto(Item item) {
        ItemForRequestDto dto = new ItemForRequestDto();
        dto.setId(item.getId());
        dto.setName(item.getName());
        dto.setOwnerId(item.getOwner().getId());
        return dto;
    }
}
