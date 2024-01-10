package ru.practicum.shareit.request.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import javax.validation.Valid;
import java.util.Collection;

@RestController
@RequestMapping("/requests")
@RequiredArgsConstructor
@Slf4j
public class ItemRequestController {
    private final ItemRequestService requestService;

    @PostMapping
    public ItemRequestDto save(@RequestHeader("X-Sharer-User-Id") Long userId, @Valid @RequestBody ItemRequestDto dto) {
        ItemRequestDto itemRequestDto = requestService.save(userId, dto);
        log.info("Save item request - {}", itemRequestDto);
        return itemRequestDto;
    }

    @GetMapping("/{requestId}")
    public ItemRequestDto findById(@RequestHeader("X-Sharer-User-Id") Long userId, @PathVariable Long requestId) {
        ItemRequestDto itemRequestDto = requestService.findById(userId, requestId);
        log.info("Find by id - {} item request - {} ", requestId, itemRequestDto);
        return itemRequestDto;
    }

    @GetMapping
    public Collection<ItemRequestDto> findAll(@RequestHeader("X-Sharer-User-Id") Long userId) {
        Collection<ItemRequestDto> itemRequestDtos = requestService.findAll(userId);
        log.info("find all request items: size - {}", itemRequestDtos.size());
        return itemRequestDtos;
    }

    @GetMapping("/all")
    public Collection<ItemRequestDto> findAllFromOtherUsers(@RequestHeader("X-Sharer-User-Id") Long userId,
        @RequestParam(defaultValue = "0", required = false) Integer from,
        @RequestParam(defaultValue = "10", required = false) Integer size) {
        Collection<ItemRequestDto> itemRequestDtos = requestService.findAllFromOtherUsers(userId, from, size);
        log.info("Find all items request from user id - {}: size - {}", userId, itemRequestDtos);
        return itemRequestDtos;
    }
}