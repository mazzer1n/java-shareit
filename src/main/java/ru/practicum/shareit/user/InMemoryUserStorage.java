package ru.practicum.shareit.user;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exception.AlreadyExistException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.UserValidationException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.Collection;
import java.util.HashMap;

@Repository
public class InMemoryUserStorage implements UserStorage {
    private final HashMap<Integer, User> users = new HashMap<>();
    private static int userId = 0;

    public static Integer increaseUserId() {
        return InMemoryUserStorage.userId += 1;
    }

    @Override
    public Collection<User> getAll() {
        return users.values();
    }

    @Override
    public User get(int id) {
        if (!users.containsKey(id)) {
            throw new NotFoundException("Пользователь с id " + id + " не найден.");
        }

        return users.get(id);
    }

    @Override
    public User create(UserDto dto) {
        validateEmailAlreadyExist(dto);

        Integer id = increaseUserId();
        User user = UserMapper.toUser(id, dto);
        users.put(id, user);

        return user;
    }

    @Override
    public User update(Integer id, UserDto dto) {
        User user = users.get(id);
        updateUserData(user, dto);
        users.put(user.getId(), user);

        return user;
    }

    @Override
    public void delete(int id) {
        users.remove(id);
    }

    private void validateEmailAlreadyExist(UserDto userDto) {
        if (userDto.getEmail() == null) {
            throw new UserValidationException(
                    "Электронный адрес должен присутствовать при создании пользователя."
            );
        }

        for (User user : users.values()) {
            if (user.getEmail().equals(userDto.getEmail())) {
                throw new AlreadyExistException(
                        "Пользователь с электронным адресом " + userDto.getEmail() + " уже существует."
                );
            }
        }
    }

    private void updateUserData(User user, UserDto dto) {
        if (dto.getName() != null) {
            user.setName(dto.getName());
        }

        String newEmail = dto.getEmail();
        if (newEmail != null && emailExists(newEmail, user.getId())) {
            throw new AlreadyExistException(
                    "Пользователь с электронным адресом " + newEmail + " уже существует."
            );
        }

        if (newEmail != null) {
            user.setEmail(newEmail);
        }
    }

    private boolean emailExists(String newEmail, Integer userId) {
        return users.values().stream()
                .anyMatch(value -> value.getEmail().equals(newEmail) && !value.getId().equals(userId));
    }

}
