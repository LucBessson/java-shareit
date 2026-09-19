package ru.practicum.shareit.booking;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.ForbiddenException;
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

        if (request.getStart().isBefore(LocalDateTime.now())) {
            throw new ValidationException(
                    "Booking start date cannot be in the past");
        }

        if (request.getEnd().isBefore(LocalDateTime.now())) {
            throw new ValidationException(
                    "Booking end date cannot be in the past");
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
            throw new ValidationException(
                    "Item is not available for booking");
        }

        if (item.getOwner().getId().equals(userId)) {
            throw new ConflictException(
                    "Owner cannot book own item");
        }

        Booking booking = BookingMapper.toBooking(
                request,
                item,
                booker
        );

        return BookingMapper.toBookingDto(
                bookingRepository.save(booking)
        );
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

        if (booking.getBooker().getId().equals(userId)) {
            throw new NotFoundException(
                    "Booking with id "
                            + bookingId
                            + " not found");
        }

        if (!booking.getItem().getOwner().getId().equals(userId)) {
            throw new ForbiddenException(
                    "User with id " + userId
                            + " is not the owner of the item");
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

        return BookingMapper.toBookingDto(
                bookingRepository.save(booking)
        );
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

        return BookingMapper.toBookingDto(booking);
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
                now
        );

        return bookings.stream()
                .map(BookingMapper::toBookingDto)
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

        Sort sort = Sort.by(
                Sort.Direction.DESC,
                "start"
        );

        LocalDateTime now = LocalDateTime.now();

        List<Booking> bookings = switch (state.toUpperCase()) {
            case "ALL" -> bookingRepository.findByItem_IdIn(
                    itemIds,
                    sort
            );

            case "CURRENT" -> bookingRepository
                    .findByItem_IdInAndStartIsBeforeAndEndIsAfter(
                            itemIds,
                            now,
                            now,
                            sort
                    );

            case "PAST" -> bookingRepository.findByItem_IdInAndEndIsBefore(
                    itemIds,
                    now,
                    sort
            );

            case "FUTURE" -> bookingRepository.findByItem_IdInAndStartIsAfter(
                    itemIds,
                    now,
                    sort
            );

            case "WAITING" -> bookingRepository.findByItem_IdInAndStatus(
                    itemIds,
                    BookingStatus.WAITING,
                    sort
            );

            case "REJECTED" -> bookingRepository.findByItem_IdInAndStatus(
                    itemIds,
                    BookingStatus.REJECTED,
                    sort
            );

            default -> throw new ValidationException(
                    "Unknown state: " + state
            );
        };

        return bookings.stream()
                .map(BookingMapper::toBookingDto)
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
                    userId,
                    sort
            );

            case "CURRENT" -> bookingRepository
                    .findByBooker_IdAndStartIsBeforeAndEndIsAfter(
                            userId,
                            now,
                            now,
                            sort
                    );

            case "PAST" -> bookingRepository.findByBooker_IdAndEndIsBefore(
                    userId,
                    now,
                    sort
            );

            case "FUTURE" -> bookingRepository.findByBooker_IdAndStartIsAfter(
                    userId,
                    now,
                    sort
            );

            case "WAITING" -> bookingRepository.findByBooker_Id(
                            userId,
                            sort
                    ).stream()
                    .filter(booking ->
                            booking.getStatus()
                                    == BookingStatus.WAITING)
                    .toList();

            case "REJECTED" -> bookingRepository.findByBooker_Id(
                            userId,
                            sort
                    ).stream()
                    .filter(booking ->
                            booking.getStatus()
                                    == BookingStatus.REJECTED)
                    .toList();

            default -> throw new ValidationException(
                    "Unknown state: " + state
            );
        };
    }

    private void checkUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException(
                    "User with id " + userId + " not found");
        }
    }
}