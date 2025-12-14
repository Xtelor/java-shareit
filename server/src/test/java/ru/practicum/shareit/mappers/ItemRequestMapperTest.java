package ru.practicum.shareit.mappers;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequestMapper;
import ru.practicum.shareit.request.dto.ItemForRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ItemRequestMapperTest {

    @Test
    void mapToEntity_shouldMapFieldsCorrectly() {
        ItemRequestCreateDto dto = ItemRequestCreateDto.builder()
                .description("Нужна дрель")
                .build();

        User requester = new User();
        requester.setId(1L);
        requester.setName("Запрашивающий");

        ItemRequest request = ItemRequestMapper.mapToEntity(dto, requester);

        assertThat(request.getDescription()).isEqualTo("Нужна дрель");
        assertThat(request.getRequester()).isEqualTo(requester);
    }

    @Test
    void mapToDto_shouldMapRequestAndItemsCorrectly() {
        User owner = new User();
        owner.setId(10L);
        owner.setName("Владелец");

        Item item1 = new Item();
        item1.setId(100L);
        item1.setName("Дрель");
        item1.setOwner(owner);

        Item item2 = new Item();
        item2.setId(200L);
        item2.setName("Отвертка");
        item2.setOwner(owner);

        LocalDateTime created = LocalDateTime.now();

        ItemRequest request = new ItemRequest();
        request.setId(5L);
        request.setDescription("Нужен инструмент");
        request.setCreated(created);

        List<Item> items = List.of(item1, item2);

        ItemRequestDto dto = ItemRequestMapper.mapToDto(request, items);

        assertThat(dto.getId()).isEqualTo(5L);
        assertThat(dto.getDescription()).isEqualTo("Нужен инструмент");
        assertThat(dto.getCreated()).isEqualTo(created);
        assertThat(dto.getItems()).hasSize(2);

        ItemForRequestDto dto1 = dto.getItems().get(0);
        ItemForRequestDto dto2 = dto.getItems().get(1);

        assertThat(dto1.getId()).isEqualTo(100L);
        assertThat(dto1.getName()).isEqualTo("Дрель");
        assertThat(dto1.getOwnerId()).isEqualTo(10L);

        assertThat(dto2.getId()).isEqualTo(200L);
        assertThat(dto2.getName()).isEqualTo("Отвертка");
        assertThat(dto2.getOwnerId()).isEqualTo(10L);
    }

    @Test
    void mapToItemForRequestDto_shouldMapFieldsCorrectly() {
        User owner = new User();
        owner.setId(3L);

        Item item = new Item();
        item.setId(7L);
        item.setName("Пила");
        item.setOwner(owner);

        ItemForRequestDto dto = ItemRequestMapper.mapToItemForRequestDto(item);

        assertThat(dto.getId()).isEqualTo(7L);
        assertThat(dto.getName()).isEqualTo("Пила");
        assertThat(dto.getOwnerId()).isEqualTo(3L);
    }
}