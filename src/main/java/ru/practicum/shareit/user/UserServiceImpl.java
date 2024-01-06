package ru.practicum.shareit.user;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.UserBadRequestException;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.Collection;

import static java.util.stream.Collectors.toList;
import static ru.practicum.shareit.user.UserMapper.toUser;
import static ru.practicum.shareit.user.UserMapper.toUserDto;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    @Override
    public Collection<UserDto> findAll() {
        return userRepository.findAll()
                .stream()
                .map(UserMapper::toUserDto)
                .collect(toList());
    }

    @Transactional(readOnly = true)
    @Override
    public UserDto findById(Long id) {
        return toUserDto(getExistingUser(id));
    }

    @Transactional
    @Override
    public UserDto save(UserDto dto) {
        if (dto.getEmail() == null || !dto.getEmail().contains("@")) {
            throw new UserBadRequestException("При создании должен быть указан корректный email.");
        }

        return toUserDto(userRepository.save(toUser(dto)));
    }

    @Transactional
    @Override
    public UserDto update(UserDto dto, Long userId) {
        User updated = getExistingUser(userId);
        updateName(updated, dto.getName());
        updateEmail(updated, dto.getEmail());

        return toUserDto(userRepository.save(updated));
    }

    @Transactional
    @Override
    public void delete(Long id) {
        userRepository.deleteById(id);
    }

    private User getExistingUser(long id) {
        return userRepository.findById(id).orElseThrow(
                () -> new UserNotFoundException("Пользователь с id " + id + " не найден.")
        );
    }

    private void updateName(User user, String newName) {
        if (newName != null && !newName.isBlank()) {
            user.setName(newName);
        }
    }

    private void updateEmail(User user, String newEmail) {
        if (newEmail != null && !newEmail.isBlank()) {
            user.setEmail(newEmail);
        }
    }
}
