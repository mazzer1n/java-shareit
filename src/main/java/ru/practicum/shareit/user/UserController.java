package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.group.Marker;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.Collection;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {
    private final UserService userService;

    @GetMapping
    public Collection<UserDto> findAll() {
        Collection<UserDto> users = userService.findAll();
        log.info("total {} users found", users.size());
        return users;
    }

    @GetMapping("/{id}")
    public UserDto findById(@PathVariable Long id) {
        UserDto userDto = userService.findById(id);
        log.info("Find user by id - {}: {}", id, userDto);
        return userDto;
    }

    @Validated(Marker.OnCreate.class)
    @PostMapping
    public UserDto save(@RequestBody UserDto dto) {
        UserDto userDto = userService.save(dto);
        log.info("Save user: {}", userDto);
        return userDto;
    }

    @Validated(Marker.OnUpdate.class)
    @PatchMapping("/{userId}")
    public UserDto update(@RequestBody UserDto dto, @PathVariable Long userId) {
        UserDto userDto = userService.update(dto, userId);
        log.info("Update user: {}", userDto);
        return userDto;
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        userService.delete(id);
        log.info("Delete user: id - {}", id);
    }
}


