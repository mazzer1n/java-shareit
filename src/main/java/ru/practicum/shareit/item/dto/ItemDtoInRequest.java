package ru.practicum.shareit.item.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ItemDtoInRequest {
    private Long id;
    private String name;
    private String description;
    private Boolean available;
    private Long requestId;
}