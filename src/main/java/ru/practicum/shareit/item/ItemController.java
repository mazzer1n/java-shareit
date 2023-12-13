package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.Collection;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
@Slf4j
public class ItemController {
    private final ItemService itemService;

    @PostMapping
    public ItemDto add(@RequestHeader("X-Sharer-User-Id") Integer userId, @RequestBody ItemDto dto) {
        ItemDto itemDto = itemService.add(userId, dto);
        log.info("Create item by userId={} item={}", userId, itemDto);
        return itemDto;
    }

    @PatchMapping("/{itemId}")
    public ItemDto update(@RequestHeader("X-Sharer-User-Id") Integer userId,
                       @PathVariable Integer itemId,
                       @RequestBody ItemDto dto) {
        ItemDto itemDto = itemService.update(userId, itemId, dto);
        log.info("Update item by userId={} item={}", userId, itemDto);
        return itemDto;
    }

    @GetMapping("/{itemId}")
    public ItemDto get(@RequestHeader("X-Sharer-User-Id") Integer userId, @PathVariable Integer itemId) {
        ItemDto itemDto = itemService.get(userId,itemId);
        log.info("Create item by userId={} item={}", userId, itemDto);
        return itemDto;
    }

    @GetMapping
    public Collection<ItemDto> getAll(@RequestHeader("X-Sharer-User-Id") Integer userId) {
        Collection<ItemDto> itemsDto = itemService.getAll(userId);
        log.info("Get items by userId={} size={}", userId, itemsDto.size());
        return itemsDto;
    }

    @GetMapping("/search")
    public Collection<ItemDto> search(@RequestHeader("X-Sharer-User-Id") Integer userId, @RequestParam String text) {
        Collection<ItemDto> itemsDto = itemService.search(userId, text);
        log.info("Get items by userId={} size={}", userId, itemsDto.size());
        return itemsDto;
    }
}
