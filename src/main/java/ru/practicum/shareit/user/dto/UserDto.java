package ru.practicum.shareit.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import javax.validation.constraints.Email;

@Getter
@AllArgsConstructor
public class UserDto {
    private long id;
    private String name;
    @Email
    private String email;
}
