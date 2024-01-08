package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.group.Marker;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.ArrayList;
import java.util.Collection;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
@Slf4j
public class ItemController {
    private final ItemService itemService;

    @Validated(Marker.OnCreate.class)
    @PostMapping
    public ItemDto save(@RequestHeader("X-Sharer-User-Id") Long userId, @RequestBody ItemDto dto) {
        ItemDto itemDto = itemService.save(userId, dto);
        log.info("Item saved: {}", dto);
        return itemDto;
    }

    @Validated(Marker.OnCreate.class)
    @PostMapping("/{itemId}/comment")
    public CommentDto save(@RequestHeader("X-Sharer-User-Id") Long userId,
                           @PathVariable Long itemId,
                           @RequestBody CommentDto dto) {
        CommentDto commentDto = itemService.saveComment(userId, itemId, dto);
        log.info("Comment saved: {}", commentDto);
        return commentDto;
    }

    @Validated(Marker.OnUpdate.class)
    @PatchMapping("/{itemId}")
    public ItemDto update(@RequestHeader("X-Sharer-User-Id") Long userId,
                          @PathVariable Long itemId,
                          @RequestBody ItemDto dto) {
        ItemDto itemDto = itemService.update(userId, itemId, dto);
        log.info("Item updated: {}", itemDto);
        return itemDto;
    }

    @GetMapping("/{itemId}")
    public ItemDto findById(@RequestHeader("X-Sharer-User-Id") Long userId, @PathVariable Long itemId) {
        ItemDto itemDto = itemService.findById(userId, itemId);
        log.info("Get item by id: {}", itemDto);
        return itemDto;
    }

    @GetMapping
    public Collection<ItemDto> findAll(@RequestHeader("X-Sharer-User-Id") Long userId) {
        Collection<ItemDto> items = itemService.findAll(userId);
        log.info("All items by user id: size - {}", items.size());
        return items;
    }

    @GetMapping("/search")
    public Collection<ItemDto> search(@RequestHeader("X-Sharer-User-Id") Long userId, @RequestParam String text) {
        if (text == null || text.isBlank()) {
            return new ArrayList<>();
        }
        Collection<ItemDto> items = itemService.search(userId, text);
        log.info("{} found items", items.size());
        return items;
    }
}
