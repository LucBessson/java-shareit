package ru.practicum.shareit.booking;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingRequestDto;

import java.util.List;

@RestController
@RequestMapping("/bookings")
public class BookingController {

    private static final Logger log =
            LoggerFactory.getLogger(BookingController.class);

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookingDto create(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @RequestBody BookingRequestDto request) {

        log.info("Creating booking for user {}", userId);

        return bookingService.create(userId, request);
    }

    @PatchMapping("/{bookingId}")
    public BookingDto approve(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @PathVariable Long bookingId,
            @RequestParam boolean approved) {

        log.info(
                "Updating booking {} by user {}, approved={}",
                bookingId,
                userId,
                approved
        );

        return bookingService.approve(
                userId,
                bookingId,
                approved
        );
    }

    @GetMapping("/{bookingId}")
    public BookingDto getById(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @PathVariable Long bookingId) {

        log.info(
                "Getting booking {} by user {}",
                bookingId,
                userId
        );

        return bookingService.getById(
                userId,
                bookingId
        );
    }

    @GetMapping
    public List<BookingDto> getByBooker(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @RequestParam(defaultValue = "ALL") String state) {

        log.info(
                "Getting bookings for user {}, state={}",
                userId,
                state
        );

        return bookingService.getByBooker(
                userId,
                state
        );
    }

    @GetMapping("/owner")
    public List<BookingDto> getByOwner(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @RequestParam(defaultValue = "ALL") String state) {

        log.info(
                "Getting owner bookings for user {}, state={}",
                userId,
                state
        );

        return bookingService.getByOwner(
                userId,
                state
        );
    }
}
