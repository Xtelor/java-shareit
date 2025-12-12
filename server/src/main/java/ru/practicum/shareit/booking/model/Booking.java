package ru.practicum.shareit.booking.model;

import jakarta.persistence.*;
import lombok.*;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

@Entity
@Table(name = "bookings")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Booking {
    // ID
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Дата бронирования
    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    // Дата окончания бронирования
    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    // Вещь
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    @ToString.Exclude
    private Item item;

    // Бронирующий пользователь
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booker_id", nullable = false)
    @ToString.Exclude
    private User booker;

    // Статус
    @Enumerated(EnumType.STRING)
    private BookingStatus status;
}
