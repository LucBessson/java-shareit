package ru.practicum.shareit.item;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingsDto;

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

        return itemService.create(
                userId,
                itemDto
        );
    }

    @PatchMapping("/{itemId}")
    public ItemDto update(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @PathVariable Long itemId,
            @RequestBody ItemDto itemDto) {

        log.info(
                "Updating item {} by user {}",
                itemId,
                userId
        );

        return itemService.update(
                userId,
                itemId,
                itemDto
        );
    }

    @GetMapping("/{itemId}")
    public ItemWithBookingsDto getById(
            @PathVariable Long itemId,
            @RequestHeader(
                    value = "X-Sharer-User-Id",
                    required = false) Long userId) {

        log.info("Getting item {}", itemId);

        return itemService.getById(
                itemId,
                userId
        );
    }

    @GetMapping
    public List<ItemWithBookingsDto> getByOwner(
            @RequestHeader("X-Sharer-User-Id") Long userId) {

        log.info(
                "Getting items for user {}",
                userId
        );

        return itemService.getByOwner(userId);
    }

    @GetMapping("/search")
    public List<ItemDto> search(
            @RequestParam String text) {

        log.info(
                "Searching items by text '{}'",
                text
        );

        return itemService.search(text);
    }

    @PostMapping("/{itemId}/comment")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto addComment(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @PathVariable Long itemId,
            @RequestBody CommentDto commentDto) {

        log.info(
                "Adding comment to item {} by user {}",
                itemId,
                userId
        );

        return itemService.addComment(
                userId,
                itemId,
                commentDto.getText()
        );
    }
}
