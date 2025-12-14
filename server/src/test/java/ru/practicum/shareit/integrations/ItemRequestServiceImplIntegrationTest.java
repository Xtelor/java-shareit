package ru.practicum.shareit.integrations;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.jpa.properties.hibernate.id.new_generator_mappings=false"
})
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class ItemRequestServiceImplIntegrationTest {

    private final ItemRequestService requestService;
    private final UserService userService;
    private final ItemService itemService;

    // Создание запроса на вещь — успешно
    @Test
    void create_shouldCreateRequestSuccessfully() {
        UserDto requester = userService.addUser(UserDto.builder()
                .name("Ищущий")
                .email("request@test.ru")
                .build());

        ItemRequestCreateDto dto = ItemRequestCreateDto.builder()
                .description("Нужна дрель для ремонта")
                .build();

        ItemRequestDto saved = requestService.create(requester.getId(), dto);

        assertThat(saved.getId()).isPositive();
        assertThat(saved.getDescription()).isEqualTo("Нужна дрель для ремонта");
        assertThat(saved.getCreated()).isNotNull();
        assertThat(saved.getItems()).isEmpty();
    }

    // Создание запроса несуществующим пользователем — NotFoundException
    @Test
    void create_shouldThrowNotFoundException_whenUserNotExists() {
        ItemRequestCreateDto dto = ItemRequestCreateDto.builder()
                .description("Нужен молоток")
                .build();

        assertThatThrownBy(() -> requestService.create(999L, dto))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Пользователь");
    }

    // Получение своих запросов — возвращаются только свои
    @Test
    void getOwn_shouldReturnOnlyOwnRequests() {
        UserDto user1 = userService.addUser(UserDto.builder()
                .name("Пользователь 1")
                .email("u1@test.ru")
                .build());

        UserDto user2 = userService.addUser(UserDto.builder()
                .name("Пользователь 2")
                .email("u2@test.ru")
                .build());

        requestService.create(user1.getId(), ItemRequestCreateDto.builder()
                .description("Нужна дрель")
                .build());

        requestService.create(user1.getId(), ItemRequestCreateDto.builder()
                .description("Нужен шуруповёрт")
                .build());

        requestService.create(user2.getId(), ItemRequestCreateDto.builder()
                .description("Нужен перфоратор")
                .build());

        List<ItemRequestDto> ownRequests = requestService.getOwn(user1.getId());

        assertThat(ownRequests).hasSize(2);
        assertThat(ownRequests).extracting(ItemRequestDto::getDescription)
                .containsExactlyInAnyOrder("Нужна дрель", "Нужен шуруповёрт");
    }

    // Получение запросов других пользователей — свои не попадают
    @Test
    void getAll_shouldReturnOthersRequestsExcludingOwn() {
        UserDto user1 = userService.addUser(UserDto.builder()
                .name("Пользователь 1")
                .email("u1@test.ru")
                .build());

        UserDto user2 = userService.addUser(UserDto.builder()
                .name("Пользователь 2")
                .email("u2@test.ru")
                .build());

        UserDto user3 = userService.addUser(UserDto.builder()
                .name("Пользователь 3")
                .email("u3@test.ru")
                .build());

        requestService.create(user2.getId(), ItemRequestCreateDto.builder()
                .description("Нужен лобзик")
                .build());

        requestService.create(user3.getId(), ItemRequestCreateDto.builder()
                .description("Нужна болгарка")
                .build());

        requestService.create(user1.getId(), ItemRequestCreateDto.builder()
                .description("Мой запрос — не должен попасть")
                .build());

        List<ItemRequestDto> othersRequests = requestService.getAll(user1.getId(), 0, 10);

        assertThat(othersRequests).hasSize(2);
        assertThat(othersRequests).extracting(ItemRequestDto::getDescription)
                .containsExactlyInAnyOrder("Нужен лобзик", "Нужна болгарка");
    }

    // Получение запроса по ID — возвращается с вещами, если они есть
    @Test
    void getById_shouldReturnRequestWithItems() {
        UserDto requester = userService.addUser(UserDto.builder()
                .name("Ищущий")
                .email("request@test.ru")
                .build());

        UserDto owner = userService.addUser(UserDto.builder()
                .name("Владелец")
                .email("owner@test.ru")
                .build());

        ItemRequestDto requestDto = requestService.create(requester.getId(), ItemRequestCreateDto.builder()
                .description("Нужна дрель")
                .build());

        // Добавляем вещь по запросу
        itemService.addItem(ItemDto.builder()
                .name("Дрель Makita")
                .description("Отличная")
                .available(true)
                .requestId(requestDto.getId())
                .build(), owner.getId());

        ItemRequestDto found = requestService.getById(requester.getId(), requestDto.getId());

        assertThat(found.getId()).isEqualTo(requestDto.getId());
        assertThat(found.getItems()).hasSize(1);
        assertThat(found.getItems().getFirst().getName()).isEqualTo("Дрель Makita");
        assertThat(found.getItems().getFirst().getOwnerId()).isEqualTo(owner.getId());
    }

    // Получение несуществующего запроса — NotFoundException
    @Test
    void getById_shouldThrowNotFoundException_whenRequestNotExists() {
        UserDto user = userService.addUser(UserDto.builder()
                .name("Пользователь")
                .email("u@test.ru")
                .build());

        assertThatThrownBy(() -> requestService.getById(user.getId(), 999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Запрос");
    }

    // Получение чужого запроса — доступно всем
    @Test
    void getById_shouldAllowAccessToAnyRequest() {
        UserDto requester = userService.addUser(UserDto.builder()
                .name("Ищущий")
                .email("request@test.ru")
                .build());

        UserDto stranger = userService.addUser(UserDto.builder()
                .name("Чужой")
                .email("stranger@test.ru")
                .build());

        ItemRequestDto requestDto = requestService.create(requester.getId(), ItemRequestCreateDto.builder()
                .description("Нужна пила")
                .build());

        ItemRequestDto found = requestService.getById(stranger.getId(), requestDto.getId());

        assertThat(found.getId()).isEqualTo(requestDto.getId());
        assertThat(found.getDescription()).isEqualTo("Нужна пила");
    }

    // Получение своих запросов несуществующим пользователем — NotFoundException
    @Test
    void getOwn_shouldThrowNotFoundException_whenUserNotExists() {
        assertThatThrownBy(() -> requestService.getOwn(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Пользователь 999 не найден");
    }

    // Получение чужих запросов несуществующим пользователем — NotFoundException
    @Test
    void getAll_shouldThrowNotFoundException_whenUserNotExists() {
        assertThatThrownBy(() -> requestService.getAll(999L, 0, 10))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Пользователь 999 не найден");
    }

    // Получение запроса по ID несуществующим пользователем — NotFoundException
    @Test
    void getById_shouldThrowNotFoundException_whenUserNotExists() {
        UserDto requester = userService.addUser(UserDto.builder()
                .name("Ищущий")
                .email("req@test.ru")
                .build());

        ItemRequestDto request = requestService.create(requester.getId(), ItemRequestCreateDto.builder()
                .description("Нужна пила")
                .build());

        assertThatThrownBy(() -> requestService.getById(999L, request.getId()))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Пользователь 999 не найден");
    }

    // Пагинация: from больше, чем есть элементов — пустой список
    @Test
    void getAll_shouldReturnEmptyList_whenFromIsTooLarge() {
        UserDto user = userService.addUser(UserDto.builder()
                .name("Я")
                .email("me@test.ru")
                .build());

        UserDto other = userService.addUser(UserDto.builder()
                .name("Другой")
                .email("other@test.ru")
                .build());

        requestService.create(other.getId(), ItemRequestCreateDto.builder()
                .description("Один запрос")
                .build());

        List<ItemRequestDto> result = requestService.getAll(user.getId(), 100, 10);

        assertThat(result).isEmpty();
    }
}