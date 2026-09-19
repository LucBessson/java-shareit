package ru.practicum.shareit.user;

import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDto create(UserDto userDto) {
        validateName(userDto.getName());
        validateEmail(userDto.getEmail());

        if (userRepository.existsByEmailIgnoreCase(userDto.getEmail())) {
            throw new ConflictException("Email already exists");
        }

        User user = UserMapper.toUser(userDto);
        User savedUser = userRepository.save(user);

        return UserMapper.toUserDto(savedUser);
    }

    @Override
    public UserDto update(Long userId, UserDto userDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new NotFoundException(
                                "User with id " + userId + " not found"));

        if (userDto.getEmail() != null) {
            validateEmail(userDto.getEmail());

            if (userRepository.existsByEmailIgnoreCaseAndIdNot(
                    userDto.getEmail(), userId)) {
                throw new ConflictException("Email already exists");
            }

            user.setEmail(userDto.getEmail());
        }

        if (userDto.getName() != null) {
            validateName(userDto.getName());
            user.setName(userDto.getName());
        }

        return UserMapper.toUserDto(userRepository.save(user));
    }

    @Override
    public UserDto getById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new NotFoundException(
                                "User with id " + userId + " not found"));

        return UserMapper.toUserDto(user);
    }

    @Override
    public List<UserDto> getAll() {
        return userRepository.findAll()
                .stream()
                .map(UserMapper::toUserDto)
                .toList();
    }

    @Override
    public void delete(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException(
                    "User with id " + userId + " not found");
        }

        userRepository.deleteById(userId);
    }

    private void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new ValidationException("Name cannot be empty");
        }
    }

    private void validateEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new ValidationException("Email cannot be empty");
        }

        if (!email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            throw new ValidationException("Invalid email");
        }
    }
}
