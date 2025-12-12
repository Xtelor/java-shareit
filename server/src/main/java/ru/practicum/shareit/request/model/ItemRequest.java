package ru.practicum.shareit.request.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

@Entity
@Table(name = "requests")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class ItemRequest {
    // ID запроса
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Описание запроса
    @Column(name = "description", nullable = false)
    private String description;

    // Запрашивающий пользователь
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_id", nullable = false)
    @ToString.Exclude
    private User requester;

    // Дата создания запроса
    @CreationTimestamp
    @Column(name = "created", nullable = false)
    private LocalDateTime created;
}
