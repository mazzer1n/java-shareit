package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.Collection;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserStorage userStorage;

    @Override
    public Collection<UserDto> getAll() {
        return userStorage.getAll().stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
    }

    @Override
    public UserDto get(int id) {
        return UserMapper.toUserDto(userStorage.get(id));
    }

    @Override
    public UserDto create(UserDto dto) {
        return UserMapper.toUserDto(userStorage.create(dto));
    }

    @Override
    public UserDto update(Integer userId, UserDto dto) {
        return UserMapper.toUserDto(userStorage.update(userId, dto));
    }

    @Override
    public void delete(int id) {
        userStorage.delete(id);
    }
}
