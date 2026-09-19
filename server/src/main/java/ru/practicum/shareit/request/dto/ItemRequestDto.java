package ru.practicum.shareit.request.dto;

import java.time.LocalDateTime;

public class ItemRequestDto {

    private Long id;
    private String description;
    private LocalDateTime created;

    public ItemRequestDto() {
    }

    public ItemRequestDto(
            Long id,
            String description,
            LocalDateTime created) {
        this.id = id;
        this.description = description;
        this.created = created;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getCreated() {
        return created;
    }

    public void setCreated(LocalDateTime created) {
        this.created = created;
    }
}