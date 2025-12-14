package ru.practicum.shareit.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ItemRepositoryTest {

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRequestRepository itemRequestRepository;

    private User owner;
    private User anotherUser;

    @BeforeEach
    void beforeEach() {
        owner = userRepository.save(User.builder()
                .name("Владелец Вещей")
                .email("owner@yandex.ru")
                .build());

        anotherUser = userRepository.save(User.builder()
                .name("Другой Пользователь")
                .email("other@mail.ru")
                .build());
    }

    // Сохранение вещи — должен генерироваться ID
    @Test
    void contextLoads_andSaveItem_generatesId() {
        Item item = Item.builder()
                .name("Молоток")
                .description("Тяжёлый, 1 кг")
                .available(true)
                .owner(owner)
                .build();

        Item saved = itemRepository.save(item);

        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isPositive();
        assertThat(saved.getName()).isEqualTo("Молоток");
        assertThat(saved.getOwner().getId()).isEqualTo(owner.getId());
    }

    // Поиск всех вещей владельца — только его вещи
    @Test
    void findByOwnerId_returnsOnlyOwnersItems() {
        itemRepository.save(Item.builder()
                .name("Дрель")
                .description("Bosch")
                .available(true)
                .owner(owner)
                .build());

        itemRepository.save(Item.builder()
                .name("Пила")
                .description("Makita")
                .available(true)
                .owner(owner)
                .build());

        itemRepository.save(Item.builder()
                .name("Топор")
                .description("Для дров")
                .available(true)
                .owner(anotherUser)
                .build());

        List<Item> items = itemRepository.findByOwnerId(owner.getId());

        assertThat(items).hasSize(2);
        assertThat(items).extracting(Item::getName).containsExactlyInAnyOrder("Дрель", "Пила");
    }

    // Если у владельца нет вещей — возвращаем пустой список
    @Test
    void findByOwnerId_returnsEmptyList_whenNoItems() {
        User emptyUser = userRepository.save(User.builder()
                .name("Бедняк")
                .email("poor@yandex.ru")
                .build());

        List<Item> items = itemRepository.findByOwnerId(emptyUser.getId());

        assertThat(items).isEmpty();
    }

    // Поиск по тексту — ищет и в имени, и в описании, игнорируя регистр
    @Test
    void search_findsItemsByNameOrDescription_caseInsensitive() {
        itemRepository.save(Item.builder()
                .name("Отвёртка")
                .description("Крестовая PH2")
                .available(true)
                .owner(owner)
                .build());

        itemRepository.save(Item.builder()
                .name("Шуруповёрт")
                .description("аккумуляторный BOSCH")
                .available(true)
                .owner(owner)
                .build());

        itemRepository.save(Item.builder()
                .name("Лестница")
                .description("Телескопическая 3.8м")
                .available(true)
                .owner(owner)
                .build());

        List<Item> found1 = itemRepository.search("отвёртка");
        List<Item> found2 = itemRepository.search("bosch");
        List<Item> found3 = itemRepository.search("АкКуМуЛяТоРнЫй");

        assertThat(found1).hasSize(1);
        assertThat(found2).hasSize(1);
        assertThat(found3).hasSize(1);
        assertThat(found3.getFirst().getName()).isEqualTo("Шуруповёрт");
    }

    // Поиск по пустому тексту — возвращает пустой список
    @Test
    void search_withBlankText_returnsEmptyList() {
        itemRepository.save(Item.builder()
                .name("Вещь")
                .description("Хорошая вещь")
                .available(true)
                .owner(owner)
                .build());

        List<Item> result = itemRepository.search("   ");

        assertThat(result).isEmpty();
    }

    // Поиск по несуществующему тексту — пустой результат
    @Test
    void search_withNoMatches_returnsEmptyList() {
        List<Item> result = itemRepository.search("несуществующая вещь из будущего");

        assertThat(result).isEmpty();
    }

    // Вещи с requestId — должны сохраняться и находиться корректно
    @Test
    void shouldSaveAndFindItem_withRequestReference() {
        ItemRequest request = itemRequestRepository.save(ItemRequest.builder()
                .description("Нужна дрель")
                .requester(owner)
                .created(LocalDateTime.now())
                .build());

        Item item = Item.builder()
                .name("Дрель Bosch")
                .description("По вашему запросу")
                .available(true)
                .owner(owner)
                .request(request)
                .build();

        Item saved = itemRepository.save(item);

        assertThat(saved.getRequest()).isNotNull();
        assertThat(saved.getRequest().getId()).isEqualTo(request.getId());
        assertThat(saved.getRequest().getDescription()).isEqualTo("Нужна дрель");
    }

    // Поиск должен учитывать только available = true
    @Test
    void search_shouldReturnOnlyAvailableItems() {
        itemRepository.save(Item.builder()
                .name("Доступная вещь")
                .description("Можно брать")
                .available(true)
                .owner(owner)
                .build());

        itemRepository.save(Item.builder()
                .name("Недоступная вещь")
                .description("Занята")
                .available(false)
                .owner(owner)
                .build());

        List<Item> found = itemRepository.search("вещь");

        assertThat(found).hasSize(1);
        assertThat(found.getFirst().getName()).isEqualTo("Доступная вещь");
        assertThat(found.getFirst().getAvailable()).isTrue();
    }

    // Поиск по имени и описанию
    @Test
    void search_shouldFindByPartialMatch_inNameAndDescription_caseInsensitive() {
        itemRepository.save(Item.builder()
                .name("Перфоратор")
                .description("Bosch GBH 2-26")
                .available(true)
                .owner(owner)
                .build());

        assertThat(itemRepository.search("перф")).hasSize(1);
        assertThat(itemRepository.search("Перфоратор")).hasSize(1);
        assertThat(itemRepository.search("bosch")).hasSize(1);
        assertThat(itemRepository.search("GBH")).hasSize(1);
        assertThat(itemRepository.search("2-26")).hasSize(1);
        assertThat(itemRepository.search("26")).hasSize(1);
    }

    // Пустой поисковый запрос (null) — должен вести себя как blank
    @Test
    void search_withNullText_returnsEmptyList() {
        itemRepository.save(Item.builder()
                .name("Любая вещь")
                .description("Не важно")
                .available(true)
                .owner(owner)
                .build());

        List<Item> result = itemRepository.search(null);

        assertThat(result).isEmpty();
    }

    // findByOwnerId должен возвращать вещи в правильном порядке (по ID по возрастанию)
    @Test
    void findByOwnerId_returnsItems_orderedByIdAsc() {
        itemRepository.save(Item.builder()
                .name("Старая вещь")
                .description("Самая первая")
                .available(true)
                .owner(owner)
                .build());

        itemRepository.save(Item.builder()
                .name("Новая вещь")
                .description("Вторая по счёту")
                .available(true)
                .owner(owner)
                .build());

        itemRepository.save(Item.builder()
                .name("Средняя вещь")
                .description("Третья")
                .available(true)
                .owner(owner)
                .build());

        List<Item> items = itemRepository.findByOwnerId(owner.getId());

        assertThat(items).hasSize(3);
        assertThat(items)
                .extracting(Item::getName)
                .containsExactly("Старая вещь", "Новая вещь", "Средняя вещь");

        assertThat(items.get(0).getId()).isLessThan(items.get(1).getId());
        assertThat(items.get(1).getId()).isLessThan(items.get(2).getId());
    }
}