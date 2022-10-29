package ru.practicum.ewm.controller.adm;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.model.compilation.CompilationDto;
import ru.practicum.ewm.model.compilation.NewCompilationDto;
import ru.practicum.ewm.service.adm.AdminCompilationService;

@Slf4j
@RestController
@RequestMapping(path = "/admin/compilations")
@RequiredArgsConstructor
public class AdminCompilationController {
    private final AdminCompilationService service;

    // Добавление новой подборки
    @PostMapping
    public CompilationDto addCompilation(@RequestBody NewCompilationDto dto) {
        log.trace("Получен POST-запрос на добавление подборки {}.", dto.getTitle());
        return service.addCompilation(dto);
    }

    // Удаление подборки
    @DeleteMapping("/{compId}")
    public void removeCompilation(@PathVariable int compId) {
        log.trace("Получен DELETE-запрос на удаление подборки ID {}.", compId);
        service.removeCompilation(compId);
    }

    // Удаление события из подборки
    @DeleteMapping("/{compId}/events/{eventId}")
    public void removeEvent(@PathVariable int compId,
                            @PathVariable int eventId) {
        log.trace("Получен DELETE-запрос на удаление события ID {} из подборки ID {}.", eventId, compId);
        service.removeEvent(compId, eventId);
    }

    // Добавление события в подборку
    @PatchMapping("/{compId}/events/{eventId}")
    public void addEvent(@PathVariable int compId,
                         @PathVariable int eventId) {
        log.trace("Получен PATCH-запрос на добавление события ID {} в подборку ID {}.", eventId, compId);
        service.addEvent(compId, eventId);
    }

    // Открепить подборку на главной странице
    @DeleteMapping("/{compId}/pin")
    public void removePin(@PathVariable int compId) {
        log.trace("Получен DELETE-запрос на открепление подборки ID {}.", compId);
        service.addRemovePin(compId, false);
    }

    // Закрепить подборку на главной странице
    @PatchMapping("/{compId}/pin")
    public void addPin(@PathVariable int compId) {
        log.trace("Получен PATCH-запрос на закрепление подборки ID {}.", compId);
        service.addRemovePin(compId, true);
    }
}
