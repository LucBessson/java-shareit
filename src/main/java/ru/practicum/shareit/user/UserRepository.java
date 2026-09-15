package ru.practicum.shareit.user;

import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class UserRepository {

    private final Map<Long, User> users = new HashMap<>();
    private long nextId = 1L;

    public User save(User user) {
        user.setId(nextId++);
        users.put(user.getId(), user);
        return user;
    }

    public User update(User user) {
        users.put(user.getId(), user);
        return user;
    }

    public Optional<User> findById(Long id) {
        return Optional.ofNullable(users.get(id));
    }

    public List<User> findAll() {
        return new ArrayList<>(users.values());
    }

    public boolean existsById(Long id) {
        return users.containsKey(id);
    }

    public boolean existsByEmail(String email) {
        return users.values().stream()
                .anyMatch(user -> user.getEmail().equalsIgnoreCase(email));
    }

    public boolean existsByEmailAndNotId(String email, Long userId) {
        return users.values().stream()
                .anyMatch(user ->
                        !user.getId().equals(userId)
                                && user.getEmail().equalsIgnoreCase(email));
    }

    public void deleteById(Long id) {
        users.remove(id);
    }

    public void deleteAll() {
        users.clear();
        nextId = 1L;
    }
}