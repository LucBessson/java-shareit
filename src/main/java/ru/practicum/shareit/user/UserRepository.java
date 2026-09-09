package ru.practicum.shareit.user;

import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class UserRepository {

    private final List<User> users = new ArrayList<>();

    private long nextId = 1;

    public User save(User user) {
        user.setId(nextId++);
        users.add(user);
        return user;
    }

    public User update(User user) {
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getId().equals(user.getId())) {
                users.set(i, user);
                return user;
            }
        }

        return null;
    }

    public Optional<User> findById(Long id) {
        return users.stream()
                .filter(user -> user.getId().equals(id))
                .findFirst();
    }

    public List<User> findAll() {
        return new ArrayList<>(users);
    }

    public boolean existsById(Long id) {
        return users.stream()
                .anyMatch(user -> user.getId().equals(id));
    }

    public boolean existsByEmail(String email) {
        return users.stream()
                .anyMatch(user -> user.getEmail().equalsIgnoreCase(email));
    }

    public boolean existsByEmailAndNotId(String email, Long id) {
        return users.stream()
                .anyMatch(user ->
                        user.getEmail().equalsIgnoreCase(email)
                                && !user.getId().equals(id));
    }

    public void deleteById(Long id) {
        users.removeIf(user -> user.getId().equals(id));
    }
}