package ru.practicum.shareit.booking.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.core.exception.exceptions.BookingBadRequestException;
import ru.practicum.shareit.core.exception.exceptions.BookingNotFoundException;
import ru.practicum.shareit.core.exception.exceptions.UnsupportedStatusException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemServiceImpl;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserServiceImpl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static ru.practicum.shareit.booking.model.Status.*;
import static ru.practicum.shareit.booking.service.BookingServiceImpl.SORT;

@ExtendWith(MockitoExtension.class)
public class BookingServiceTest {
    @Mock
    private UserServiceImpl userService;
    @Mock
    private ItemServiceImpl itemService;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private StartAndEndValidator startAndEndValidator;
    @InjectMocks
    private BookingServiceImpl bookingService;
    private long bookingId;
    private Booking booking;
    private Item item;
    private ItemDto itemDto;
    private User user;
    private UserDto userDto;
    private User notOwner;
    private UserDto notOwnerDto;
    private Booking bookingWithStatusIsPast;
    private Booking bookingWithStatusIsFuture;
    private Booking bookingWithStatusIsCurrent;
    private Booking bookingWithStatusIsRejected;
    public static Pageable pageable = PageRequest.of(0, 10, SORT);
    @Captor
    private ArgumentCaptor<Booking> captor;

    @BeforeEach
    public void init() {
        user = new User(1L, "test", "test@mail.ru");
        userDto = UserMapper.toUserDto(user);
        notOwner = new User(2L, "fake", "fake@mail.ru");
        notOwnerDto = UserMapper.toUserDto(notOwner);
        item = new Item(1L, "tool", "cool tool", true, 1L, null);
        itemDto = ItemMapper.toItemDto(item);

        bookingId = 1L;
        booking = new Booking(
                bookingId,
                LocalDateTime.of(2026, 11, 11, 11, 11),
                LocalDateTime.of(2027, 11, 11, 11, 11),
                item,
                notOwner,
                WAITING
        );

        bookingWithStatusIsPast = new Booking(
                3L,
                LocalDateTime.of(2021, 11, 11, 11, 11),
                LocalDateTime.of(2022, 11, 11, 11, 11),
                item,
                notOwner,
                REJECTED
        );

        bookingWithStatusIsCurrent = new Booking(
                6L,
                LocalDateTime.of(2022, 11, 11, 11, 11),
                LocalDateTime.of(2024, 11, 11, 11, 11),
                item,
                notOwner,
                WAITING
        );

        bookingWithStatusIsFuture = new Booking(
                4L,
                LocalDateTime.of(2025, 11, 11, 11, 11),
                LocalDateTime.of(2026, 11, 11, 11, 11),
                item,
                notOwner,
                WAITING
        );

        bookingWithStatusIsRejected = new Booking(
                5L,
                LocalDateTime.of(2025, 11, 11, 11, 11),
                LocalDateTime.of(2026, 11, 11, 11, 11),
                item,
                notOwner,
                REJECTED
        );
    }

    @Test
    void saveBooking_whenNotOwnerRequests_thenBookingReturned() {
        when(itemService.getExistingItem(item.getId())).thenReturn(item);
        when(bookingRepository.save(any())).thenReturn(booking);

        BookingDto actual = bookingService.save(2L, BookingMapper.toShortBookingDto(booking));

        assertEquals(booking.getId(), actual.getId());
        assertEquals(booking.getItem(), actual.getItem());
        assertEquals(booking.getBooker(), actual.getBooker());
        assertEquals(booking.getStart(), actual.getStart());
        verify(bookingRepository).save(any(Booking.class));
    }

    @Test
    void saveBooking_whenOwnerRequests_thenExceptionReturned() {
        when(itemService.getExistingItem(item.getId())).thenReturn(item);

        assertThrows(BookingNotFoundException.class,
                () -> bookingService.save(1L, BookingMapper.toShortBookingDto(booking)));
    }

    @Test
    void saveBooking_whenAvailableIsFalse_thenExceptionReturned() {
        item.setAvailable(false);
        when(itemService.getExistingItem(item.getId())).thenReturn(item);

        assertThrows(BookingBadRequestException.class,
                () -> bookingService.save(2L, BookingMapper.toShortBookingDto(booking)));
    }

    @Test
    void saveBooking_whenUserNotExists_thenExceptionReturned() {
        when(itemService.getExistingItem(item.getId())).thenReturn(item);

        assertThrows(BookingNotFoundException.class,
                () -> bookingService.save(1L, BookingMapper.toShortBookingDto(booking)));
    }

    @Test
    void approveBooking_whenOwnerRequests_thenItemReturned() {
        when(itemService.getExistingItem(item.getId())).thenReturn(item);
        when(bookingRepository.save(any())).thenReturn(booking);
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

        bookingService.save(2L, BookingMapper.toShortBookingDto(booking));
        bookingService.approve(user.getId(), bookingId, true);

        verify(bookingRepository, times(2)).save(captor.capture());
        Booking savedBooking = captor.getValue();
        assertEquals(APPROVED, savedBooking.getStatus());
    }

    @Test
    void approveBooking_whenNotOwnerRequests_thenExceptionReturned() {
        when(itemService.getExistingItem(item.getId())).thenReturn(item);
        when(bookingRepository.save(any())).thenReturn(booking);
        when(bookingRepository.findById(bookingId)).thenThrow(BookingNotFoundException.class);

        bookingService.save(2L, BookingMapper.toShortBookingDto(booking));

        verify(bookingRepository, times(1)).save(captor.capture());
        assertThrows(BookingNotFoundException.class,
                () -> bookingService.approve(notOwner.getId(), bookingId, true));
    }

    @Test
    void approveBooking_whenStatusAlreadyApproved_thenExceptionReturned() {
        Booking bookingWithStatusAlreadyApproved = new Booking(
                bookingId,
                LocalDateTime.of(2026, 11, 11, 11, 11),
                LocalDateTime.of(2027, 11, 11, 11, 11),
                item,
                notOwner,
                APPROVED
        );

        when(itemService.getExistingItem(item.getId())).thenReturn(item);
        when(bookingRepository.save(any())).thenReturn(bookingWithStatusAlreadyApproved);
        when(bookingRepository.findById(bookingId)).thenThrow(BookingBadRequestException.class);

        bookingService.save(2L, BookingMapper.toShortBookingDto(bookingWithStatusAlreadyApproved));

        verify(bookingRepository, times(1)).save(captor.capture());
        assertThrows(BookingBadRequestException.class, () -> bookingService.approve(notOwner.getId(), bookingId, true));
    }

    @Test
    void findBookingById_whenExists_thenBookingReturned() {
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

        BookingDto actual = bookingService.findById(bookingId, user.getId());

        assertEquals(booking.getItem(), actual.getItem());
        assertEquals(booking.getBooker(), actual.getBooker());
    }

    @Test
    void findBookingById_whenNotExists_thenExceptionReturned() {
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.empty());

        assertThrows(BookingNotFoundException.class, () -> bookingService.findById(bookingId, user.getId()));
    }

    @Test
    void findBookingById_whenOtherUserRequests_thenExceptionReturned() {
        User other = new User(7L, "Phil", "bad@mail.ru");
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

        assertThrows(BookingNotFoundException.class, () -> bookingService.findById(bookingId, other.getId()));
    }

    @Test
    void findByUserIdAndState_whenCurrentFound_thenBookingListReturned() {
        List<Booking> bookings = List.of(bookingWithStatusIsCurrent);
        when(bookingRepository.findByBookerIdCurrent(anyLong(), any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(List.of(bookingWithStatusIsCurrent));

        List<Booking> actualBookings = bookingService.findByUserIdAndState(notOwner.getId(), "CURRENT", pageable)
                .stream()
                .map(BookingMapper::toBookingFromBookingDto)
                .collect(Collectors.toList());

        assertEquals(bookings, actualBookings);
        assertEquals(1, actualBookings.size());
    }

    @Test
    void findByUserIdAndState_whenPastFound_thenBookingListReturned() {
        List<Booking> bookings = List.of(bookingWithStatusIsPast);
        when(bookingRepository.findByBookerIdAndEndIsBefore(anyLong(), any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(List.of(bookingWithStatusIsPast));

        List<Booking> actualBookings = bookingService.findByUserIdAndState(notOwner.getId(), "PAST", pageable)
                .stream()
                .map(BookingMapper::toBookingFromBookingDto)
                .collect(Collectors.toList());

        assertEquals(bookings, actualBookings);
        assertEquals(1, actualBookings.size());
    }

    @Test
    void findByUserIdAndState_whenFutureFound_thenBookingListReturned() {
        List<Booking> bookings = List.of(bookingWithStatusIsFuture);
        when(bookingRepository.findByBookerIdAndStartIsAfter(anyLong(), any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(List.of(bookingWithStatusIsFuture));

        List<Booking> actualBookings = bookingService.findByUserIdAndState(notOwner.getId(), "FUTURE", pageable)
                .stream()
                .map(BookingMapper::toBookingFromBookingDto)
                .collect(Collectors.toList());

        assertEquals(bookings, actualBookings);
        assertEquals(1, actualBookings.size());
    }

    @Test
    void findByUserIdAndState_whenRejectedFound_thenBookingListReturned() {
        List<Booking> bookings = List.of(bookingWithStatusIsRejected);
        when(bookingRepository.findByBookerIdAndStatus(anyLong(), any(Status.class), any(Pageable.class)))
                .thenReturn(List.of(bookingWithStatusIsRejected));

        List<Booking> actualBookings = bookingService.findByUserIdAndState(notOwner.getId(), "REJECTED", pageable)
                .stream()
                .map(BookingMapper::toBookingFromBookingDto)
                .collect(Collectors.toList());

        assertEquals(bookings, actualBookings);
        assertEquals(1, actualBookings.size());
    }

    @Test
    void findByUserIdAndState_whenWaitingFound_thenBookingListReturned() {
        List<Booking> bookings = List.of(booking);
        when(bookingRepository.findByBookerIdAndStatus(anyLong(), any(Status.class), any(Pageable.class)))
                .thenReturn(List.of(booking));

        List<Booking> actualBookings = bookingService.findByUserIdAndState(notOwner.getId(), "WAITING", pageable)
                .stream()
                .map(BookingMapper::toBookingFromBookingDto)
                .collect(Collectors.toList());

        assertEquals(bookings, actualBookings);
        assertEquals(1, actualBookings.size());
    }

    @Test
    void findByUserIdAndState_whenAllFound_thenBookingListReturned() {
        List<Booking> bookings = List.of(booking);
        when(bookingRepository.findByBookerId(anyLong(), any(Pageable.class))).thenReturn(List.of(booking));

        List<Booking> actualBookings = bookingService.findByUserIdAndState(notOwner.getId(), "ALL", pageable)
                .stream()
                .map(BookingMapper::toBookingFromBookingDto)
                .collect(Collectors.toList());

        assertEquals(bookings, actualBookings);
        assertEquals(1, actualBookings.size());
    }

    @Test
    void findByUserIdAndState_whenStatusIsUnsupported_thenExceptionReturned() {
        assertThrows(UnsupportedStatusException.class,
                () -> bookingService.findByUserIdAndState(notOwner.getId(), String.valueOf("UNSUPPORTED"), pageable));
    }

    @Test
    void findBookingsByItemOwnerId_whenStatusIsUnsupported_thenExceptionReturned() {
        assertThrows(UnsupportedStatusException.class,
                () -> bookingService.findBookingsByItemOwnerId(notOwner.getId(), String.valueOf("UNSUPPORTED"), pageable));
    }

    @Test
    void findBookingsByItemOwnerId_whenCurrentFound_thenBookingListReturned() {
        List<Booking> bookings = List.of(bookingWithStatusIsCurrent);
        when(itemService.getExistingItem(item.getId())).thenReturn(item);
        when(bookingRepository.save(any())).thenReturn(bookingWithStatusIsCurrent);
        when(bookingRepository.findBookingsByItemOwnerCurrent(anyLong(), any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(bookings);

        bookingService.save(2L, BookingMapper.toShortBookingDto(bookingWithStatusIsCurrent));
        List<Booking> actualBookings = bookingService.findBookingsByItemOwnerId(1L, "CURRENT", pageable)
                .stream()
                .map(BookingMapper::toBookingFromBookingDto)
                .collect(Collectors.toList());

        assertEquals(bookings, actualBookings);
        assertEquals(1, actualBookings.size());
    }

    @Test
    void findBookingsByItemOwnerId_whenWaitingFound_thenBookingListReturned() {
        List<Booking> bookings = List.of(booking);
        when(itemService.getExistingItem(item.getId())).thenReturn(item);
        when(bookingRepository.save(any())).thenReturn(booking);
        when(bookingRepository.findBookingsByItemOwnerAndStatus(anyLong(), any(Status.class), any(Pageable.class)))
                .thenReturn(bookings);

        bookingService.save(2L, BookingMapper.toShortBookingDto(booking));
        List<Booking> actualBookings = bookingService.findBookingsByItemOwnerId(1L, "WAITING", pageable)
                .stream()
                .map(BookingMapper::toBookingFromBookingDto)
                .collect(Collectors.toList());

        assertEquals(bookings, actualBookings);
        assertEquals(1, actualBookings.size());
    }

    @Test
    void findBookingsByItemOwnerId_whenRejectedFound_thenBookingListReturned() {
        List<Booking> bookings = List.of(bookingWithStatusIsRejected);
        when(itemService.getExistingItem(item.getId())).thenReturn(item);
        when(bookingRepository.save(any())).thenReturn(bookingWithStatusIsRejected);
        when(bookingRepository.findBookingsByItemOwnerAndStatus(anyLong(), any(Status.class), any(Pageable.class)))
                .thenReturn(bookings);

        bookingService.save(2L, BookingMapper.toShortBookingDto(bookingWithStatusIsRejected));
        List<Booking> actualBookings = bookingService.findBookingsByItemOwnerId(1L, "REJECTED", pageable)
                .stream()
                .map(BookingMapper::toBookingFromBookingDto)
                .collect(Collectors.toList());

        assertEquals(bookings, actualBookings);
        assertEquals(1, actualBookings.size());
    }

    @Test
    void findBookingsByItemOwnerId_whenFutureFound_thenBookingListReturned() {
        List<Booking> bookings = List.of(bookingWithStatusIsFuture);
        when(itemService.getExistingItem(item.getId())).thenReturn(item);
        when(bookingRepository.save(any())).thenReturn(bookingWithStatusIsFuture);
        when(bookingRepository.findBookingsByItemOwnerAndStartIsAfter(anyLong(), any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(bookings);

        bookingService.save(2L, BookingMapper.toShortBookingDto(bookingWithStatusIsFuture));
        List<Booking> actualBookings = bookingService.findBookingsByItemOwnerId(1L, "FUTURE", pageable)
                .stream()
                .map(BookingMapper::toBookingFromBookingDto)
                .collect(Collectors.toList());

        assertEquals(bookings, actualBookings);
        assertEquals(1, actualBookings.size());
    }

    @Test
    void findBookingsByItemOwnerId_whenPastFound_thenBookingListReturned() {
        List<Booking> bookings = List.of(bookingWithStatusIsPast);
        when(itemService.getExistingItem(item.getId())).thenReturn(item);
        when(bookingRepository.save(any())).thenReturn(bookingWithStatusIsPast);
        when(bookingRepository.findBookingsByItemOwnerAndEndIsBefore(anyLong(), any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(bookings);

        bookingService.save(2L, BookingMapper.toShortBookingDto(bookingWithStatusIsPast));
        List<Booking> actualBookings = bookingService.findBookingsByItemOwnerId(1L, "PAST", pageable)
                .stream()
                .map(BookingMapper::toBookingFromBookingDto)
                .collect(Collectors.toList());

        assertEquals(bookings, actualBookings);
        assertEquals(1, actualBookings.size());
    }

    @Test
    void findBookingsByItemOwnerId_whenAllFound_thenBookingListReturned() {
        List<Booking> bookings = List.of(bookingWithStatusIsPast);
        when(itemService.getExistingItem(item.getId())).thenReturn(item);
        when(bookingRepository.save(any())).thenReturn(bookingWithStatusIsPast);
        when(bookingRepository.findBookingsByItemOwner(anyLong(), any(Pageable.class))).thenReturn(bookings);

        bookingService.save(2L, BookingMapper.toShortBookingDto(bookingWithStatusIsPast));
        List<Booking> actualBookings = bookingService.findBookingsByItemOwnerId(1L, "ALL", pageable)
                .stream()
                .map(BookingMapper::toBookingFromBookingDto)
                .collect(Collectors.toList());

        assertEquals(bookings, actualBookings);
        assertEquals(1, actualBookings.size());
    }
}