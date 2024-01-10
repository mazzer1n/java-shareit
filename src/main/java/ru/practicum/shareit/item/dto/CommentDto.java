package ru.practicum.shareit.item.dto;

import lombok.*;
import ru.practicum.shareit.group.Marker;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class CommentDto {
    private Long id;
    @NotBlank(groups = Marker.OnCreate.class)
    private String text;
    @NotBlank(groups = Marker.OnCreate.class)
    private String authorName;
    private LocalDateTime created;
}
