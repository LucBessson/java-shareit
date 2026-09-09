package ru.practicum.shareit.request;

import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class ItemRequestRepository {

    private final List<ItemRequest> requests = new ArrayList<>();

    private long nextId = 1L;

    public ItemRequest save(ItemRequest request) {
        request.setId(nextId++);
        requests.add(request);
        return request;
    }

    public Optional<ItemRequest> findById(Long id) {
        return requests.stream()
                .filter(request -> request.getId().equals(id))
                .findFirst();
    }

    public List<ItemRequest> findAll() {
        return new ArrayList<>(requests);
    }

    public List<ItemRequest> findByRequestorId(Long userId) {
        return requests.stream()
                .filter(request ->
                        request.getRequestor() != null
                                && request.getRequestor().getId().equals(userId))
                .toList();
    }
}