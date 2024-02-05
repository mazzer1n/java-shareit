package ru.practicum.shareit.request.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.core.exception.exceptions.PaginationBadRequestException;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.RequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ItemRequestServiceTest {

    @InjectMocks
    private ItemRequestServiceImpl requestService;

    @Mock
    private ItemRequestRepository requestRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ItemRepository itemRepository;

    private long requestId = 1L;
    private long userId = 1L;
    private ItemRequest expectedRequest;

    @BeforeEach
    void setUp() {
        expectedRequest = new ItemRequest();
        expectedRequest.setId(requestId);
        expectedRequest.setDescription("Test Request");
    }

    @Test
    void saveRequest_whenInvoked_thenRequestReturned() {
        when(requestRepository.save(any())).thenReturn(expectedRequest);

        ItemRequestDto actual = requestService.save(userId, RequestMapper.toRequestDto(expectedRequest));

        assertEquals(expectedRequest.getId(), actual.getId());
        assertEquals(expectedRequest.getDescription(), actual.getDescription());
        verify(requestRepository).save(any(ItemRequest.class));
    }

    @Test
    void findRequests_whenRequestsFound_thenRequestListReturned() {
        expectedRequest.setRequester(null);
        List<ItemRequest> requests = List.of(expectedRequest);
        when(requestRepository.findByRequesterId(1L, Sort.by("created").descending()))
                .thenReturn(requests);

        List<ItemRequest> actualRequests = requestService.findAll(userId)
                .stream()
                .map(RequestMapper::toRequest)
                .collect(Collectors.toList());

        assertEquals(requests, actualRequests);
        assertEquals(1, actualRequests.size());
        verify(requestRepository, times(1))
                .findByRequesterId(userId, Sort.by("created").descending());
    }

    @Test
    void findRequests_whenEmptyList_thenEmptyListReturned() {
        List<ItemRequest> requests = List.of();
        when(requestRepository.findByRequesterId(1L, Sort.by("created").descending()))
                .thenReturn(requests);

        List<ItemRequest> actualRequests = requestService.findAll(userId)
                .stream()
                .map(RequestMapper::toRequest)
                .collect(Collectors.toList());

        assertEquals(requests, actualRequests);
        assertTrue(actualRequests.isEmpty());
        verify(requestRepository, times(1))
                .findByRequesterId(userId, Sort.by("created").descending());
    }

    @Test
    void findRequestsFromOtherUsers_whenRequestsFound_thenRequestListReturned() {
        expectedRequest.setRequester(null);
        List<ItemRequest> requests = List.of(expectedRequest);
        Pageable pageable = PageRequest.of(0, 10, Sort.by("created").descending());
        when(requestRepository.findByRequesterIdIsNot(1L, pageable)).thenReturn(requests);

        List<ItemRequest> actualRequests = requestService.findAllFromOtherUsers(userId, 0, 10)
                .stream()
                .map(RequestMapper::toRequest)
                .collect(Collectors.toList());

        assertEquals(requests, actualRequests);
        assertEquals(1, actualRequests.size());
        verify(requestRepository, times(1)).findByRequesterIdIsNot(userId, pageable);
    }

    @Test
    void findRequestsFromOtherUsers_whenIncorrectPagination_thenExceptionReturned() {
        assertThrows(PaginationBadRequestException.class,
                () -> requestService.findAllFromOtherUsers(2L, -1, 0));
    }
}