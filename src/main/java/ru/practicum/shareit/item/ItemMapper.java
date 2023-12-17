package ru.practicum.shareit.item;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;

@UtilityClass
public class ItemMapper {
    public static ItemDto toItemDto(Item item) {
        return new ItemDto(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getAvailable(),
                item.getRequest()
        );
    }

    public static Item toItem(Integer itemId, Integer ownerId, ItemDto dto) {
        return new Item(
                itemId,
                dto.getName(),
                dto.getDescription(),
                dto.getAvailable(),
                ownerId,
                dto.getRequest()
        );
    }
}
