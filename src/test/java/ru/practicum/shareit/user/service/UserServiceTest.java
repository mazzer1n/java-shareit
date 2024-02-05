package ru.practicum.shareit.user.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private long userId;
    private User expectedUser;

    @Captor
    private ArgumentCaptor<User> captor;

    @BeforeEach
    public void setUp() {
        userId = 1L;
        expectedUser = new User(userId, "test", "test@mail.ru");
    }

    @Test
    void saveUser_whenUserEmailValid_thenUserReturned() {
        when(userRepository.save(expectedUser)).thenReturn(expectedUser);

        UserDto actual = userService.save(UserMapper.toUserDto(expectedUser));

        assertEquals(expectedUser.getId(), actual.getId());
        assertEquals(expectedUser.getName(), actual.getName());
        assertEquals(expectedUser.getEmail(), actual.getEmail());
        verify(userRepository).save(expectedUser);
    }

    @Test
    void findUsers_whenUsersFound_thenUserListReturned() {
        List<User> users = List.of(expectedUser);
        when(userRepository.findAll()).thenReturn(users);

        List<User> actualUsers = userService.findAll().stream().map(UserMapper::toUser).collect(Collectors.toList());

        assertEquals(users, actualUsers);
        assertEquals(1, actualUsers.size());
    }

    @Test
    void findUsers_whenEmptyList_thenEmptyListReturned() {
        List<User> users = List.of();
        when(userRepository.findAll()).thenReturn(users);

        List<User> actualUsers = userService.findAll().stream().map(UserMapper::toUser).collect(Collectors.toList());

        assertEquals(users, actualUsers);
        assertTrue(actualUsers.isEmpty());
    }

}


