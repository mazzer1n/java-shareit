package ru.practicum.shareit.item.storage;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.core.exception.exceptions.ItemNotFoundException;
import ru.practicum.shareit.item.dto.ItemDtoInRequest;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Item;

import java.util.List;
import java.util.stream.Collectors;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {
    @Query("select i from Item i " +
        "where upper(i.name) like upper(concat('%', ?1, '%')) " +
        " or upper(i.description) like upper(concat('%', ?1, '%')) and i.available = true")
    List<Item> search(String text, Pageable pageable);

    List<Item> findByRequestId(Long requestId);

    List<Item> findByOwner(Long userId, Pageable pageable);

    default Item getExistingItem(long id) {
        return findById(id).orElseThrow(
                () -> new ItemNotFoundException("Товар с id " + id + " не найден.")
        );
    }

    default boolean hasUserZeroItems(long userId) {
        return findAll()
                .stream()
                .filter(item -> item.getOwner().equals(userId))
                .findAny().isEmpty();
    }

    default List<ItemDtoInRequest> getItemsByRequestId(Long id) {
        return findByRequestId(id)
                .stream().map(ItemMapper::toItemDtoInRequest)
                .collect(Collectors.toList());
    }
}