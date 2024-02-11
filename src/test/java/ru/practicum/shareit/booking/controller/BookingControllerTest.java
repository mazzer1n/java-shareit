package ru.practicum.shareit.booking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.ShortBookingDto;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.core.exception.exceptions.BookingBadRequestException;
import ru.practicum.shareit.core.exception.exceptions.BookingNotFoundException;
import ru.practicum.shareit.core.exception.exceptions.UserNotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = BookingController.class)
@AutoConfigureMockMvc
public class BookingControllerTest {
    private ObjectMapper objectMapper;
    private MockMvc mockMvc;
    @MockBean
    private BookingService bookingService;
    private static ShortBookingDto shortBookingDto;
    private static BookingDto bookingDto;
    private static Item item;
    private static User booker;

    @Autowired
    public BookingControllerTest(ObjectMapper objectMapper, MockMvc mockMvc, BookingService bookingServiceInterface) {
        this.objectMapper = objectMapper;
        this.mockMvc = mockMvc;
        this.bookingService = bookingServiceInterface;
    }

    @BeforeAll
    static void init() {
        shortBookingDto = ShortBookingDto.builder()
            .id(1L)
            .start(LocalDateTime.of(2026, 11, 11, 11, 11))
            .end(LocalDateTime.of(2027, 11, 11, 11, 11))
            .itemId(1L)
            .bookerId(1L)
            .build();

        item = Item.builder()
            .id(1L)
            .name("tool")
            .description("cool")
            .available(true)
            .owner(2L)
            .build();

        booker = User.builder()
            .id(1L)
            .name("user")
            .email("test@mail.ru")
            .build();

        bookingDto = BookingDto.builder()
            .id(1L)
            .start(LocalDateTime.of(2026, 11, 11, 11, 11))
            .end(LocalDateTime.of(2027, 11, 11, 11, 11))
            .item(item)
            .booker(booker)
            .build();
    }

    @Test
    void saveBooking_whenInvoked_thenStatusSuccessfulAndBookingReturned() throws Exception {
        when(bookingService.save(anyLong(), any(ShortBookingDto.class))).thenReturn(bookingDto);

        mockMvc.perform(
                post("/bookings")
                    .header("X-Sharer-User-Id", 1)
                    .content(objectMapper.writeValueAsString(shortBookingDto))
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().is2xxSuccessful())
            .andExpect(jsonPath("$.id", is(shortBookingDto.getId()), Long.class));

        verify(bookingService, times(1)).save(anyLong(), any(ShortBookingDto.class));
    }

    @Test
    void saveBooking_whenNullStart_thenExceptionReturned() throws Exception {
        ShortBookingDto bookinfWithNullStart = ShortBookingDto.builder()
            .end(LocalDateTime.of(2027, 11, 11, 11, 11))
            .itemId(1L)
            .bookerId(1L)
            .build();
        when(bookingService.save(anyLong(), any(ShortBookingDto.class))).thenThrow(BookingBadRequestException.class);

        mockMvc.perform(
                post("/bookings")
                    .header("X-Sharer-User-Id", 1)
                    .content(objectMapper.writeValueAsString(bookinfWithNullStart))
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().is4xxClientError());
    }

    @Test
    void saveBooking_whenNullEnd_thenExceptionReturned() throws Exception {
        ShortBookingDto bookingWithNullEnd = ShortBookingDto.builder()
            .start(LocalDateTime.of(2027, 11, 11, 11, 11))
            .itemId(1L)
            .bookerId(1L)
            .build();
        when(bookingService.save(anyLong(), any(ShortBookingDto.class))).thenThrow(BookingBadRequestException.class);

        mockMvc.perform(
                post("/bookings")
                    .header("X-Sharer-User-Id", 1)
                    .content(objectMapper.writeValueAsString(bookingWithNullEnd))
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().is4xxClientError());
    }

    @Test
    void saveBooking_whenEndInPast_thenExceptionReturned() throws Exception {
        ShortBookingDto bookingWithEndInPast = ShortBookingDto.builder()
            .start(LocalDateTime.of(2027, 11, 11, 11, 11))
            .end(LocalDateTime.of(2020, 11, 11, 11, 11))
            .itemId(1L)
            .bookerId(1L)
            .build();
        when(bookingService.save(anyLong(), any(ShortBookingDto.class))).thenThrow(BookingBadRequestException.class);

        mockMvc.perform(
                post("/bookings")
                    .header("X-Sharer-User-Id", 1)
                    .content(objectMapper.writeValueAsString(bookingWithEndInPast))
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().is4xxClientError());
    }

    @Test
    void saveBooking_whenStartInPast_thenExceptionReturned() throws Exception {
        ShortBookingDto bookingWithStartInPast = ShortBookingDto.builder()
            .end(LocalDateTime.of(2027, 11, 11, 11, 11))
            .start(LocalDateTime.of(2020, 11, 11, 11, 11))
            .itemId(1L)
            .bookerId(1L)
            .build();
        when(bookingService.save(anyLong(), any(ShortBookingDto.class))).thenThrow(BookingBadRequestException.class);

        mockMvc.perform(
                post("/bookings")
                    .header("X-Sharer-User-Id", 1)
                    .content(objectMapper.writeValueAsString(bookingWithStartInPast))
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().is4xxClientError());
    }

    @Test
    public void findByUserIdAndState_whenInvoked_thenStatus200andReturnBookingList() throws Exception {
        List<BookingDto> expectedBookings = List.of(bookingDto);

        when(bookingService.findByUserIdAndState(anyLong(), anyString(), any(Pageable.class)))
                .thenReturn(expectedBookings);

        mockMvc.perform(
                        get("/bookings")
                                .header("X-Sharer-User-Id", "1")
                                .param("state", "ALL"))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(List.of(bookingDto))));

        verify(bookingService, times(1)).findByUserIdAndState(anyLong(), anyString(), any(Pageable.class));
    }


    @Test
    public void findBooking_whenExist_thenStatus200andBookingReturned() throws Exception {
        when(bookingService.findById(anyLong(), anyLong())).thenReturn(bookingDto);

        mockMvc.perform(
                get("/bookings/{bookingId}", 1)
                    .header("X-Sharer-User-Id", 1))
            .andExpect(status().isOk())
            .andExpect(content().json(objectMapper.writeValueAsString(bookingDto)));

        verify(bookingService, times(1)).findById(anyLong(), anyLong());
    }

    @Test
    public void findBooking_whenNotExist_thenThrowNotFound() throws Exception {
        when(bookingService.findById(anyLong(), anyLong())).thenThrow(BookingNotFoundException.class);

        mockMvc.perform(get("/bookings/{bookingId}", 1L)
                .header("X-Sharer-User-Id", 1))
            .andExpect(status().isNotFound());
    }

    @Test
    public void findBookingsByItemOwnerId_whenInvoked_thenStatus200andReturnBookingList() throws Exception {
        List<BookingDto> expectedBookings = List.of(bookingDto);
        when(bookingService.findBookingsByItemOwnerId(anyLong(), anyString(), any(Pageable.class))).thenReturn(expectedBookings);

        mockMvc.perform(
                        get("/bookings/owner")
                                .header("X-Sharer-User-Id", 1)
                                .param("state", "ALL"))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(List.of(bookingDto))));

        verify(bookingService, times(1)).findBookingsByItemOwnerId(anyLong(), anyString(), any(Pageable.class));
    }

    @Test
    public void updateBooking_thenStatus200andUpdatedReturns() throws Exception {
        when(bookingService.approve(anyLong(), anyLong(), anyBoolean())).thenReturn(bookingDto);

        mockMvc.perform(
                patch("/bookings/1")
                    .header("X-Sharer-User-Id", 1)
                    .param("approved", "true")
                    .content(objectMapper.writeValueAsString(true))
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value(bookingDto.getStatus()));

        verify(bookingService, times(1)).approve(anyLong(), anyLong(), anyBoolean());
    }

    @Test
    public void updateBooking_whenNotOwner_thenExceptionReturns() throws Exception {
        when(bookingService.approve(anyLong(), anyLong(), anyBoolean())).thenThrow(UserNotFoundException.class);

        mockMvc.perform(
                patch("/bookings/1")
                    .header("X-Sharer-User-Id", 2)
                    .param("approved", "true")
                    .content(objectMapper.writeValueAsString(true))
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }
}