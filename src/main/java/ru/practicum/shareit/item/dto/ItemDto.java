package ru.practicum.shareit.item.dto;

public class ItemDto {

    private Long id;
    private String name;
    private String description;
    private Boolean available;

    public ItemDto() {
    }

    public ItemDto(
            Long id,
            String name,
            String description,
            Boolean available) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.available = available;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getAvailable() {
        return available;
    }

    public void setAvailable(Boolean available) {
        this.available = available;
    }
}
