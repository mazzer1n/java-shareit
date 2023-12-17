package ru.practicum.shareit.user.model;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@EqualsAndHashCode(of = "id")
@Getter
@Setter
@AllArgsConstructor
public class User {
    private Integer id;
    private String name;
    private String email;
}
