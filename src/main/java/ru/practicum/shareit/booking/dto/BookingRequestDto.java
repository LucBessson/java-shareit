package ru.practicum.shareit.booking.dto;

import java.time.LocalDateTime;

public class BookingRequestDto {

    private LocalDateTime start;
    private LocalDateTime end;
    private Long itemId;

    public BookingRequestDto() {
    }

    public LocalDateTime getStart() {
        return start;
    }

    public void setStart(LocalDateTime start) {
        this.start = start;
    }

    public LocalDateTime getEnd() {
        return end;
    }

    public void setEnd(LocalDateTime end) {
        this.end = end;
    }

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }
}
