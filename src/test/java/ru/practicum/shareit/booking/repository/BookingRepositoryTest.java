package ru.practicum.shareit.booking.repository;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.*;
import org.springframework.data.domain.*;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static ru.practicum.shareit.booking.model.Status.*;
import static ru.practicum.shareit.booking.service.BookingServiceImpl.SORT;

@DataJpaTest
public class BookingRepositoryTest {
    private final BookingRepository bookingRepository;
    private final TestEntityManager entityManager;
    private Booking bookingWithEndBeforeAndItemId;
    private User booker;
    private Item item;

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

        bookingWithEndBeforeAndItemId = Booking.builder()
            .start(LocalDateTime.of(2021, 11, 11, 11, 11))
            .end(LocalDateTime.of(2022, 11, 11, 11, 11))
            .item(item)
            .booker(booker)
            .status(APPROVED)
            .build();
    }

    @Test
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