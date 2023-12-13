package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;

import javax.validation.Valid;
import java.util.Collection;


@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {
    private final UserService userService;

    @GetMapping
    public Collection<UserDto> getAll() {
        Collection<UserDto> users = userService.getAll();
        log.info("Get all users, size={}", users.size());
        return users;
    }

    @GetMapping("/{id}")
    public UserDto get(@PathVariable int id) {
        UserDto userDto = userService.get(id);
        log.info("Get user by id={} user={}", id, userDto);
        return userDto;
    }

    @PostMapping
    public UserDto create(@Valid @RequestBody UserDto dto) {
        UserDto userDto = userService.create(dto);
        log.info("Create user={}", userDto);
        return userDto;
    }

    @PatchMapping("/{userId}")
    public UserDto update(@PathVariable int userId, @Valid @RequestBody UserDto dto) {
        UserDto userDto = userService.update(userId, dto);
        log.info("Update user by id={} user={}", userId, userDto);
        return userDto;
    }

    @DeleteMapping("/{id}")
    public boolean delete(@PathVariable int id) {
        log.info("Remove user by id={}", id);
        return userService.delete(id);
    }
}

