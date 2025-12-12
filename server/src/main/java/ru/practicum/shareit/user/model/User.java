package ru.practicum.shareit.user.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class User {
    // ID пользователя
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Имя пользователя
    @Column(name = "name", nullable = false)
    private String name;

    // Электронная почта пользователя
    @Column(name = "email", nullable = false, unique = true)
    private String email;
}
