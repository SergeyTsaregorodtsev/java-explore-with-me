package ru.practicum.ewm.controller.priv;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.model.request.ParticipationRequestDto;
import ru.practicum.ewm.service.priv.PrivateRequestService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(path = "/users/{userId}/requests")
@RequiredArgsConstructor
public class PrivateRequestController {
    private final PrivateRequestService service;

    // Получение информации о заявках текущего пользователя на участие в чужих событиях
    @GetMapping
    public List<ParticipationRequestDto> getRequests(@PathVariable long userId) {
        log.trace("Получен GET-запрос по заявкам пользователя ID {}.", userId);
        return service.getRequests(userId);
    }

    // Добавление запроса от текущего пользователя на участие в событии
    @PostMapping
    public ParticipationRequestDto addRequest(@PathVariable long userId,
                                              @RequestParam(name = "eventId") long eventId) {
        log.trace("Получен POST-запрос на добавление заявки на событие ID {} от пользователя ID {}.", eventId, userId);
        return service.addRequest(userId, eventId);
    }

    // Отмена своего запроса на участие в событии
    @PatchMapping("/{requestId}/cancel")
    public ParticipationRequestDto cancelRequest(@PathVariable long userId,
                                                 @PathVariable long requestId) {
        log.trace("Получен PATCH-запрос на отмену заявки ID {} пользователя ID {}.", requestId, userId);
        return service.cancelRequest(userId, requestId);
    }
}