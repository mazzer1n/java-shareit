package ru.practicum.shareit.user.model;

import lombok.*;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;

@EqualsAndHashCode(of = "id")
@Getter
@Setter
@AllArgsConstructor
public class User {
    private Integer id;
    @NotNull
    private String name;
    @NotNull
    @Email
    private String email;
}
