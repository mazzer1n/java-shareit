package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.core.validation.PaginationValidator;
import ru.practicum.shareit.item.dto.ItemDtoInRequest;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.request.dto.*;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.shareit.request.dto.RequestMapper.*;

@Service
@RequiredArgsConstructor
public class ItemRequestServiceImpl implements ItemRequestService {
    private final ItemRequestRepository requestRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    public static final Sort SORT = Sort.by("created").descending();

    @Transactional
    @Override
    public ItemRequestDto save(Long userId, ItemRequestDto dto) {
        User user = userRepository.getExistingUser(userId);

        ItemRequest request = toRequest(dto);
        request.setRequester(user);
        request.setCreated(LocalDateTime.now());
        request = requestRepository.save(request);

        return toRequestDto(request);
    }

    @Transactional(readOnly = true)
    @Override
    public ItemRequestDto findById(Long userId, Long requestId) {
        userRepository.getExistingUser(userId);
        ItemRequest request = getExistingRequest(requestId);
        ItemRequestDto result = toRequestDto(request);
        fillRequestsWithItems(result);

        return result;
    }

    @Transactional(readOnly = true)
    @Override
    public Collection<ItemRequestDto> findAll(Long userId) {
        userRepository.getExistingUser(userId);
        List<ItemRequest> requests = requestRepository.findByRequesterId(userId, SORT);

        return mapListToDtoList(requests);
    }

    @Transactional(readOnly = true)
    @Override
    public Collection<ItemRequestDto> findAllFromOtherUsers(Long userId, Integer from, Integer size) {
        PaginationValidator.validatePagination(from, size);
        userRepository.getExistingUser(userId);
        Pageable pageable = PageRequest.of(from / size, size, SORT);
        List<ItemRequest> requests = requestRepository.findByRequesterIdIsNot(userId, pageable);

        return mapListToDtoList(requests);
    }

    private List<ItemRequestDto> mapListToDtoList(List<ItemRequest> requests) {
        List<ItemRequestDto> result = requests.stream()
            .map(RequestMapper::toRequestDto)
            .collect(Collectors.toList());

        for (ItemRequestDto request : result) {
            fillRequestsWithItems(request);
        }

        return result;
    }

    public ItemRequest getExistingRequest(long id) {
        return requestRepository.getExistingRequest(id);
    }

    private void fillRequestsWithItems(ItemRequestDto request) {
        List<ItemDtoInRequest> items = itemRepository.getItemsByRequestId(request.getId());

        if (!items.isEmpty()) {
            request.setItems(items);
        } else {
            request.setItems(new ArrayList<>());
        }
    }
}