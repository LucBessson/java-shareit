package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingRequestDto;

import java.util.List;

public interface BookingService {

    BookingDto create(Long userId, BookingRequestDto request);

    BookingDto approve(
            Long userId,
            Long bookingId,
            boolean approved);

    BookingDto getById(Long userId, Long bookingId);

    List<BookingDto> getByBooker(Long userId, String state);

    List<BookingDto> getByOwner(Long userId, String state);
}
