package ru.practicum.shareit.request;

import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestWithItemsDto;

import java.util.List;

@RestController
@RequestMapping("/requests")
public class ItemRequestController {

    private final ItemRequestService itemRequestService;

    public ItemRequestController(
            ItemRequestService itemRequestService) {
        this.itemRequestService = itemRequestService;
    }

    @PostMapping
    public ItemRequestDto create(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @RequestBody ItemRequestDto itemRequestDto) {

        return itemRequestService.create(userId, itemRequestDto);
    }

    @GetMapping
    public List<ItemRequestWithItemsDto> getOwn(
            @RequestHeader("X-Sharer-User-Id") Long userId) {

        return itemRequestService.getOwn(userId);
    }

    @GetMapping("/all")
    public List<ItemRequestWithItemsDto> getAll(
            @RequestHeader("X-Sharer-User-Id") Long userId) {

        return itemRequestService.getAll(userId);
    }

    @GetMapping("/{requestId}")
    public ItemRequestWithItemsDto getById(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @PathVariable Long requestId) {

        return itemRequestService.getById(userId, requestId);
    }
}