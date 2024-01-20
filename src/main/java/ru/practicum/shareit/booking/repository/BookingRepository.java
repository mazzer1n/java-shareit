package ru.practicum.shareit.booking.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.booking.model.*;
import ru.practicum.shareit.core.exception.exceptions.CommentBadRequestException;
import ru.practicum.shareit.item.dto.ItemDto;

import java.time.LocalDateTime;
import java.util.List;

import static ru.practicum.shareit.booking.dto.BookingMapper.toShortBookingDto;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByBookerId(Long bookerId, Pageable pageable);

    List<Booking> findByBookerIdAndStatus(Long bookerId, Status status, Pageable pageable);

    List<Booking> findByBookerIdAndEndIsBefore(Long bookerId, LocalDateTime end, Pageable pageable);

    List<Booking> findByBookerIdAndStartIsAfter(Long bookerId, LocalDateTime start, Pageable pageable);

    @Query("select b from Booking b " +
            "where b.booker.id = ?1 " +
            "and b.start < ?2 " +
            "and b.end > ?2 ")
    List<Booking> findByBookerIdCurrent(Long bookerId, LocalDateTime now, Pageable pageable);

    List<Booking> findBookingsByItemOwner(Long userId, Pageable pageable);

    List<Booking> findBookingsByItemOwnerAndStatus(Long userId, Status status, Pageable pageable);

    List<Booking> findBookingsByItemOwnerAndEndIsBefore(Long userId, LocalDateTime end, Pageable pageable);

    List<Booking> findBookingsByItemOwnerAndStartIsAfter(Long userId, LocalDateTime start, Pageable pageable);

    @Query("select b from Booking b " +
            "where b.item.id = ?1 " +
            "and b.start < ?2 " +
            "order by b.start desc")
    List<Booking> findBookingByItemIdAndStartBefore(Long itemId, LocalDateTime now);

    @Query("select b from Booking b " +
            "where b.item.id = ?1 " +
            "and b.start > ?2 " +
            "order by b.start asc")
    List<Booking> findBookingByItemIdAndStartAfter(Long itemId, LocalDateTime now);

    @Query("select b from Booking b " +
            "where b.item.owner = ?1 " +
            "and b.start < ?2 " +
            "and b.end > ?2 " +
            "order by b.start asc")
    List<Booking> findBookingsByItemOwnerCurrent(Long userId, LocalDateTime now, Pageable pageable);

    @Query("select b from Booking b " +
            "where b.item.id = ?1 " +
            "and b.booker.id = ?2 " +
            "and b.end < ?3")
    List<Booking> findBookingsToAddComment(Long itemId, Long userId, LocalDateTime now);

    default void fillItemWithBookings(ItemDto result) {
        LocalDateTime now = LocalDateTime.now();
        findBookingByItemIdAndStartBefore(result.getId(), now)
                .stream()
                .findFirst().ifPresent(lastBooking -> result.setLastBooking(toShortBookingDto(lastBooking)));

        findBookingByItemIdAndStartAfter(result.getId(), now)
                .stream()
                .findFirst().ifPresent(nextBooking -> result.setNextBooking(toShortBookingDto(nextBooking)));

        if (result.getLastBooking() == null) {
            result.setNextBooking(null);
        }
    }

    default void validateBookingsToAddComment(Long userId, Long itemId) {
        List<Booking> previousBookings = findBookingsToAddComment(itemId, userId, LocalDateTime.now());

        if (previousBookings.isEmpty()) {
            throw new CommentBadRequestException(
                    "Пользователь может оставить комментарий только на вещь, которую ранее использовал."
            );
        }
    }
}