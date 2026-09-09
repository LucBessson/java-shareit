package ru.practicum.shareit.request;

import ru.practicum.shareit.request.dto.ItemRequestDto;

public class ItemRequestMapper {

    private ItemRequestMapper() {
    }

    public static ItemRequestDto toItemRequestDto(
            ItemRequest itemRequest) {

        return new ItemRequestDto(
                itemRequest.getId(),
                itemRequest.getDescription(),
                itemRequest.getCreated()
        );
    }

    public static ItemRequest toItemRequest(
            ItemRequestDto itemRequestDto) {

        ItemRequest itemRequest = new ItemRequest();

        itemRequest.setId(itemRequestDto.getId());
        itemRequest.setDescription(itemRequestDto.getDescription());
        itemRequest.setCreated(itemRequestDto.getCreated());

        return itemRequest;
    }
}