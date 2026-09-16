package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

public class BookingMapper {

    private BookingMapper() {
    }

    public static BookingDto toBookingDto(Booking booking) {
        Long itemId = null;
        Long bookerId = null;

        if (booking.getItem() != null) {
            itemId = booking.getItem().getId();
        }

        if (booking.getBooker() != null) {
            bookerId = booking.getBooker().getId();
        }

        return new BookingDto(
                booking.getId(),
                booking.getStart(),
                booking.getEnd(),
                itemId,
                bookerId,
                booking.getStatus()
        );
    }

    public static Booking toBooking(BookingDto bookingDto) {
        Booking booking = new Booking();

        booking.setId(bookingDto.getId());
        booking.setStart(bookingDto.getStart());
        booking.setEnd(bookingDto.getEnd());
        booking.setStatus(bookingDto.getStatus());

        if (bookingDto.getItemId() != null) {
            Item item = new Item();
            item.setId(bookingDto.getItemId());
            booking.setItem(item);
        }

        if (bookingDto.getBookerId() != null) {
            User user = new User();
            user.setId(bookingDto.getBookerId());
            booking.setBooker(user);
        }

        return booking;
    }
}