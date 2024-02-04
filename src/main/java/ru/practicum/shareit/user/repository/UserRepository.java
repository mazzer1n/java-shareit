package ru.practicum.shareit.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.core.exception.exceptions.UserNotFoundException;
import ru.practicum.shareit.user.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    default User getExistingUser(long id) {
        return findById(id)
                .orElseThrow(() -> new UserNotFoundException("Пользователь с id " + id + " не найден.")
        );
    }
}