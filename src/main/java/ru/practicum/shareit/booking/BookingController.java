package ru.practicum.shareit.booking;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;

import java.util.List;

@RestController
@RequestMapping("/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookingDto create(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @RequestBody BookingDto bookingDto) {

        return bookingService.create(userId, bookingDto);
    }

    @PostMapping("/{bookingId}")
    public BookingDto approve(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @PathVariable Long bookingId,
            @RequestParam boolean approved) {

        return bookingService.approve(
                userId,
                bookingId,
                approved);
    }

    @GetMapping("/{bookingId}")
    public BookingDto getById(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @PathVariable Long bookingId) {

        return bookingService.getById(userId, bookingId);
    }

    @GetMapping
    public List<BookingDto> getByBooker(
            @RequestHeader("X-Sharer-User-Id") Long userId) {

        return bookingService.getByBooker(userId);
    }

    @GetMapping("/owner")
    public List<BookingDto> getByOwner(
            @RequestHeader("X-Sharer-User-Id") Long userId) {

        return bookingService.getByOwner(userId);
    }
}