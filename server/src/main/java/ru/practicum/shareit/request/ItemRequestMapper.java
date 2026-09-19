package ru.practicum.shareit.request;

import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemForRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestWithItemsDto;

import java.util.List;

public final class ItemRequestMapper {

    private ItemRequestMapper() {
    }

    public static ItemRequest toItemRequest(
            ItemRequestDto itemRequestDto) {

        ItemRequest itemRequest = new ItemRequest();
        itemRequest.setDescription(
                itemRequestDto.getDescription()
        );

        return itemRequest;
    }

    public static ItemRequestWithItemsDto toWithItemsDto(
            ItemRequest itemRequest,
            List<Item> items) {

        List<ItemForRequestDto> itemDtos = items.stream()
                .map(item ->
                        new ItemForRequestDto(
                                item.getId(),
                                item.getName(),
                                item.getOwner().getId()
                        )
                )
                .toList();

        return new ItemRequestWithItemsDto(
                itemRequest.getId(),
                itemRequest.getDescription(),
                itemRequest.getCreated(),
                itemDtos
        );
    }
}