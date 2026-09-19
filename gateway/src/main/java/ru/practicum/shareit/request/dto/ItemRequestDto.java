package ru.practicum.shareit.request.dto;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

public class ItemRequestDto {
    private Long id;

    @NotBlank(message = "Description must not be blank")
    private String description;
    private LocalDateTime created;

    public ItemRequestDto() {
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
