package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.Collection;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemStorage itemStorage;

    @Override
    public ItemDto add(Integer userId, ItemDto dto) {
        return ItemMapper.toItemDto(itemStorage.add(userId, dto));
    }

    @Override
    public ItemDto update(Integer userId, Integer itemId, ItemDto dto) {
        return ItemMapper.toItemDto(itemStorage.update(userId, itemId, dto));
    }

    @Override
    public ItemDto get(Integer userId, Integer itemId) {
        return ItemMapper.toItemDto(itemStorage.get(userId, itemId));
    }

    @Override
    public Collection<ItemDto> getAll(Integer userId) {
        return itemStorage.getAll(userId).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public Collection<ItemDto> search(Integer userId, String text) {
        return itemStorage.search(userId, text).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }
}
