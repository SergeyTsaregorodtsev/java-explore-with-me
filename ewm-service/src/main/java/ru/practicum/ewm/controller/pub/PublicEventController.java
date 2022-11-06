package ru.practicum.ewm.controller.pub;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.model.event.EventFullDto;
import ru.practicum.ewm.model.event.EventShortDto;
import ru.practicum.ewm.model.event.EventUserFilter;
import ru.practicum.ewm.service.pub.PublicEventService;
import ru.practicum.ewm.statclient.StatClient;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Validated
@RestController
@RequestMapping(path = "/events")
@RequiredArgsConstructor
public class PublicEventController {
    private final PublicEventService service;
    private final StatClient statClient;

    // Получение событий с возможностью фильтрации
    @GetMapping
    public List<EventShortDto> getEvents(@RequestParam(name = "text") String text,
                                         @RequestParam(name = "categories") long[] categories,
                                         @RequestParam(name = "paid") boolean paid,
                                         @RequestParam(name = "rangeStart") String rangeStart,
                                         @RequestParam(name = "rangeEnd") String rangeEnd,
                                         @RequestParam(name = "onlyAvailable", defaultValue = "false") boolean onlyAvailable,
                                         @RequestParam(name = "sort") String sort,
                                         @PositiveOrZero @RequestParam(name = "from", defaultValue = "0") int from,
                                         @Positive @RequestParam(name = "size", defaultValue = "10") int size,
                                         HttpServletRequest request) {
        log.trace("Получен GET-запрос по событиям text - {}, категории - {}, paid - {}, start - {}, end - {}," +
                        " onlyAvailable - {}, sort - {}, from = {}, size = {}.",
                text, Arrays.toString(categories), paid, rangeStart, rangeEnd, onlyAvailable, sort, from, size);
        return service.getEvents(new EventUserFilter(text, categories, paid, rangeStart, rangeEnd, onlyAvailable), sort, from, size, request);
    }

    // Получение подробной информации об опубликованном событии по его идентификатору
    @GetMapping(path = "/{eventId}")
    public EventFullDto getEvent(@PathVariable long eventId, HttpServletRequest request) {
        log.trace("Получен GET-запрос на событие ID {}.", eventId);
        return service.getEvent(eventId, request);
    }
}