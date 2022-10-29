package ru.practicum.ewm.controller.adm;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.model.event.*;
import ru.practicum.ewm.service.adm.AdminEventService;

import java.util.Arrays;
import java.util.List;

@Slf4j
@RestController
@RequestMapping(path = "/admin/events")
@RequiredArgsConstructor
public class AdminEventController {
    private final AdminEventService service;

    @GetMapping
    public List<EventFullDto> getEvents(@RequestParam int[] users,
                                        @RequestParam String[] states,
                                        @RequestParam int[] categories,
                                        @RequestParam String rangeStart,
                                        @RequestParam String rangeEnd,
                                        @RequestParam(name = "from", defaultValue = "0") int from,
                                        @RequestParam(name = "size", defaultValue = "10") int size) {
        log.trace("Получен GET-запрос по событиям пользователей IDs - {}, статусы - {}, категории - {}, start - {}, end - {}, from = {}, size = {}.",
                Arrays.toString(users),
                Arrays.toString(states),
                Arrays.toString(categories),
                rangeStart, rangeEnd, from, size);
        return service.getEvents(new EventAdminFilter(users, states, categories, rangeStart, rangeEnd), from, size);
    }

    @PutMapping("/{eventId}")
    public EventFullDto updateEvent(@PathVariable int eventId,
                                    @RequestBody AdminUpdateEventRequest request) {
        log.trace("Получен PUT-запрос на обновление события ID {}.", eventId);
        return service.updateEvent(eventId, request);
    }

    @PatchMapping("/{eventId}/publish")
    public EventFullDto publishEvent(@PathVariable int eventId) {
        log.trace("Получен PATCH-запрос на публикацию события ID {}.", eventId);
        return service.publishEvent(eventId);
    }

    @PatchMapping("/{eventId}/reject")
    public EventFullDto rejectEvent(@PathVariable int eventId) {
        log.trace("Получен PATCH-запрос на отказ в публикации события ID {}.", eventId);
        return service.rejectEvent(eventId);
    }
}