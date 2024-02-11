package ru.practicum.shareit.item.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.service.BookingServiceImpl;
import ru.practicum.shareit.core.exception.exceptions.ItemNotFoundException;
import ru.practicum.shareit.core.exception.exceptions.UserNotFoundException;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.CommentRepository;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.service.ItemRequestServiceImpl;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserServiceImpl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.shareit.item.dto.CommentMapper.toComment;
import static ru.practicum.shareit.item.dto.CommentMapper.toCommentDto;
import static ru.practicum.shareit.item.dto.ItemMapper.toItem;
import static ru.practicum.shareit.item.dto.ItemMapper.toItemDto;

@Service
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final CommentRepository commentRepository;
    private final UserServiceImpl userService;
    private final BookingServiceImpl bookingService;
    private final ItemRequestServiceImpl requestService;

    @Autowired
    public ItemServiceImpl(ItemRepository itemRepository, CommentRepository commentRepository, UserServiceImpl userService,
                           @Lazy BookingServiceImpl bookingService, @Lazy ItemRequestServiceImpl requestService) {
        this.itemRepository = itemRepository;
        this.commentRepository = commentRepository;
        this.userService = userService;
        this.bookingService = bookingService;
        this.requestService = requestService;
    }

    @Transactional
    @Override
    public ItemDto save(Long userId, ItemDto dto) {
        userService.getExistingUser(userId);

        Item item = toItem(dto);
        item.setOwner(userId);
        setRequestWhenCreateItem(item, dto);
        item = itemRepository.save(item);

        return toItemDto(item);
    }

    @Transactional
    @Override
    public ItemDto update(Long userId, Long itemId, ItemDto dto) {
        Item item = getExistingItem(itemId);
        if (!item.getOwner().equals(userId)) {
            throw new UserNotFoundException("Id пользователя не совпадает.");
        }

        updateItemProperties(item, dto);
        item = itemRepository.save(item);

        return fillItemWithCommentsAndBookings(item);
    }

    @Transactional(readOnly = true)
    @Override
    public ItemDto findById(Long userId, Long itemId) {
        userService.getExistingUser(userId);
        Item item = getExistingItem(itemId);
        ItemDto result = toItemDto(item);
        fillItemWithComments(result, itemId);

        if (item.getOwner().equals(userId)) {
            bookingService.fillItemWithBookings(result);
            return result;
        }

        return result;
    }

    @Transactional(readOnly = true)
    @Override
    public Collection<ItemDto> findAll(Long userId, int from, int size) {
        Pageable pageable = PageRequest.of(from / size, size);
        List<ItemDto> result = new ArrayList<>();
        List<Item> items = itemRepository.findByOwner(userId, pageable);

        for (Item item : items) {
            result.add(fillItemWithCommentsAndBookings(item));
        }

        return result;
    }

    @Transactional(readOnly = true)
    @Override
    public Collection<ItemDto> search(Long userId, String text, int from, int size) {
        Pageable pageable = PageRequest.of(from / size, size);

        return itemRepository.search(text, pageable)
                .stream()
                .map(this::fillItemWithCommentsAndBookings)
                .collect(Collectors.toList());
    }

    private void setRequestWhenCreateItem(Item item, ItemDto dto) {
        if (dto.getRequestId() != null) {
            Long requestId = dto.getRequestId();
            ItemRequest request = requestService.getExistingRequest(requestId);
            item.setRequest(request);
        }
    }

    private void updateItemProperties(Item item, ItemDto dto) {
        if (dto.getAvailable() != null) {
            item.setAvailable(dto.getAvailable());
        }

        if (dto.getName() != null && !dto.getName().isBlank()) {
            item.setName(dto.getName());
        }

        if (dto.getDescription() != null && !dto.getDescription().isBlank()) {
            item.setDescription(dto.getDescription());
        }
    }

    private ItemDto fillItemWithCommentsAndBookings(Item item) {
        ItemDto result = toItemDto(item);
        fillItemWithComments(result, item.getId());
        bookingService.fillItemWithBookings(result);

        return result;
    }

    @Transactional
    @Override
    public CommentDto saveComment(Long userId, Long itemId, CommentDto dto) {
        User user = userService.getExistingUser(userId);
        Item item = getExistingItem(itemId);
        bookingService.validateBookingsToAddComment(userId, itemId);

        Comment comment = toComment(dto);
        comment.setCreated(LocalDateTime.now());
        comment.setItem(item);
        comment.setAuthor(user);

        return toCommentDto(commentRepository.save(comment));
    }

    public void fillItemWithComments(ItemDto result, Long itemId) {
        List<Comment> comments = commentRepository.findAllByItemId(itemId);
        if (!comments.isEmpty()) {
            result.setComments(comments.stream()
                    .map(CommentMapper::toCommentDto)
                    .collect(Collectors.toList()));
        } else {
            result.setComments(new ArrayList<>());
        }
    }

    public List<ItemDtoInRequest> getItemsByRequestId(long id) {
        return itemRepository.findByRequestId(id)
                .stream().map(ItemMapper::toItemDtoInRequest)
                .collect(Collectors.toList());
    }

    public boolean hasUserZeroItems(long userId) {
        return itemRepository.findAll()
                .stream()
                .filter(item -> item.getOwner().equals(userId))
                .findAny().isEmpty();
    }

    public Item getExistingItem(long id) {
        return itemRepository.findById(id).orElseThrow(
                () -> new ItemNotFoundException("Товар с id " + id + " не найден.")
        );
    }
}