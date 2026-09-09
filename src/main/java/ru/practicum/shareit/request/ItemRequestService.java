package ru.practicum.shareit.request;

import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.List;

public interface ItemRequestService {

    ItemRequestDto create(Long userId, ItemRequestDto requestDto);

    List<ItemRequestDto> getByUser(Long userId);

    List<ItemRequestDto> getAllExceptUser(Long userId);
}