package ru.practicum.shareit.item;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;

import java.util.*;

@Repository
public class ItemRepository {

    private final Map<Long, Item> items = new HashMap<>();

    private long nextId = 1L;

    public Item save(Item item) {
        item.setId(nextId++);
        items.put(item.getId(), item);
        return item;
    }

    public Item update(Item item) {
        items.put(item.getId(), item);
        return item;
    }

    public Optional<Item> findById(Long id) {
        return Optional.ofNullable(items.get(id));
    }

    public List<Item> findAll() {
        return new ArrayList<>(items.values());
    }

    public List<Item> findByOwnerId(Long ownerId) {
        return items.values().stream()
                .filter(item ->
                        item.getOwner() != null
                                && item.getOwner().getId().equals(ownerId))
                .toList();
    }

    public List<Item> search(String text) {
        String searchText = text.toLowerCase();

        return items.values().stream()
                .filter(Item::isAvailable)
                .filter(item -> {
                    String name = item.getName() == null
                            ? ""
                            : item.getName().toLowerCase();

                    String description = item.getDescription() == null
                            ? ""
                            : item.getDescription().toLowerCase();

                    return name.contains(searchText)
                            || description.contains(searchText);
                })
                .toList();
    }

    public void deleteAll() {
        items.clear();
        nextId = 1L;
    }
}

