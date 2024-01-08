package ru.practicum.shareit.item;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.exception.CommentBadRequestException;
import ru.practicum.shareit.exception.ItemBadRequestException;
import ru.practicum.shareit.exception.ItemNotFoundException;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.shareit.booking.dto.BookingMapper.toShortBookingDto;
import static ru.practicum.shareit.item.CommentMapper.toComment;
import static ru.practicum.shareit.item.CommentMapper.toCommentDto;
import static ru.practicum.shareit.item.ItemMapper.toItem;
import static ru.practicum.shareit.item.ItemMapper.toItemDto;


@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final BookingRepository bookingRepository;

    @Transactional
    @Override
    public ItemDto save(Long userId, ItemDto dto) {
        validateItemProperties(dto);
        getExistingUser(userId);

        Item item = toItem(dto);
        item.setOwner(userId);
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
        getExistingUser(userId);
        Item item = getExistingItem(itemId);
        ItemDto result = toItemDto(item);
        fillComments(result, itemId);

        if (item.getOwner().equals(userId)) {
            fillBookings(result);
            return result;
        }

        return result;
    }

    @Transactional(readOnly = true)
    @Override
    public Collection<ItemDto> findAll(Long userId) {
        List<Item> items = itemRepository.findAll();
        List<ItemDto> result = new ArrayList<>();

        for (Item item : items) {
            if (item.getOwner().equals(userId)) {
                result.add(fillItemWithCommentsAndBookings(item));
            }
        }

        return result;
    }

    @Transactional(readOnly = true)
    @Override
    public Collection<ItemDto> search(Long userId, String text) {
        return itemRepository.search(text)
                .stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }


    @Transactional
    @Override
    public CommentDto saveComment(Long userId, Long itemId, CommentDto dto) {
        validateCommentText(dto.getText());
        User user = getExistingUser(userId);
        Item item = getExistingItem(itemId);

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

        Comment comment = toComment(dto);
        comment.setCreated(LocalDateTime.now());
        comment.setItem(item);
        comment.setAuthor(user);

        return toCommentDto(commentRepository.save(comment));
    }

    private void validateCommentText(String text) {
        if (text.isEmpty()) {
            throw new CommentBadRequestException("Комментарий не может быть пустым.");
        }
    }

    private User getExistingUser(long id) {
        return userRepository.findById(id).orElseThrow(
                () -> new UserNotFoundException("Пользователь с id " + id + " не найден.")
        );
    }

    private Item getExistingItem(long id) {
        return itemRepository.findById(id).orElseThrow(
                () -> new ItemNotFoundException("Товар с id " + id + " не найден.")
        );
    }

    private void validateItemProperties(ItemDto dto) {
        if (dto.getName() == null || dto.getName().isBlank()) {
            throw new ItemBadRequestException("Не указано имя.");
        }

        if (dto.getDescription() == null || dto.getDescription().isBlank()) {
            throw new ItemBadRequestException("Не указано описание.");
        }

        if (dto.getAvailable() == null) {
            throw new ItemBadRequestException("Не указана доступность товара.");
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
        fillComments(result, item.getId());
        fillBookings(result);

        return result;
    }

    private void fillComments(ItemDto result, Long itemId) {
        List<Comment> comments = commentRepository.findAllByItemId(itemId);
        if (!comments.isEmpty()) {
            result.setComments(comments.stream()
                    .map(CommentMapper::toCommentDto)
                    .collect(Collectors.toList()));
        } else {
            result.setComments(new ArrayList<>());
        }
    }

    private void fillBookings(ItemDto result) {
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
