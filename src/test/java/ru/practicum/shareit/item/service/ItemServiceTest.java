package ru.practicum.shareit.item.service;

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
import ru.practicum.shareit.booking.service.BookingServiceImpl;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.CommentRepository;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.request.service.ItemRequestServiceImpl;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.service.UserServiceImpl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ItemServiceTest {
    @Mock
    private UserServiceImpl userService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private BookingServiceImpl bookingService;
    @Mock
    private ItemRequestServiceImpl requestService;
    @InjectMocks
    private ItemServiceImpl itemService;
    private long itemId;
    private long userId;
    private Item expectedItem;
    private User user;
    private UserDto userDto;
    private User notOwner;
    private UserDto notOwnerDto;
    private Comment expectedComment;
    @Captor
    private ArgumentCaptor<Item> captor;

    @BeforeEach
    public void init() {
        userId = 1L;
        user = new User(userId, "test", "test@mail.ru");
        userDto = UserMapper.toUserDto(user);

        itemId = 1L;
        expectedItem = new Item(itemId, "tool", "cool tool", true, userId, null);

        notOwner = new User(2L, "fake", "fake@mail.ru");
        notOwnerDto = UserMapper.toUserDto(notOwner);

        expectedComment = Comment.builder()
                .id(1L)
                .text("cool")
                .item(expectedItem)
                .author(user)
                .created(LocalDateTime.now())
                .build();
    }

    @Test
    void saveItem_whenInvoked_thenItemReturned() {
        when(itemRepository.save(any())).thenReturn(expectedItem);

        ItemDto actual = itemService.save(userId, ItemMapper.toItemDto(expectedItem));

        assertEquals(expectedItem.getId(), actual.getId());
        assertEquals(expectedItem.getName(), actual.getName());
        assertEquals(expectedItem.getDescription(), actual.getDescription());
        assertEquals(expectedItem.getAvailable(), actual.getAvailable());
        verify(itemRepository).save(any(Item.class));
    }

    @Test
    void findItems_whenEmptyList_thenEmptyListReturned() {
        Pageable pageable = PageRequest.of(0, 10);
        List<Item> items = List.of();
        when(itemRepository.findByOwner(1L, pageable)).thenReturn(items);

        List<Item> actualItems = itemService.findAll(userId, 0, 10)
                .stream()
                .map(ItemMapper::toItem)
                .collect(Collectors.toList());

        assertEquals(items, actualItems);
        assertTrue(actualItems.isEmpty());
        verify(itemRepository, times(1)).findByOwner(userId, pageable);
    }

    @Test
    void search_whenTextIsNull_thenEmptyListReturned() {
        List<Item> items = new ArrayList<>();

        List<Item> actualItems = itemService.search(userId, null, 0, 10)
                .stream()
                .map(ItemMapper::toItem)
                .collect(Collectors.toList());

        assertEquals(items, actualItems);
        assertTrue(actualItems.isEmpty());
    }

}