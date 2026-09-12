package ru.practicum.shareit.item;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class ItemRepository {

    private final List<Item> items = new ArrayList<>();

    private long nextId = 1L;

    public Item save(Item item) {
        item.setId(nextId++);
        items.add(item);
        return item;
    }

    public Item update(Item item) {
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).getId().equals(item.getId())) {
                items.set(i, item);
                return item;
            }
        }

        return null;
    }

    public Optional<Item> findById(Long id) {
        return items.stream()
                .filter(item -> item.getId().equals(id))
                .findFirst();
    }

    public List<Item> findAll() {
        return new ArrayList<>(items);
    }

    public List<Item> findByOwnerId(Long ownerId) {
        return items.stream()
                .filter(item ->
                        item.getOwner() != null
                                && item.getOwner().getId().equals(ownerId))
                .toList();
    }

    public List<Item> search(String text) {
        String searchText = text.toLowerCase();

        return items.stream()
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