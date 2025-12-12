package ru.practicum.shareit.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ItemRequestRepositoryTest {

    @Autowired
    private ItemRequestRepository requestRepository;

    @Autowired
    private UserRepository userRepository;

    private final LocalDateTime now = LocalDateTime.now();

    // Запросы конкретного пользователя — сортировка по дате создания (убывание)
    @Test
    void findAllByRequesterIdOrderByCreatedDesc_returnsOnlyThisUserRequests_sortedByCreatedDesc() {
        User user = userRepository.save(User.builder()
                .name("user")
                .email("user@test.ru")
                .build());

        // Старый запрос
        requestRepository.save(ItemRequest.builder()
                .description("Нужна дрель")
                .requester(user)
                .created(now.minusDays(3))
                .build());

        // Новый запрос
        requestRepository.save(ItemRequest.builder()
                .description("Нужен шуруповёрт")
                .requester(user)
                .created(now.minusHours(2))
                .build());

        // Ещё новее
        requestRepository.save(ItemRequest.builder()
                .description("Нужна пила")
                .requester(user)
                .created(now.minusHours(1))
                .build());

        List<ItemRequest> requests = requestRepository
                .findAllByRequesterIdOrderByCreatedDesc(user.getId());

        assertThat(requests).hasSize(3);
        assertThat(requests.getFirst().getDescription()).isEqualTo("Нужна пила");
        assertThat(requests.get(1).getDescription()).isEqualTo("Нужен шуруповёрт");
        assertThat(requests.get(2).getDescription()).isEqualTo("Нужна дрель");
    }

    // Запросы всех пользователей, кроме указанного
    @Test
    void findAllByRequesterIdNot_returnsOtherUsersRequests_withPagination() {
        User user1 = userRepository.save(User.builder()
                .name("Иван")
                .email("ivan@test.ru")
                .build());

        User user2 = userRepository.save(User.builder()
                .name("Пётр")
                .email("petya@test.ru")
                .build());

        User user3 = userRepository.save(User.builder()
                .name("Мария")
                .email("maria@test.ru")
                .build());

        requestRepository.save(ItemRequest.builder()
                .description("Нужен молоток")
                .requester(user2)
                .created(now.minusDays(5))
                .build());

        requestRepository.save(ItemRequest.builder()
                .description("Нужна отвёртка")
                .requester(user2)
                .created(now.minusDays(1))
                .build());

        requestRepository.save(ItemRequest.builder()
                .description("Нужна лестница")
                .requester(user3)
                .created(now.minusHours(3))
                .build());

        Pageable pageable = PageRequest.of(0, 10,
                Sort.by(Sort.Direction.DESC, "created"));
        List<ItemRequest> userSees = requestRepository
                .findAllByRequesterIdNot(user1.getId(), pageable).getContent();

        assertThat(userSees).hasSize(3);
        assertThat(userSees.getFirst().getRequester().getName()).isEqualTo("Мария");
        assertThat(userSees.get(1).getRequester().getName()).isEqualTo("Пётр");
        assertThat(userSees.get(2).getRequester().getName()).isEqualTo("Пётр");
    }

    // Пустой результат — если нет чужих запросов
    @Test
    void findAllByRequesterIdNot_returnsEmpty_whenNoOtherRequests() {
        User onlyUser = userRepository.save(User.builder()
                .name("Одинокий")
                .email("alone@test.ru")
                .build());

        requestRepository.save(ItemRequest.builder()
                .description("Нужна дрель")
                .requester(onlyUser)
                .created(now)
                .build());

        Pageable pageable = PageRequest.of(0, 10);
        List<ItemRequest> result = requestRepository
                .findAllByRequesterIdNot(onlyUser.getId(), pageable).getContent();

        assertThat(result).isEmpty();
    }

    // Сохранение запроса — должен генерироваться ID
    @Test
    void shouldSaveRequestAndGenerateId() {
        User user = userRepository.save(User.builder()
                .name("Вася")
                .email("vasya@test.ru")
                .build());

        ItemRequest request = ItemRequest.builder()
                .description("Нужна газонокосилка")
                .requester(user)
                .created(now)
                .build();

        ItemRequest saved = requestRepository.save(request);

        assertThat(saved.getId()).isPositive();
    }
}