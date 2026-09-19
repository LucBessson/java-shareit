package ru.practicum.shareit.booking.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDateTime;

public class BookItemRequestDto {
    @NotNull(message = "Item id must not be null")
    @Positive(message = "Item id must be positive")
    private Long itemId;

    @NotNull(message = "Start must not be null")
    @FutureOrPresent(message = "Start must be in the present or future")
    private LocalDateTime start;

    @NotNull(message = "End must not be null")
    @Future(message = "End must be in the future")
    private LocalDateTime end;

    public BookItemRequestDto() {
    }

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
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
}
