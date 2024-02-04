package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.Collection;

import static java.util.stream.Collectors.toList;
import static ru.practicum.shareit.user.dto.UserMapper.toUser;
import static ru.practicum.shareit.user.dto.UserMapper.toUserDto;

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
        return toUserDto(userRepository.getExistingUser(id));
    }

    @Transactional
    @Override
    public UserDto save(UserDto dto) {
        return toUserDto(userRepository.save(toUser(dto)));
    }

    @Transactional
    @Override
    public UserDto update(UserDto dto, Long userId) {
        User updated = userRepository.getExistingUser(userId);
        updateName(updated, dto.getName());
        updateEmail(updated, dto.getEmail());
        userRepository.save(updated);

        return toUserDto(updated);
    }

    @Transactional
    @Override
    public void delete(Long id) {
        userRepository.getExistingUser(id);
        userRepository.deleteById(id);
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