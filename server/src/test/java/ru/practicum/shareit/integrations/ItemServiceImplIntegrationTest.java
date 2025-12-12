package ru.practicum.shareit.integrations;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exceptions.ForbiddenException;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.item.dto.CommentRequestDto;
import ru.practicum.shareit.item.dto.CommentResponseDto;
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
class ItemServiceImplIntegrationTest {

    private final ItemService itemService;
    private final UserService userService;
    private final BookingService bookingService;

    // Добавление вещи — успешно
    @Test
    void addItem_shouldCreateItemSuccessfully() {
        UserDto owner = userService.addUser(UserDto.builder()
                .name("Владелец")
                .email("owner@test.ru")
                .build());

        ItemDto dto = ItemDto.builder()
                .name("Дрель")
                .description("Аккумуляторная")
                .available(true)
                .build();

        ItemDto saved = itemService.addItem(dto, owner.getId());

        assertThat(saved.getId()).isPositive();
        assertThat(saved.getName()).isEqualTo("Дрель");
        assertThat(saved.getDescription()).isEqualTo("Аккумуляторная");
        assertThat(saved.getAvailable()).isTrue();
    }

    // Добавление вещи несуществующим пользователем — NotFoundException
    @Test
    void addItem_shouldThrowNotFoundException_whenOwnerNotExists() {
        ItemDto dto = ItemDto.builder()
                .name("Вещь")
                .description("Тест")
                .available(true)
                .build();

        assertThatThrownBy(() -> itemService.addItem(dto, 999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Пользователь с id = 999 не найден.");
    }

    // Обновление вещи — обновляются только переданные поля
    @Test
    void updateItem_shouldUpdateOnlyProvidedFields() {
        UserDto owner = userService.addUser(UserDto.builder()
                .name("Владелец")
                .email("owner@test.ru")
                .build());

        ItemDto original = itemService.addItem(ItemDto.builder()
                .name("Старая дрель")
                .description("Старая")
                .available(true)
                .build(), owner.getId());

        ItemDto updateDto = ItemDto.builder()
                .name("Новая дрель")
                .description("Профессиональная")
                .build();

        ItemDto updated = itemService.updateItem(original.getId(), updateDto, owner.getId());

        assertThat(updated.getId()).isEqualTo(original.getId());
        assertThat(updated.getName()).isEqualTo("Новая дрель");
        assertThat(updated.getDescription()).isEqualTo("Профессиональная");
        assertThat(updated.getAvailable()).isTrue();
    }

    // Обновление чужой вещи — NotFoundException
    @Test
    void updateItem_shouldThrowNotFoundException_whenNotOwner() {
        UserDto owner = userService.addUser(UserDto.builder()
                .name("Один")
                .email("1@test.ru")
                .build());

        UserDto stranger = userService.addUser(UserDto.builder()
                .name("Два")
                .email("2@test.ru").build());

        ItemDto item = itemService.addItem(ItemDto.builder()
                .name("Моя вещь")
                .description("Только моя")
                .available(true)
                .build(), owner.getId());

        ItemDto update = ItemDto.builder().name("Попытка захвата").build();

        assertThatThrownBy(() -> itemService.updateItem(item.getId(), update, stranger.getId()))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("Редактировать вещь может только её владелец.");
    }

    // Получение вещи по ID — обычным пользователем
    @Test
    void getItemById_shouldReturnItem() {
        UserDto owner = userService.addUser(UserDto.builder()
                .name("Владелец")
                .email("o@test.ru")
                .build());

        UserDto user = userService.addUser(UserDto.builder()
                .name("Пользователь")
                .email("u@test.ru")
                .build());

        ItemDto item = itemService.addItem(ItemDto.builder()
                .name("Вещь")
                .description("Для теста")
                .available(true)
                .build(), owner.getId());

        ItemDto found = itemService.getItemById(item.getId());

        assertThat(found.getId()).isEqualTo(item.getId());
        assertThat(found.getName()).isEqualTo("Вещь");
    }

    // Получение вещи по ID с бронированиями — владельцем
    @Test
    void getItemByIdWithBookings_shouldReturnItemWithBookingInfo_whenOwner() {
        UserDto owner = userService.addUser(UserDto.builder()
                .name("Владелец")
                .email("o@test.ru")
                .build());

        ItemDto item = itemService.addItem(ItemDto.builder()
                .name("Вещь с бронированием")
                .description("Тест")
                .available(true)
                .build(), owner.getId());

        ItemDto fullItem = itemService.getItemByIdWithBookings(owner.getId(), item.getId());

        assertThat(fullItem.getId()).isEqualTo(item.getId());
        assertThat(fullItem.getComments()).isEmpty();
    }

    // Получение всех вещей владельца
    @Test
    void getItemsByOwner_shouldReturnOnlyOwnerItems() {
        UserDto owner = userService.addUser(UserDto.builder()
                .name("Владелец")
                .email("o@test.ru")
                .build());

        UserDto another = userService.addUser(UserDto.builder()
                .name("Другой")
                .email("a@test.ru")
                .build());

        itemService.addItem(ItemDto.builder()
                .name("Моя 1")
                .description("Первая")
                .available(true)
                .build(), owner.getId());

        itemService.addItem(ItemDto.builder()
                .name("Моя 2")
                .description("Вторая")
                .available(true)
                .build(), owner.getId());

        itemService.addItem(ItemDto.builder()
                .name("Чужая")
                .description("Не моя")
                .available(true)
                .build(), another.getId());

        List<ItemDto> items = itemService.getItemsByOwner(owner.getId());

        assertThat(items).hasSize(2);
        assertThat(items).extracting(ItemDto::getName)
                .containsExactlyInAnyOrder("Моя 1", "Моя 2");
    }

    // Поиск вещей по тексту
    @Test
    void searchForItem_shouldFindItemsByNameOrDescription() {
        UserDto owner = userService.addUser(UserDto.builder()
                .name("Владелец")
                .email("o@test.ru")
                .build());

        itemService.addItem(ItemDto.builder()
                .name("Дрель Bosch")
                .description("Профессиональная")
                .available(true)
                .build(), owner.getId());

        itemService.addItem(ItemDto.builder()
                .name("Молоток")
                .description("дрель в описании")
                .available(true)
                .build(), owner.getId());

        itemService.addItem(ItemDto.builder()
                .name("Пила")
                .description("Циркулярная")
                .available(false).build(), owner.getId());

        List<ItemDto> found = itemService.searchForItem("дрель");

        assertThat(found).hasSize(2);
        assertThat(found).extracting(ItemDto::getName)
                .contains("Дрель Bosch", "Молоток");
    }

    // Поиск с пустым текстом — пустой список
    @Test
    void searchForItem_shouldReturnEmptyList_whenTextIsBlank() {
        List<ItemDto> found = itemService.searchForItem("   ");

        assertThat(found).isEmpty();
    }

    // Добавление комментария к вещи — только после завершённого бронирования
    @Test
    void addComment_shouldCreateCommentSuccessfully() {
        UserDto owner = userService.addUser(UserDto.builder()
                .name("Владелец")
                .email("o@test.ru")
                .build());

        UserDto booker = userService.addUser(UserDto.builder()
                .name("Букер")
                .email("b@test.ru")
                .build());

        ItemDto item = itemService.addItem(ItemDto.builder()
                .name("Вещь для комментария")
                .description("Отличная")
                .available(true)
                .build(), owner.getId());

        // Создаём бронирование в прошлом — оно уже завершено!
        BookingRequestDto bookingDto = BookingRequestDto.builder()
                .itemId(item.getId())
                .start(LocalDateTime.now().minusDays(10))
                .end(LocalDateTime.now().minusDays(5))
                .build();

        BookingResponseDto booking = bookingService.create(booker.getId(), bookingDto);
        bookingService.approve(owner.getId(), booking.getId(), true); // APPROVED

        // Теперь можно оставить комментарий!
        CommentRequestDto commentRequest = CommentRequestDto.builder()
                .text("Отличная вещь, всё понравилось!")
                .build();

        CommentResponseDto comment = itemService.addComment(booker.getId(), item.getId(), commentRequest);

        assertThat(comment.getId()).isPositive();
        assertThat(comment.getText()).isEqualTo("Отличная вещь, всё понравилось!");
        assertThat(comment.getAuthorName()).isEqualTo("Букер");
    }

    // Удаление вещи — она должна исчезнуть
    @Test
    void deleteItem_shouldRemoveItem() {
        UserDto owner = userService.addUser(UserDto.builder()
                .name("Владелец")
                .email("o@test.ru")
                .build());

        ItemDto item = itemService.addItem(ItemDto.builder()
                .name("Удаляемая вещь")
                .description("Пока")
                .available(true)
                .build(), owner.getId());

        itemService.deleteItem(item.getId());

        assertThatThrownBy(() -> itemService.getItemById(item.getId()))
                .isInstanceOf(NotFoundException.class);
    }
}