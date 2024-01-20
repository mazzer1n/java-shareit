package ru.practicum.shareit.request.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.core.exception.exceptions.RequestNotFoundException;
import ru.practicum.shareit.request.model.ItemRequest;

import java.util.List;

@Repository
public interface ItemRequestRepository extends JpaRepository<ItemRequest, Long> {
    List<ItemRequest> findByRequesterId(Long userId, Sort sort);

    List<ItemRequest> findByRequesterIdIsNot(Long userId, Pageable pageable);

    default ItemRequest getExistingRequest(long id) {
        return findById(id).orElseThrow(
                () -> new RequestNotFoundException("Запрос с id " + id + " не найден.")
        );
    }
}