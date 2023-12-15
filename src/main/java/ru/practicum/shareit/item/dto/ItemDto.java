package ru.practicum.shareit.item.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.practicum.shareit.group.Marker;
import ru.practicum.shareit.request.ItemRequest;

import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
public class ItemDto {
    @NotNull(groups = Marker.OnUpdate.class)
    private long id;
    @NotNull(groups = Marker.OnCreate.class)
    @NotNull(groups = Marker.OnUpdate.class)
    private String name;
    private String description;
    private Boolean available;
    private ItemRequest request;
}