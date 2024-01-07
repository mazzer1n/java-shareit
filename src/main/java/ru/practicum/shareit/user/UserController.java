package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.group.Marker;
import ru.practicum.shareit.user.dto.UserDto;

import javax.validation.Valid;
import java.util.Collection;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping
    public Collection<UserDto> findAll() {
        return userService.findAll();
    }

    @GetMapping("/{id}")
    public UserDto findById(@PathVariable Long id) {
        return userService.findById(id);
    }

    @Validated(Marker.OnCreate.class)
    @PostMapping
    public UserDto save(@RequestBody UserDto dto) {
        return userService.save(dto);
    }

    @Validated(Marker.OnUpdate.class)
    @PatchMapping("/{userId}")
    public UserDto update(@RequestBody UserDto dto, @PathVariable Long userId) {
        return userService.update(dto, userId);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        userService.delete(id);
    }
}


