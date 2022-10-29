package ru.practicum.ewm.controller.priv;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.model.event.*;
import ru.practicum.ewm.model.request.ParticipationRequestDto;
import ru.practicum.ewm.service.priv.PrivateEventService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(path = "/users/{userId}/events")
@RequiredArgsConstructor
public class PrivateEventController {
    private final PrivateEventService service;

    // Получение событий, добавленных текущим пользователем
    @GetMapping
    public List<EventShortDto> getEvents(@PathVariable int userId,
                                         @RequestParam(name = "from", defaultValue = "0") int from,
                                         @RequestParam(name = "size", defaultValue = "10") int size) {
        log.trace("Получен GET-запрос по событиям пользователя ID {}, from = {}, size = {}.", userId, from, size);
        return service.getEvents(userId, from, size);
    }

    // Изменение события добавленного текущим пользователем
    @PatchMapping
    public EventFullDto updateEvent(@PathVariable int userId,
                                    @RequestBody UpdateEventRequest request) {
        log.trace("Получен PATCH-запрос на обновление события ID {} пользователя ID {}.", request.getEventId(), userId);
        return service.updateEvent(userId, request);
    }

    // Добавление нового события
    @PostMapping
    public EventFullDto addEvent(@PathVariable int userId,
                                 @RequestBody NewEventDto eventDto) {
        log.trace("Получен POST-запрос на добавление события ID {} пользователя ID {}.", eventDto.getTitle(), userId);
        return service.addEvent(userId, eventDto);
    }

    // Получение полной информации о событии добавленном текущим пользователем
    @GetMapping("/{eventId}")
    public EventFullDto getEvent(@PathVariable int userId,
                                 @PathVariable int eventId) {
        log.trace("Получен GET-запрос пользователя ID {} по событию ID {}.", userId, eventId);
        return service.getEvent(userId, eventId);
    }

    // Отмена события добавленного текущим пользователем
    @PatchMapping("/{eventId}")
    public EventFullDto cancelEvent(@PathVariable int userId,
                                    @PathVariable int eventId) {
        log.trace("Получен PATCH-запрос на отмену события ID {} пользователя ID {}.", eventId, userId);
        return service.cancelEvent(userId, eventId);
    }

    // Получение информации о запросах на участие в событии текущего пользователя
    @GetMapping("/{eventId}/requests")
    public List<ParticipationRequestDto> getRequests(@PathVariable int userId,
                                                     @PathVariable int eventId) {
        log.trace("Получен GET-запрос пользователя ID {} по событию ID {}.", userId, eventId);
        return service.getRequests(userId, eventId);
    }

    // Подтверждение чужой заявки на участие в событии текущего пользователя
    @PatchMapping("/{eventId}/requests/{reqId}/confirm")
    public ParticipationRequestDto confirmRequest(@PathVariable int userId,
                                                  @PathVariable int eventId,
                                                  @PathVariable int reqId) {
        log.trace("Получен PATCH-запрос на подтверждение заявки ID {} пользователя ID {} на событие ID {}.",
                reqId, userId, eventId);
        return service.confirmRequest(userId, eventId, reqId, true);
    }

    // Отклонение чужой заявки на участие в событии текущего пользователя
    @PatchMapping("/{eventId}/requests/{reqId}/reject")
    public ParticipationRequestDto rejectRequest(@PathVariable int userId,
                                                  @PathVariable int eventId,
                                                  @PathVariable int reqId) {
        log.trace("Получен PATCH-запрос на отклонение заявки ID {} пользователя ID {} на событие ID {}.",
                reqId, userId, eventId);
        return service.confirmRequest(userId, eventId, reqId, false);
    }
}