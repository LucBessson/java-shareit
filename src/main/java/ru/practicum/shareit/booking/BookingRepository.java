package ru.practicum.shareit.booking;

import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class BookingRepository {

    private final List<Booking> bookings = new ArrayList<>();

    private long nextId = 1L;

    public Booking save(Booking booking) {
        booking.setId(nextId++);
        bookings.add(booking);
        return booking;
    }

    public Optional<Booking> findById(Long id) {
        return bookings.stream()
                .filter(booking -> booking.getId().equals(id))
                .findFirst();
    }

    public List<Booking> findAll() {
        return new ArrayList<>(bookings);
    }

    public List<Booking> findByBookerId(Long userId) {
        return bookings.stream()
                .filter(booking ->
                        booking.getBooker() != null
                                && booking.getBooker().getId().equals(userId))
                .toList();
    }

    public List<Booking> findByOwnerId(Long userId) {
        return bookings.stream()
                .filter(booking ->
                        booking.getItem() != null
                                && booking.getItem().getOwner() != null
                                && booking.getItem().getOwner()
                                .getId()
                                .equals(userId))
                .toList();
    }
}