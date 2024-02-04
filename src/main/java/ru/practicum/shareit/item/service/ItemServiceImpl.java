package ru.practicum.shareit.item.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.core.exception.exceptions.UserNotFoundException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentMapper;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoInRequest;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.CommentRepository;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

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
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final ItemRequestRepository requestRepository;

    @Autowired
    public ItemServiceImpl(ItemRepository itemRepository, CommentRepository commentRepository, UserRepository userRepository,
                           @Lazy BookingRepository bookingRepository, @Lazy ItemRequestRepository requestRepository) {
        this.itemRepository = itemRepository;
        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
        this.bookingRepository = bookingRepository;
        this.requestRepository = requestRepository;
    }

    @Transactional
    @Override
    public ItemDto save(Long userId, ItemDto dto) {
        userRepository.getExistingUser(userId);

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
        userRepository.getExistingUser(userId);
        Item item = getExistingItem(itemId);
        ItemDto result = toItemDto(item);
        fillItemWithComments(result, itemId);

        if (item.getOwner().equals(userId)) {
            bookingRepository.fillItemWithBookings(result);
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

    public Item getExistingItem(long id) {
        return itemRepository.getExistingItem(id);
    }

    private void setRequestWhenCreateItem(Item item, ItemDto dto) {
        if (dto.getRequestId() != null) {
            Long requestId = dto.getRequestId();
            ItemRequest request = requestRepository.getExistingRequest(requestId);
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
        bookingRepository.fillItemWithBookings(result);

        return result;
    }

    @Transactional
    @Override
    public CommentDto saveComment(Long userId, Long itemId, CommentDto dto) {
        User user = userRepository.getExistingUser(userId);
        Item item = getExistingItem(itemId);
        bookingRepository.validateBookingsToAddComment(userId, itemId);

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
}