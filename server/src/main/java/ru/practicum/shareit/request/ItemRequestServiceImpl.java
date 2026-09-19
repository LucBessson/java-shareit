package ru.practicum.shareit.request;

import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestWithItemsDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ItemRequestServiceImpl implements ItemRequestService {

    private final ItemRequestRepository itemRequestRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    public ItemRequestServiceImpl(
            ItemRequestRepository itemRequestRepository,
            UserRepository userRepository,
            ItemRepository itemRepository) {
        this.itemRequestRepository = itemRequestRepository;
        this.userRepository = userRepository;
        this.itemRepository = itemRepository;
    }

    @Override
    public ItemRequestDto create(
            Long userId,
            ItemRequestDto itemRequestDto) {

        User requester = getUser(userId);

        if (itemRequestDto.getDescription() == null
                || itemRequestDto.getDescription().isBlank()) {
            throw new ValidationException(
                    "Request description cannot be empty");
        }

        ItemRequest itemRequest =
                ItemRequestMapper.toItemRequest(itemRequestDto);
        itemRequest.setRequester(requester);
        itemRequest.setCreated(LocalDateTime.now());

        ItemRequest saved =
                itemRequestRepository.save(itemRequest);

        return new ItemRequestDto(
                saved.getId(),
                saved.getDescription(),
                saved.getCreated()
        );
    }

    @Override
    public List<ItemRequestWithItemsDto> getOwn(Long userId) {
        getUser(userId);

        List<ItemRequest> requests =
                itemRequestRepository
                        .findAllByRequesterIdOrderByCreatedDesc(userId);

        return fillItems(requests);
    }

    @Override
    public List<ItemRequestWithItemsDto> getAll(Long userId) {
        getUser(userId);

        List<ItemRequest> requests =
                itemRequestRepository
                        .findAllByRequesterIdNotOrderByCreatedDesc(userId);

        return fillItems(requests);
    }

    @Override
    public ItemRequestWithItemsDto getById(
            Long userId,
            Long requestId) {

        ItemRequest itemRequest =
                itemRequestRepository.findById(requestId)
                        .orElseThrow(() ->
                                new NotFoundException(
                                        "Request with id "
                                                + requestId
                                                + " not found"));

        List<Item> items =
                itemRepository.findByRequest_Id(requestId);

        return ItemRequestMapper.toWithItemsDto(
                itemRequest, items);
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() ->
                        new NotFoundException(
                                "User with id "
                                        + userId
                                        + " not found"));
    }

    private List<ItemRequestWithItemsDto> fillItems(
            List<ItemRequest> requests) {

        List<Long> requestIds = requests.stream()
                .map(ItemRequest::getId)
                .toList();

        Map<Long, List<Item>> itemsByRequest =
                itemRepository.findByRequest_IdIn(requestIds)
                        .stream()
                        .collect(Collectors.groupingBy(
                                item -> item.getRequest().getId()));

        return requests.stream()
                .map(request ->
                        ItemRequestMapper.toWithItemsDto(
                                request,
                                itemsByRequest.getOrDefault(
                                        request.getId(),
                                        List.of())
                        )
                )
                .toList();
    }
}