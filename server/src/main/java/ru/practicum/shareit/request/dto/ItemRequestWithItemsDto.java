package ru.practicum.shareit.request.dto;

import java.time.LocalDateTime;
import java.util.List;

public class ItemRequestWithItemsDto {

    private Long id;
    private String description;
    private LocalDateTime created;
    private List<ItemForRequestDto> items;

    public ItemRequestWithItemsDto() {
    }

    public ItemRequestWithItemsDto(
            Long id,
            String description,
            LocalDateTime created,
            List<ItemForRequestDto> items) {
        this.id = id;
        this.description = description;
        this.created = created;
        this.items = items;
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

    public List<ItemForRequestDto> getItems() {
        return items;
    }

    public void setItems(List<ItemForRequestDto> items) {
        this.items = items;
    }
}