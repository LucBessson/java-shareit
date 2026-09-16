package ru.practicum.shareit.item;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

@RestController
@RequestMapping("/items")
public class ItemController {

    private static final Logger log =
            LoggerFactory.getLogger(ItemController.class);

    private final ItemService itemService;

    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ItemDto create(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @RequestBody ItemDto itemDto) {

        log.info("Creating item for user {}", userId);

        return itemService.create(userId, itemDto);
    }

    @PatchMapping("/{itemId}")
    public ItemDto update(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @PathVariable Long itemId,
            @RequestBody ItemDto itemDto) {

        log.info("Updating item {} by user {}", itemId, userId);

        return itemService.update(userId, itemId, itemDto);
    }

    @GetMapping("/{itemId}")
    public ItemDto getById(@PathVariable Long itemId) {

        log.info("Getting item {}", itemId);

        return itemService.getById(itemId);
    }

    @GetMapping
    public List<ItemDto> getByOwner(
            @RequestHeader("X-Sharer-User-Id") Long userId) {

        log.info("Getting items for user {}", userId);

        return itemService.getByOwner(userId);
    }

    @GetMapping("/search")
    public List<ItemDto> search(
            @RequestParam String text) {

        log.info("Searching items by text '{}'", text);

        return itemService.search(text);
    }
}

