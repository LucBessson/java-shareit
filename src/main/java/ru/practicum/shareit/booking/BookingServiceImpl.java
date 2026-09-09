package ru.practicum.shareit.booking;

import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    public BookingServiceImpl(
            BookingRepository bookingRepository,
            ItemRepository itemRepository,
            UserRepository userRepository) {
        this.bookingRepository = bookingRepository;
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
    }

    @Override
    public BookingDto create(
            Long userId,
            BookingDto bookingDto) {

        User booker = userRepository.findById(userId)
                .orElseThrow(() ->
                        new NotFoundException("User not found"));

        if (bookingDto.getItemId() == null) {
            throw new ValidationException("Item id cannot be null");
        }

        Item item = itemRepository.findById(bookingDto.getItemId())
                .orElseThrow(() ->
                        new NotFoundException("Item not found"));

        if (item.getOwner() != null
                && item.getOwner().getId().equals(userId)) {
            throw new ConflictException(
                    "Owner cannot book their own item");
        }

        if (!item.isAvailable()) {
            throw new ConflictException(
                    "Item is not available");
        }

        validateDates(
                bookingDto.getStart(),
                bookingDto.getEnd());

        Booking booking = new Booking();

        booking.setStart(bookingDto.getStart());
        booking.setEnd(bookingDto.getEnd());
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(BookingStatus.WAITING);

        Booking savedBooking = bookingRepository.save(booking);

        return BookingMapper.toBookingDto(savedBooking);
    }

    @Override
    public BookingDto approve(
            Long userId,
            Long bookingId,
            boolean approved) {

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() ->
                        new NotFoundException("Booking not found"));

        if (booking.getItem() == null
                || booking.getItem().getOwner() == null
                || !booking.getItem().getOwner().getId().equals(userId)) {
            throw new NotFoundException("Booking not found");
        }

        if (booking.getStatus() != BookingStatus.WAITING) {
            throw new ConflictException(
                    "Booking has already been processed");
        }

        booking.setStatus(
                approved
                        ? BookingStatus.APPROVED
                        : BookingStatus.REJECTED
        );

        return BookingMapper.toBookingDto(booking);
    }

    @Override
    public BookingDto getById(
            Long userId,
            Long bookingId) {

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() ->
                        new NotFoundException("Booking not found"));

        boolean isBooker = booking.getBooker() != null
                && booking.getBooker().getId().equals(userId);

        boolean isOwner = booking.getItem() != null
                && booking.getItem().getOwner() != null
                && booking.getItem().getOwner().getId().equals(userId);

        if (!isBooker && !isOwner) {
            throw new NotFoundException("Booking not found");
        }

        return BookingMapper.toBookingDto(booking);
    }

    @Override
    public List<BookingDto> getByBooker(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User not found");
        }

        return bookingRepository.findByBookerId(userId)
                .stream()
                .map(BookingMapper::toBookingDto)
                .toList();
    }

    @Override
    public List<BookingDto> getByOwner(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User not found");
        }

        return bookingRepository.findByOwnerId(userId)
                .stream()
                .map(BookingMapper::toBookingDto)
                .toList();
    }

    private void validateDates(
            LocalDateTime start,
            LocalDateTime end) {

        if (start == null || end == null) {
            throw new ValidationException(
                    "Start and end cannot be null");
        }

        if (start.isAfter(end)) {
            throw new ValidationException(
                    "Start cannot be after end");
        }

        if (start.equals(end)) {
            throw new ValidationException(
                    "Start and end cannot be equal");
        }

        if (start.isBefore(LocalDateTime.now())) {
            throw new ValidationException(
                    "Start cannot be in the past");
        }
    }
}