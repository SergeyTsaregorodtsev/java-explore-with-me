package ru.practicum.ewm.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.model.EndpointHitDto;
import ru.practicum.ewm.service.StatService;
import ru.practicum.ewm.model.ViewStats;

import java.util.Arrays;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class StatController {
    private final StatService service;

    @PostMapping("/hit")
    public void hit(@RequestBody EndpointHitDto hit) {
        service.hit(hit);
        log.trace("Получен POST-запрос приложения '{}' по запросу на {} от пользователя IP {}.",
                hit.getApp(), hit.getUri(), hit.getIp());
    }

    @GetMapping("/stats")
    public List<ViewStats> get(@RequestParam(name = "start") String start,
                               @RequestParam(name = "end") String end,
                               @RequestParam(name = "uris") String[] uris,
                               @RequestParam(name = "unique", defaultValue = "false") boolean unique,
                               @RequestParam(name = "app", defaultValue = "ewm-main-service") String app) {
        log.trace("Получен GET-запрос: start-{} , end-{}, uris-{}, unique-{}.",
                start, end, Arrays.toString(uris), unique);
        return service.get(start, end, uris, unique, app);
    }
}