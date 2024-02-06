package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.dto.ShortBookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.core.exception.exceptions.BookingBadRequestException;
import ru.practicum.shareit.core.exception.exceptions.BookingNotFoundException;
import ru.practicum.shareit.core.exception.exceptions.CommentBadRequestException;
import ru.practicum.shareit.core.exception.exceptions.UnsupportedStatusException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemServiceImpl;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserServiceImpl;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.shareit.booking.dto.BookingMapper.*;
import static ru.practicum.shareit.booking.model.Status.*;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final ItemServiceImpl itemService;
    private final UserServiceImpl userService;
    public static final Sort SORT = Sort.by("start").descending();

    @Transactional
    @Override
    public BookingDto save(Long userId, ShortBookingDto dto) {
        Item item = itemService.getExistingItem(dto.getItemId());
        User booker = userService.getExistingUser(userId);

        if (item.getOwner().equals(userId)) {
            throw new BookingNotFoundException("Вещь не может быть забронирована ее владельцем.");
        }

        if (!item.getAvailable()) {
            throw new BookingBadRequestException("В данный момент товар недоступен для бронирования.");
        }

        Booking booking = toBooking(dto, item, booker);
        booking.setStatus(WAITING);

        return toBookingDto(bookingRepository.save(booking));
    }

    @Transactional
    @Override
    public BookingDto approve(Long userId, Long bookingId, Boolean approved) {
        Booking booking = getExistingBooking(bookingId);
        Item item = itemService.getExistingItem(booking.getItem().getId());

        if (!item.getOwner().equals(userId)) {
            throw new BookingNotFoundException("Запрос может быть выполнен только владельцем вещи.");
        }

        Status status = approved ? APPROVED : REJECTED;
        if (booking.getStatus().equals(status)) {
            throw new BookingBadRequestException("Ваша заявка уже ожидает подтверждения.");
        }

        booking.setStatus(status);
        booking = bookingRepository.save(booking);

        return toBookingDto(booking);
    }

    @Transactional(readOnly = true)
    @Override
    public BookingDto findById(Long id, Long userId) {
        userService.getExistingUser(userId);
        Booking booking = getExistingBooking(id);
        validateRequester(booking, userId);

        return toBookingDto(booking);
    }

    @Transactional(readOnly = true)
    @Override
    public Collection<BookingDto> findByUserIdAndState(Long userId, String state, Pageable pageable) {
        userService.getExistingUser(userId);
        hasUserZeroItems(userId);

        List<Booking> bookings;

        switch (state) {
            case "ALL":
                bookings = bookingRepository.findByBookerId(userId, pageable);
                break;
            case "CURRENT":
                bookings = bookingRepository.findByBookerIdCurrent(userId, LocalDateTime.now(), pageable);
                break;
            case "PAST":
                bookings = bookingRepository.findByBookerIdAndEndIsBefore(userId, LocalDateTime.now(), pageable);
                break;
            case "FUTURE":
                bookings = bookingRepository.findByBookerIdAndStartIsAfter(userId, LocalDateTime.now(), pageable);
                break;
            case "WAITING":
                bookings = bookingRepository.findByBookerIdAndStatus(userId, WAITING, pageable);
                break;
            case "REJECTED":
                bookings = bookingRepository.findByBookerIdAndStatus(userId, REJECTED, pageable);
                break;
            default:
                throw new UnsupportedStatusException("Unknown state: UNSUPPORTED_STATUS");
        }

        return bookings.stream()
                .map(BookingMapper::toBookingDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Override
    public Collection<BookingDto> findBookingsByItemOwnerId(Long userId, String state, Pageable pageable) {
        userService.getExistingUser(userId);
        hasUserZeroItems(userId);

        List<Booking> bookings;

        switch (state) {
            case "ALL":
                bookings = bookingRepository.findBookingsByItemOwner(userId, pageable);
                break;
            case "CURRENT":
                bookings = bookingRepository.findBookingsByItemOwnerCurrent(userId, LocalDateTime.now(),
                        pageable);
                break;
            case "PAST":
                bookings = bookingRepository.findBookingsByItemOwnerAndEndIsBefore(userId, LocalDateTime.now(), pageable);
                break;
            case "FUTURE":
                bookings = bookingRepository.findBookingsByItemOwnerAndStartIsAfter(userId, LocalDateTime.now(), pageable);
                break;
            case "WAITING":
                bookings = bookingRepository.findBookingsByItemOwnerAndStatus(userId, WAITING, pageable);
                break;
            case "REJECTED":
                bookings = bookingRepository.findBookingsByItemOwnerAndStatus(userId, REJECTED, pageable);
                break;
            default:
                throw new UnsupportedStatusException("Unknown state: UNSUPPORTED_STATUS");
        }

        return bookings.stream()
                .map(BookingMapper::toBookingDto)
                .collect(Collectors.toList());
    }


    public String checkUserBookingState(String state) {
        if (state == null || state.isBlank()) {
            state = "ALL";
        }

        return state;
    }

    private Booking getExistingBooking(long id) {
        return bookingRepository.findById(id).orElseThrow(
                () -> new BookingNotFoundException("Бронирование с id " + id + " не найдено.")
        );
    }

    public void hasUserZeroItems(long userId) {
        if (itemService.hasUserZeroItems(userId)) {
            throw new BookingBadRequestException("Этот запрос имеет смысл для владельца хотя бы одной вещи. ");
        }
    }

    private void validateRequester(Booking booking, long userId) {
        long bookingAuthorId = booking.getBooker().getId();
        long itemOwnerId = booking.getItem().getOwner();

        if (bookingAuthorId != userId && itemOwnerId != userId) {
            throw new BookingNotFoundException("Запрос может быть выполнен либо автором бронирования, " +
                    "либо владельцем вещи, к которой относится бронирование.");
        }
    }

    public void validateBookingsToAddComment(Long userId, Long itemId) {
        List<Booking> previousBookings = bookingRepository.findBookingsToAddComment(itemId, userId, LocalDateTime.now());

        if (previousBookings.isEmpty()) {
            throw new CommentBadRequestException(
                    "Пользователь может оставить комментарий только на вещь, которую ранее использовал."
            );
        }

        for (Booking booking : previousBookings) {
            if (booking.getEnd().isAfter(LocalDateTime.now())) {
                throw new CommentBadRequestException("Оставить комментарий можно только после окончания срока аренды");
            }
        }
    }

    public void fillItemWithBookings(ItemDto result) {
        LocalDateTime now = LocalDateTime.now();
        bookingRepository
                .findBookingByItemIdAndStartBefore(result.getId(), now)
                .stream()
                .findFirst().ifPresent(lastBooking -> result.setLastBooking(toShortBookingDto(lastBooking)));

        bookingRepository
                .findBookingByItemIdAndStartAfter(result.getId(), now)
                .stream()
                .findFirst().ifPresent(nextBooking -> result.setNextBooking(toShortBookingDto(nextBooking)));

        if (result.getLastBooking() == null) {
            result.setNextBooking(null);
        }
    }
}