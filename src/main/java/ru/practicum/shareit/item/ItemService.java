package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.ItemDto;

import java.util.Collection;

public interface ItemService {
    ItemDto add(Integer userId, ItemDto dto);

    ItemDto update(Integer userId, Integer itemId, ItemDto dto);

    ItemDto get(Integer userId, Integer itemId);

    Collection<ItemDto> getAll(Integer userId);

    Collection<ItemDto> search(Integer userId, String text);
}
