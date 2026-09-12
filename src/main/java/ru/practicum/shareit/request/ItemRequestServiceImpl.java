package ru.practicum.shareit.request;

import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ItemRequestServiceImpl implements ItemRequestService {

    private final ItemRequestRepository requestRepository;

    private final UserRepository userRepository;

    public ItemRequestServiceImpl(
            ItemRequestRepository requestRepository,
            UserRepository userRepository) {
        this.requestRepository = requestRepository;
        this.userRepository = userRepository;
    }

    @Override
    public ItemRequestDto create(
            Long userId,
            ItemRequestDto requestDto) {

        User requestor = userRepository.findById(userId)
                .orElseThrow(() ->
                        new NotFoundException("User not found"));

        if (requestDto.getDescription() == null
                || requestDto.getDescription().isBlank()) {
            throw new ValidationException(
                    "Request description cannot be empty");
        }

        ItemRequest request =
                ItemRequestMapper.toItemRequest(requestDto);

        request.setRequestor(requestor);
        request.setCreated(LocalDateTime.now());

        ItemRequest savedRequest =
                requestRepository.save(request);

        return ItemRequestMapper.toItemRequestDto(savedRequest);
    }

    @Override
    public List<ItemRequestDto> getByUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User not found");
        }

        return requestRepository.findByRequestorId(userId)
                .stream()
                .map(ItemRequestMapper::toItemRequestDto)
                .toList();
    }

    @Override
    public List<ItemRequestDto> getAllExceptUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User not found");
        }

        return requestRepository.findAll()
                .stream()
                .filter(request ->
                        request.getRequestor() != null
                                && !request.getRequestor()
                                .getId()
                                .equals(userId))
                .map(ItemRequestMapper::toItemRequestDto)
                .toList();
    }
}