package ru.practicum.shareit.booking;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    public BookingServiceImpl(
            BookingRepository bookingRepository,
            UserRepository userRepository,
            ItemRepository itemRepository) {
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.itemRepository = itemRepository;
    }

    @Override
    public BookingDto create(
            Long userId,
            BookingRequestDto request) {

        User booker = userRepository.findById(userId)
                .orElseThrow(() ->
                        new NotFoundException(
                                "User with id " + userId + " not found"));

        if (request.getStart() == null
                || request.getEnd() == null) {
            throw new ValidationException(
                    "Start and end dates are required");
        }

        if (!request.getStart().isBefore(request.getEnd())) {
            throw new ValidationException(
                    "Start must be before end");
        }

        if (request.getItemId() == null) {
            throw new ValidationException(
                    "Item id is required");
        }

        Item item = itemRepository.findById(request.getItemId())
                .orElseThrow(() ->
                        new NotFoundException(
                                "Item with id "
                                        + request.getItemId()
                                        + " not found"));

        if (!item.isAvailable()) {
            throw new ConflictException(
                    "Item is not available for booking");
        }

        if (item.getOwner().getId().equals(userId)) {
            throw new ConflictException(
                    "Owner cannot book own item");
        }

        Booking booking = new Booking();
        booking.setStart(request.getStart());
        booking.setEnd(request.getEnd());
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(BookingStatus.WAITING);

        return toDto(bookingRepository.save(booking));
    }

    @Override
    public BookingDto approve(
            Long userId,
            Long bookingId,
            boolean approved) {

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() ->
                        new NotFoundException(
                                "Booking with id "
                                        + bookingId
                                        + " not found"));

        if (!booking.getItem().getOwner().getId().equals(userId)) {
            throw new NotFoundException(
                    "Booking with id "
                            + bookingId
                            + " not found");
        }

        if (booking.getStatus() != BookingStatus.WAITING) {
            throw new ConflictException(
                    "Booking is already processed");
        }

        booking.setStatus(
                approved
                        ? BookingStatus.APPROVED
                        : BookingStatus.REJECTED
        );

        return toDto(bookingRepository.save(booking));
    }

    @Override
    public BookingDto getById(
            Long userId,
            Long bookingId) {

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() ->
                        new NotFoundException(
                                "Booking with id "
                                        + bookingId
                                        + " not found"));

        boolean isBooker =
                booking.getBooker().getId().equals(userId);

        boolean isOwner =
                booking.getItem().getOwner().getId().equals(userId);

        if (!isBooker && !isOwner) {
            throw new NotFoundException(
                    "Booking with id "
                            + bookingId
                            + " not found");
        }

        return toDto(booking);
    }

    @Override
    public List<BookingDto> getByBooker(
            Long userId,
            String state) {

        checkUser(userId);

        LocalDateTime now = LocalDateTime.now();

        List<Booking> bookings = getBookerBookings(
                userId,
                state,
                now);

        return bookings.stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public List<BookingDto> getByOwner(
            Long userId,
            String state) {

        checkUser(userId);

        List<Item> items = itemRepository.findByOwner_Id(userId);

        if (items.isEmpty()) {
            return List.of();
        }

        List<Long> itemIds = items.stream()
                .map(Item::getId)
                .toList();

        List<Booking> bookings =
                bookingRepository.findByItem_IdIn(
                        itemIds,
                        Sort.by(
                                Sort.Direction.DESC,
                                "start"
                        )
                );

        return filterByState(
                bookings,
                state,
                LocalDateTime.now()
        ).stream()
                .map(this::toDto)
                .toList();
    }

    private List<Booking> getBookerBookings(
            Long userId,
            String state,
            LocalDateTime now) {

        Sort sort = Sort.by(
                Sort.Direction.DESC,
                "start"
        );

        return switch (state.toUpperCase()) {
            case "ALL" -> bookingRepository.findByBooker_Id(
                    userId, sort);

            case "CURRENT" -> bookingRepository
                    .findByBooker_IdAndStartIsBeforeAndEndIsAfter(
                            userId,
                            now,
                            now,
                            sort);

            case "PAST" -> bookingRepository
                    .findByBooker_IdAndEndIsBefore(
                            userId,
                            now,
                            sort);

            case "FUTURE" -> bookingRepository
                    .findByBooker_IdAndStartIsAfter(
                            userId,
                            now,
                            sort);

            case "WAITING" -> bookingRepository.findByBooker_Id(
                            userId,
                            sort)
                    .stream()
                    .filter(booking ->
                            booking.getStatus()
                                    == BookingStatus.WAITING)
                    .toList();

            case "REJECTED" -> bookingRepository.findByBooker_Id(
                            userId,
                            sort)
                    .stream()
                    .filter(booking ->
                            booking.getStatus()
                                    == BookingStatus.REJECTED)
                    .toList();

            default -> throw new ValidationException(
                    "Unknown state: " + state);
        };
    }

    private List<Booking> filterByState(
            List<Booking> bookings,
            String state,
            LocalDateTime now) {

        return switch (state.toUpperCase()) {
            case "ALL" -> bookings;

            case "CURRENT" -> bookings.stream()
                    .filter(booking ->
                            booking.getStart().isBefore(now)
                                    && booking.getEnd().isAfter(now))
                    .toList();

            case "PAST" -> bookings.stream()
                    .filter(booking ->
                            booking.getEnd().isBefore(now))
                    .toList();

            case "FUTURE" -> bookings.stream()
                    .filter(booking ->
                            booking.getStart().isAfter(now))
                    .toList();

            case "WAITING" -> bookings.stream()
                    .filter(booking ->
                            booking.getStatus()
                                    == BookingStatus.WAITING)
                    .toList();

            case "REJECTED" -> bookings.stream()
                    .filter(booking ->
                            booking.getStatus()
                                    == BookingStatus.REJECTED)
                    .toList();

            default -> throw new ValidationException(
                    "Unknown state: " + state);
        };
    }

    private void checkUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException(
                    "User with id " + userId + " not found");
        }
    }

    private BookingDto toDto(Booking booking) {
        return new BookingDto(
                booking.getId(),
                booking.getStart(),
                booking.getEnd(),
                ItemMapper.toItemDto(booking.getItem()),
                UserMapper.toUserDto(booking.getBooker()),
                booking.getStatus()
        );
    }
}