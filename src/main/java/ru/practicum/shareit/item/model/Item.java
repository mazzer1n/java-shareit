package ru.practicum.shareit.item.model;

import lombok.Builder;
import ru.practicum.shareit.request.ItemRequest;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
@Builder
@AllArgsConstructor
public class Item {
    private Integer id;
    @NotEmpty
    private String name;
    @NotEmpty
    private String description;
    @NotNull
    private Boolean available;
    @NotNull
    private Integer owner;
    private ItemRequest request;
}
