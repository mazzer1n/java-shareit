package ru.practicum.shareit.user.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import ru.practicum.shareit.group.Marker;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;

@Getter
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
public class UserDto {
    @NotNull(groups = Marker.OnUpdate.class)
    private long id;
    @NotNull(groups = Marker.OnCreate.class)
    private String name;
    @Email(groups = Marker.OnCreate.class)
    @Email(groups = Marker.OnUpdate.class)
    private String email;
}
