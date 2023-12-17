package ru.practicum.shareit.user;

import ru.practicum.shareit.user.dto.UserDto;

import java.util.Collection;

public interface UserService {
    Collection<UserDto> getAll();

    UserDto get(int id);

    UserDto create(UserDto dto);

    UserDto update(Integer userId, UserDto dto);

    void delete(int id);
}
