package ru.practicum.shareit.booking.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.*;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.core.validation.PaginationValidator;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.Collection;

@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
@Slf4j
public class BookingController {
    private final BookingService bookingService;
    public static final Sort SORT = Sort.by("start").descending();

    @GetMapping("/owner")
    public Collection<BookingDto> findAllByUserId(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                  @RequestParam(name = "state", defaultValue = "ALL") String stateParam,
                                                  @PositiveOrZero @RequestParam(name = "from", defaultValue = "0") Integer from,
                                                  @Positive @RequestParam(name = "size", defaultValue = "10") Integer size) {
        PaginationValidator.validatePagination(from, size);
        Pageable pageable = PageRequest.of(from / size, size, SORT);
        Collection<BookingDto> bookingDtos = bookingService.findBookingsByItemOwnerId(userId, stateParam, pageable);
        log.info("All bookings by user id - {}: size - {}", userId, bookingDtos.size());
        return bookingDtos;
    }

    @GetMapping
    public Collection<BookingDto> findByUserIdAndState(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                       @RequestParam(name = "state", defaultValue = "ALL") String stateParam,
                                                       @PositiveOrZero @RequestParam(name = "from", defaultValue = "0") Integer from,
                                                       @Positive @RequestParam(name = "size", defaultValue = "10") Integer size) {
        PaginationValidator.validatePagination(from, size);
        Pageable pageable = PageRequest.of(from / size, size, SORT);
        Collection<BookingDto> bookingDtos = bookingService.findByUserIdAndState(userId, stateParam, pageable);
        log.info("All bookings by user id - {} and state - {}: size - {}", userId, stateParam, bookingDtos.size());
        return bookingDtos;
    }

    @GetMapping("/{bookingId}")
    public BookingDto findById(@PathVariable Long bookingId, @RequestHeader("X-Sharer-User-Id") Long userId) {
        BookingDto bookingDto = bookingService.findById(bookingId, userId);
        log.info("Booking by id - {}: {}", bookingId, bookingDto);
        return bookingDto;
    }

    @PostMapping
    public BookingDto save(@RequestHeader("X-Sharer-User-Id") Long userId, @Valid @RequestBody ShortBookingDto dto) {
        BookingDto bookingDto = bookingService.save(userId, dto);
        log.info("Save booking - {}", bookingDto);
        return bookingDto;
    }

    @PatchMapping("/{bookingId}")
    public BookingDto update(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @PathVariable Long bookingId,
            @RequestParam Boolean approved
    ) {
        BookingDto bookingDto = bookingService.approve(userId, bookingId, approved);
        log.info("Update booking - {}", bookingDto);
        return bookingDto;
    }
}