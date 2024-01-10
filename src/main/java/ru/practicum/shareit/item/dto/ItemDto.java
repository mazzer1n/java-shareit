package ru.practicum.shareit.item.dto;

import lombok.*;
import ru.practicum.shareit.booking.dto.ShortBookingDto;
import ru.practicum.shareit.group.Marker;
import ru.practicum.shareit.request.ItemRequest;

import javax.validation.constraints.NotNull;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class ItemDto {
    @NotNull(groups = Marker.OnUpdate.class)
    private Long id;
    @NotNull(groups = Marker.OnCreate.class)
    @NotNull(groups = Marker.OnUpdate.class)
    private String name;
    private String description;
    private Boolean available;
    private ItemRequest request;
    private List<CommentDto> comments;
    private ShortBookingDto lastBooking;
    private ShortBookingDto nextBooking;
}