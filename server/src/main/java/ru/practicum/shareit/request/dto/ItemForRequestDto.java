package ru.practicum.shareit.request.dto;

public class ItemForRequestDto {

    private Long id;
    private String name;
    private Long ownerId;

    public ItemForRequestDto() {
    }

    public ItemForRequestDto(
            Long id,
            String name,
            Long ownerId) {
        this.id = id;
        this.name = name;
        this.ownerId = ownerId;
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

    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }
}