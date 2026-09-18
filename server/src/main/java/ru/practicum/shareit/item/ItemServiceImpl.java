package ru.practicum.shareit.item;

import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingsDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private final ItemRequestRepository itemRequestRepository;

    public ItemServiceImpl(
            ItemRepository itemRepository,
            UserRepository userRepository,
            BookingRepository bookingRepository,
            CommentRepository commentRepository,
            ItemRequestRepository itemRequestRepository) {
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
        this.bookingRepository = bookingRepository;
        this.commentRepository = commentRepository;
        this.itemRequestRepository = itemRequestRepository;
    }

    @Override
    public ItemDto create(
            Long userId,
            ItemDto itemDto) {

        User owner = userRepository.findById(userId)
                .orElseThrow(() ->
                        new NotFoundException(
                                "User with id "
                                        + userId
                                        + " not found"));

        validateItem(itemDto);

        Item item = ItemMapper.toItem(itemDto);
        item.setOwner(owner);

        if (itemDto.getRequestId() != null) {
            ItemRequest request = itemRequestRepository
                    .findById(itemDto.getRequestId())
                    .orElseThrow(() ->
                            new NotFoundException(
                                    "Request with id "
                                            + itemDto.getRequestId()
                                            + " not found"));

            item.setRequest(request);
        }

        return ItemMapper.toItemDto(
                itemRepository.save(item)
        );
    }

    @Override
    public ItemDto update(
            Long userId,
            Long itemId,
            ItemDto itemDto) {

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() ->
                        new NotFoundException(
                                "Item with id "
                                        + itemId
                                        + " not found"));

        if (item.getOwner() == null
                || !item.getOwner().getId().equals(userId)) {
            throw new NotFoundException(
                    "Item with id "
                            + itemId
                            + " not found");
        }

        if (itemDto.getName() != null) {
            if (itemDto.getName().isBlank()) {
                throw new ValidationException(
                        "Item name cannot be empty");
            }

            item.setName(itemDto.getName());
        }

        if (itemDto.getDescription() != null) {
            if (itemDto.getDescription().isBlank()) {
                throw new ValidationException(
                        "Item description cannot be empty");
            }

            item.setDescription(
                    itemDto.getDescription()
            );
        }

        if (itemDto.getAvailable() != null) {
            item.setAvailable(
                    itemDto.getAvailable()
            );
        }

        return ItemMapper.toItemDto(
                itemRepository.save(item)
        );
    }

    @Override
    public ItemWithBookingsDto getById(
            Long itemId,
            Long userId) {

        Item item = getItem(itemId);

        return buildItemDto(item, userId);
    }

    @Override
    public List<ItemWithBookingsDto> getByOwner(
            Long userId) {

        if (!userRepository.existsById(userId)) {
            throw new NotFoundException(
                    "User with id "
                            + userId
                            + " not found");
        }

        return itemRepository.findByOwner_Id(userId)
                .stream()
                .map(item -> buildItemDto(item, userId))
                .toList();
    }

    @Override
    public List<ItemDto> search(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }

        return itemRepository.search(text)
                .stream()
                .map(ItemMapper::toItemDto)
                .toList();
    }

    public CommentDto addComment(
            Long userId,
            Long itemId,
            String text) {

        if (text == null || text.isBlank()) {
            throw new ValidationException(
                    "Comment text cannot be empty");
        }

        Item item = getItem(itemId);

        User author = userRepository.findById(userId)
                .orElseThrow(() ->
                        new NotFoundException(
                                "User with id "
                                        + userId
                                        + " not found"));

        boolean rented = bookingRepository
                .existsByItem_IdAndBooker_IdAndEndIsBefore(
                        itemId,
                        userId,
                        LocalDateTime.now()
                );

        if (!rented) {
            throw new ValidationException(
                    "User has not rented this item");
        }

        Comment comment = new Comment();
        comment.setText(text);
        comment.setItem(item);
        comment.setAuthor(author);
        comment.setCreated(LocalDateTime.now());

        return toCommentDto(
                commentRepository.save(comment)
        );
    }

    private Item getItem(Long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() ->
                        new NotFoundException(
                                "Item with id "
                                        + itemId
                                        + " not found"));
    }

    private void validateItem(ItemDto itemDto) {
        if (itemDto.getName() == null
                || itemDto.getName().isBlank()) {
            throw new ValidationException(
                    "Item name cannot be empty");
        }

        if (itemDto.getDescription() == null
                || itemDto.getDescription().isBlank()) {
            throw new ValidationException(
                    "Item description cannot be empty");
        }

        if (itemDto.getAvailable() == null) {
            throw new ValidationException(
                    "Available must be specified");
        }
    }

    private ItemWithBookingsDto buildItemDto(
            Item item,
            Long userId) {

        BookingDto lastBooking = null;
        BookingDto nextBooking = null;

        if (item.getOwner().getId().equals(userId)) {

            LocalDateTime now = LocalDateTime.now();

            List<Booking> pastBookings =
                    bookingRepository.findByItem_IdAndEndIsBefore(
                            item.getId(),
                            now,
                            org.springframework.data.domain.Sort.by(
                                    org.springframework.data.domain.Sort.Direction.DESC,
                                    "end"
                            )
                    );

            List<Booking> futureBookings =
                    bookingRepository.findByItem_IdAndStartIsAfter(
                            item.getId(),
                            now,
                            org.springframework.data.domain.Sort.by(
                                    org.springframework.data.domain.Sort.Direction.ASC,
                                    "start"
                            )
                    );

            if (!pastBookings.isEmpty()) {
                lastBooking = toBookingDto(
                        pastBookings.get(0)
                );
            }

            if (!futureBookings.isEmpty()) {
                nextBooking = toBookingDto(
                        futureBookings.get(0)
                );
            }
        }

        List<CommentDto> comments =
                commentRepository
                        .findByItem_IdOrderByCreatedDesc(item.getId())
                        .stream()
                        .map(this::toCommentDto)
                        .toList();

        return new ItemWithBookingsDto(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.isAvailable(),
                lastBooking,
                nextBooking,
                comments
        );
    }

    private BookingDto toBookingDto(
            Booking booking) {

        return new BookingDto(
                booking.getId(),
                booking.getStart(),
                booking.getEnd(),
                ItemMapper.toItemDto(
                        booking.getItem()
                ),
                ru.practicum.shareit.user.UserMapper.toUserDto(
                        booking.getBooker()
                ),
                booking.getStatus()
        );
    }

    private CommentDto toCommentDto(
            Comment comment) {

        return new CommentDto(
                comment.getId(),
                comment.getText(),
                comment.getAuthor().getName(),
                comment.getCreated()
        );
    }
}
