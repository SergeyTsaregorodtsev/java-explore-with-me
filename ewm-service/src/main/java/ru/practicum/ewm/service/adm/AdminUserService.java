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

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminUserService {
    private final UserRepository repository;

    public List<UserDto> getUsers(long[] ids, int from, int size) {
        if (ids.length == 0) {
            Sort sortById = Sort.by(Sort.Direction.ASC, "user_id");
            Pageable page = PageRequest.of(from / size, size, sortById);
            Page<User> userPage = repository.findAll(page);
            return userPage.getContent()
                    .stream()
                    .map(UserMapper::toDto)
                    .collect(Collectors.toList());
        } else {
            List<Long> idList = LongStream.of(ids).boxed().collect(Collectors.toList());
            return repository.findAllByIdIn(idList)
                    .stream()
                    .map(UserMapper::toDto)
                    .collect(Collectors.toList());
        }
    }

    public UserDto addUser(NewUserRequest userRequest) {
        User user = repository.save(UserMapper.toUser(userRequest));
        log.trace("Добавлен пользователь {}.", user.getName());
        return UserMapper.toDto(user);
    }

    public void removeUser(long userId) {
        repository.deleteById(userId);
    }
}