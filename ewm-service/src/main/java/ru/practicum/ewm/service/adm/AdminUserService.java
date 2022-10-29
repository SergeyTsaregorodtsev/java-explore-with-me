package ru.practicum.ewm.service.adm;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.model.user.NewUserRequest;
import ru.practicum.ewm.model.user.User;
import ru.practicum.ewm.model.user.UserDto;
import ru.practicum.ewm.model.user.UserMapper;
import ru.practicum.ewm.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminUserService {
    private final UserRepository repository;

    public List<UserDto> getUsers(int[] ids, int from, int size) {
        List<UserDto> result = new ArrayList<>();

        if (ids.length == 0) {
            Sort sortById = Sort.by(Sort.Direction.ASC, "user_id");
            Pageable page = PageRequest.of(from / size, size, sortById);
            Page<User> userPage = repository.findAll(page);
            for (User user : userPage.getContent()) {
                result.add(UserMapper.toDto(user));
            }
        } else {
            for (int i : ids) {
                Optional<User> user = repository.findById(i);
                if (user.isPresent()) {
                    result.add(UserMapper.toDto(user.get()));
                } else {
                    log.trace("Пользователь ID {} не найден.", i);
                }
            }
        }
        return result;
    }

    public UserDto addUser(NewUserRequest userRequest) {
        User user = repository.save(UserMapper.toUser(userRequest));
        log.trace("Добавлен пользователь {}.", user.getName());
        return UserMapper.toDto(user);
    }

    public void removeUser(int userId) {
        repository.deleteById(userId);
    }
}