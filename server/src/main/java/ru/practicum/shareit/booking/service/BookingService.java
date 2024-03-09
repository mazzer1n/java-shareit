package ru.practicum.shareit.booking.service;

import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.booking.dto.*;

import java.util.Collection;

public interface BookingService {
    BookingDto save(Long userId, ShortBookingDto dto);

    BookingDto approve(Long userId, Long bookingId, Boolean approved);

    BookingDto findById(Long id, Long userId);

    Collection<BookingDto> findByUserIdAndState(Long userId, String state, Pageable pageable);

    Collection<BookingDto> findBookingsByItemOwnerId(Long userId, String state, Pageable pageable);

    public void hasUserZeroItems(long userId);

    public String checkUserBookingState(String state);
}