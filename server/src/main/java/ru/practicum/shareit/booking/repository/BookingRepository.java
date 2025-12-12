package ru.practicum.shareit.booking.repository;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    // Все бронирования пользователя (booker)
    List<Booking> findByBookerId(Long bookerId, Sort sort);

    // Текущие бронирования пользователя
    @Query("SELECT b FROM Booking b WHERE b.booker.id = :bookerId " +
            "AND b.startDate <= :now AND b.endDate >= :now")
    List<Booking> findCurrentByBookerId(@Param("bookerId") Long bookerId,
                                        @Param("now") LocalDateTime now,
                                        Sort sort);

    // Прошлые бронирования пользователя
    List<Booking> findByBookerIdAndEndDateIsBefore(Long bookerId,
                                                   LocalDateTime endDate,
                                                   Sort sort);

    // Будущие бронирования пользователя
    List<Booking> findByBookerIdAndStartDateIsAfter(Long bookerId,
                                                    LocalDateTime startDate,
                                                    Sort sort);

    // Бронирования пользователя по статусу
    List<Booking> findByBookerIdAndStatus(Long bookerId,
                                          BookingStatus status,
                                          Sort sort);

    // Все бронирования для вещей владельца
    @Query("SELECT b FROM Booking b WHERE b.item.owner.id = :ownerId")
    List<Booking> findByItemOwnerId(@Param("ownerId")Long ownerId, Sort sort);

    // Текущие бронирования для вещей владельца
    @Query("SELECT b FROM Booking b WHERE b.item.owner.id = :ownerId " +
            "AND b.startDate <= :now AND b.endDate >= :now")
    List<Booking> findCurrentByItemOwnerId(@Param("ownerId") Long ownerId,
                                           @Param("now") LocalDateTime now,
                                           Sort sort);

    // Прошлые бронирования для вещей владельца
    @Query("SELECT b FROM Booking b WHERE b.item.owner.id = :ownerId AND b.endDate < :endDate")
    List<Booking> findByItemOwnerIdAndEndDateIsBefore(@Param("ownerId") Long ownerId,
                                                      @Param("endDate") LocalDateTime endDate,
                                                      Sort sort);

    // Будущие бронирования для вещей владельца
    @Query("SELECT b FROM Booking b WHERE b.item.owner.id = :ownerId AND b.startDate > :startDate")
    List<Booking> findByItemOwnerIdAndStartDateIsAfter(@Param("ownerId") Long ownerId,
                                                       @Param("startDate") LocalDateTime startDate,
                                                       Sort sort);

    // Бронирования для вещей владельца по статусу
    @Query("SELECT b FROM Booking b WHERE b.item.owner.id = :ownerId AND b.status = :status")
    List<Booking> findByItemOwnerIdAndStatus(@Param("ownerId") Long ownerId,
                                             @Param("status") BookingStatus status,
                                             Sort sort);

    // Последнее бронирование вещи (до текущего момента)
    Optional<Booking> findFirstByItemIdAndStartDateLessThanEqualAndStatus(Long itemId,
                                                                          LocalDateTime now,
                                                                          BookingStatus status,
                                                                          Sort sort);

    // Следующее бронирование вещи (после текущего момента)
    Optional<Booking> findFirstByItemIdAndStartDateAfterAndStatus(Long itemId,
                                                                  LocalDateTime now,
                                                                  BookingStatus status, Sort sort);

    // Проверка: есть ли у пользователя одобренное бронирование этой вещи
    boolean existsByBookerIdAndItemIdAndStatusAndEndDateBefore(Long bookerId,
                                                               Long itemId,
                                                               BookingStatus status,
                                                               LocalDateTime endDate
    );
}
