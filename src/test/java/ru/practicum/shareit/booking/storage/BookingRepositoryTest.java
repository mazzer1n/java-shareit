package ru.practicum.shareit.booking.storage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static ru.practicum.shareit.booking.model.Status.APPROVED;
import static ru.practicum.shareit.booking.model.Status.WAITING;
import static ru.practicum.shareit.booking.controller.BookingController.SORT;

@DataJpaTest
public class BookingRepositoryTest {
    private BookingRepository bookingRepository;
    private TestEntityManager entityManager;
    private Booking bookingWithStatusIsCurrent;
    private Booking bookingWithStartAfterAndItemId;
    private Booking bookingWithEndBeforeAndItemId;
    private User booker;
    private Item item;

    public static Pageable pageable = PageRequest.of(0, 10, SORT);

    @Autowired
    public BookingRepositoryTest(BookingRepository bookingRepository, TestEntityManager entityManager) {
        this.bookingRepository = bookingRepository;
        this.entityManager = entityManager;
    }

    @BeforeEach
    void beforeEach() {
        booker = User.builder()
                .name("test")
                .email("test@mail.ru")
                .build();

        item = Item.builder()
                .name("tool")
                .description("cool tool")
                .available(true)
                .owner(1L)
                .build();

        bookingWithStatusIsCurrent = Booking.builder()
                .start(LocalDateTime.of(2022, 11, 11, 11, 11))
                .end(LocalDateTime.of(2024, 11, 11, 11, 11))
                .item(item)
                .booker(booker)
                .status(WAITING)
                .build();

        bookingWithStartAfterAndItemId = Booking.builder()
                .start(LocalDateTime.of(2025, 11, 11, 11, 11))
                .end(LocalDateTime.of(2026, 11, 11, 11, 11))
                .item(item)
                .booker(booker)
                .status(WAITING)
                .build();

        bookingWithEndBeforeAndItemId = Booking.builder()
                .start(LocalDateTime.of(2021, 11, 11, 11, 11))
                .end(LocalDateTime.of(2022, 11, 11, 11, 11))
                .item(item)
                .booker(booker)
                .status(APPROVED)
                .build();
    }

    @Test
    @DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
    void findByBookerIdCurrent() {
        this.entityManager.persist(booker);
        this.entityManager.persist(item);
        this.entityManager.persist(bookingWithStatusIsCurrent);
        List<Booking> actual = bookingRepository.findByBookerIdCurrent(booker.getId(), LocalDateTime.now(), pageable);

        assertEquals(1, actual.size());
        assertEquals(bookingWithStatusIsCurrent.getStart(), actual.get(0).getStart());
        assertEquals(bookingWithStatusIsCurrent.getEnd(), actual.get(0).getEnd());
        assertEquals(bookingWithStatusIsCurrent.getBooker(), actual.get(0).getBooker());
        assertEquals(bookingWithStatusIsCurrent.getItem(), actual.get(0).getItem());
        assertEquals(bookingWithStatusIsCurrent.getStatus(), actual.get(0).getStatus());
    }

    @Test
    @DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
    void findBookingByItemIdAndStartBefore() {
        this.entityManager.persist(booker);
        this.entityManager.persist(item);
        this.entityManager.persist(bookingWithEndBeforeAndItemId);
        List<Booking> actual = bookingRepository.findBookingByItemIdAndStartBefore(item.getId(), LocalDateTime.now());

        assertEquals(1, actual.size());
        assertEquals(bookingWithEndBeforeAndItemId.getStart(), actual.get(0).getStart());
        assertEquals(bookingWithEndBeforeAndItemId.getEnd(), actual.get(0).getEnd());
        assertEquals(bookingWithEndBeforeAndItemId.getBooker(), actual.get(0).getBooker());
        assertEquals(bookingWithEndBeforeAndItemId.getItem(), actual.get(0).getItem());
        assertEquals(bookingWithEndBeforeAndItemId.getStatus(), actual.get(0).getStatus());
    }

    @Test
    @DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
    void findBookingByItemIdAndStartAfter() {
        this.entityManager.persist(booker);
        this.entityManager.persist(item);
        this.entityManager.persist(bookingWithStartAfterAndItemId);
        List<Booking> actual = bookingRepository.findBookingByItemIdAndStartAfter(item.getId(), LocalDateTime.now());

        assertEquals(1, actual.size());
        assertEquals(bookingWithStartAfterAndItemId.getStart(), actual.get(0).getStart());
        assertEquals(bookingWithStartAfterAndItemId.getEnd(), actual.get(0).getEnd());
        assertEquals(bookingWithStartAfterAndItemId.getBooker(), actual.get(0).getBooker());
        assertEquals(bookingWithStartAfterAndItemId.getItem(), actual.get(0).getItem());
        assertEquals(bookingWithStartAfterAndItemId.getStatus(), actual.get(0).getStatus());
    }

    @Test
    @DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
    void findBookingsByItemOwnerCurrent() {
        this.entityManager.persist(booker);
        this.entityManager.persist(item);
        this.entityManager.persist(bookingWithStatusIsCurrent);
        List<Booking> actual = bookingRepository.findBookingsByItemOwnerCurrent(1L, LocalDateTime.now(), pageable);

        assertEquals(1, actual.size());
        assertEquals(bookingWithStatusIsCurrent.getStart(), actual.get(0).getStart());
        assertEquals(bookingWithStatusIsCurrent.getEnd(), actual.get(0).getEnd());
        assertEquals(bookingWithStatusIsCurrent.getBooker(), actual.get(0).getBooker());
        assertEquals(bookingWithStatusIsCurrent.getItem(), actual.get(0).getItem());
        assertEquals(bookingWithStatusIsCurrent.getStatus(), actual.get(0).getStatus());
    }

    @Test
    @DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
    void findBookingsToAddComment() {
        this.entityManager.persist(booker);
        this.entityManager.persist(item);
        this.entityManager.persist(bookingWithEndBeforeAndItemId);
        List<Booking> actual = bookingRepository.findBookingsToAddComment(item.getId(), booker.getId(), LocalDateTime.now());

        assertEquals(1, actual.size());
        assertEquals(bookingWithEndBeforeAndItemId.getStart(), actual.get(0).getStart());
        assertEquals(bookingWithEndBeforeAndItemId.getEnd(), actual.get(0).getEnd());
        assertEquals(bookingWithEndBeforeAndItemId.getBooker(), actual.get(0).getBooker());
        assertEquals(bookingWithEndBeforeAndItemId.getItem(), actual.get(0).getItem());
        assertEquals(bookingWithEndBeforeAndItemId.getStatus(), actual.get(0).getStatus());
    }
}
