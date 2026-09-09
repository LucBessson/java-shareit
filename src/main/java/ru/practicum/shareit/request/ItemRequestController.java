package ru.practicum.shareit.request;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.List;

@RestController
@RequestMapping("/requests")
public class ItemRequestController {

    private final ItemRequestService requestService;

    public ItemRequestController(ItemRequestService requestService) {
        this.requestService = requestService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ItemRequestDto create(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @RequestBody ItemRequestDto requestDto) {

        return requestService.create(userId, requestDto);
    }

    @GetMapping
    public List<ItemRequestDto> getByUser(
            @RequestHeader("X-Sharer-User-Id") Long userId) {

        return requestService.getByUser(userId);
    }

    @GetMapping("/all")
    public List<ItemRequestDto> getAllExceptUser(
            @RequestHeader("X-Sharer-User-Id") Long userId) {

        return requestService.getAllExceptUser(userId);
    }
}