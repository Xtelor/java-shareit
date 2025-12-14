package ru.practicum.shareit.item.model;

import jakarta.persistence.*;
import lombok.*;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.request.model.ItemRequest;

@Entity
@Table(name = "items")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Item {
    // ID вещи
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Название вещи
    @Column(name = "name", nullable = false)
    private String name;

    // Описание вещи
    @Column(name = "description", nullable = false)
    private String description;

    // Статус вещи(доступна/недоступна)
    @Column(name = "available", nullable = false)
    private Boolean available;

    // Владелец вещи
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    @ToString.Exclude
    private User owner;

    // Запрос на вещь (может быть null)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id")
    @ToString.Exclude
    private ItemRequest request;
}
