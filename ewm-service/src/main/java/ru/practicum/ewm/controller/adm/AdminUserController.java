package ru.practicum.ewm.controller.adm;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.model.user.NewUserRequest;
import ru.practicum.ewm.model.user.UserDto;
import ru.practicum.ewm.service.adm.AdminUserService;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@Slf4j
@Validated
@RestController
@RequestMapping(path = "/admin/users")
@RequiredArgsConstructor
public class AdminUserController {
    private final AdminUserService service;

    @GetMapping
    public List<UserDto> getUser(@RequestParam long[] ids,
                                 @PositiveOrZero @RequestParam(name = "from", defaultValue = "0") int from,
                                 @Positive @RequestParam(name = "size", defaultValue = "10") int size) {
        log.trace("Получен GET-запрос по пользователям, from = {}, size = {}.", from, size);
        return service.getUsers(ids, from, size);
    }

    @PostMapping
    public UserDto addUser(@Valid @RequestBody NewUserRequest userRequest) {
        log.trace("Получен POST-запрос на добавление пользователя {}.", userRequest.getName());
        return service.addUser(userRequest);
    }

    @DeleteMapping("/{userId}")
    public void removeUser(@PathVariable long userId) {
        log.trace("Получен DELETE-запрос на удаление пользователя ID {}.", userId);
        service.removeUser(userId);
    }
}