package ru.practicum.shareit.mappers;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.ItemRequest;

import static org.assertj.core.api.Assertions.assertThat;

class ItemMapperTest {

    @Test
    void mapToItemDto_shouldReturnNull_whenItemIsNull() {
        assertThat(ItemMapper.mapToItemDto(null)).isNull();
    }

    @Test
    void mapToItemDto_shouldMapFieldsWithoutRequest() {
        Item item = Item.builder()
                .id(1L)
                .name("Дрель")
                .description("Аккумуляторная")
                .available(true)
                .build();

        ItemDto dto = ItemMapper.mapToItemDto(item);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getName()).isEqualTo("Дрель");
        assertThat(dto.getDescription()).isEqualTo("Аккумуляторная");
        assertThat(dto.getAvailable()).isTrue();
        assertThat(dto.getRequestId()).isNull();
        assertThat(dto.getLastBooking()).isNull();
        assertThat(dto.getNextBooking()).isNull();
        assertThat(dto.getComments()).isNotNull().isEmpty();
    }

    @Test
    void mapToItemDto_shouldSetRequestId_whenRequestNotNull() {
        ItemRequest request = new ItemRequest();
        request.setId(10L);

        Item item = Item.builder()
                .id(2L)
                .name("Отвертка")
                .description("Крестовая")
                .available(false)
                .request(request)
                .build();

        ItemDto dto = ItemMapper.mapToItemDto(item);

        assertThat(dto.getRequestId()).isEqualTo(10L);
    }

    @Test
    void mapToItem_shouldReturnNull_whenDtoIsNull() {
        assertThat(ItemMapper.mapToItem(null, null)).isNull();
    }

    @Test
    void mapToItem_shouldMapFieldsCorrectly() {
        ItemRequest request = new ItemRequest();
        request.setId(5L);

        ItemDto dto = ItemDto.builder()
                .id(3L)
                .name("Пила")
                .description("Цепная")
                .available(true)
                .requestId(5L)
                .build();

        Item item = ItemMapper.mapToItem(dto, request);

        assertThat(item.getId()).isEqualTo(3L);
        assertThat(item.getName()).isEqualTo("Пила");
        assertThat(item.getDescription()).isEqualTo("Цепная");
        assertThat(item.getAvailable()).isTrue();
        assertThat(item.getRequest()).isEqualTo(request);
    }

    @Test
    void updateItemFromDto_shouldUpdateAllNonNullFields() {
        Item item = Item.builder()
                .id(1L)
                .name("Старое имя")
                .description("Старое описание")
                .available(true)
                .build();

        ItemDto dto = ItemDto.builder()
                .name("Новое имя")
                .description("Новое описание")
                .available(false)
                .build();

        ItemMapper.updateItemFromDto(dto, item);

        assertThat(item.getName()).isEqualTo("Новое имя");
        assertThat(item.getDescription()).isEqualTo("Новое описание");
        assertThat(item.getAvailable()).isFalse();
    }

    @Test
    void updateItemFromDto_shouldNotUpdateWhenValuesNullOrBlank() {
        Item item = Item.builder()
                .id(1L)
                .name("Имя")
                .description("Описание")
                .available(true)
                .build();

        ItemDto dto = ItemDto.builder()
                .name("   ")
                .description(null)
                .available(null)
                .build();

        ItemMapper.updateItemFromDto(dto, item);

        assertThat(item.getName()).isEqualTo("Имя");
        assertThat(item.getDescription()).isEqualTo("Описание");
        assertThat(item.getAvailable()).isTrue();
    }
}