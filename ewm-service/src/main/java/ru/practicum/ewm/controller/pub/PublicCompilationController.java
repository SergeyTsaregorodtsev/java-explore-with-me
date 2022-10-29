package ru.practicum.ewm.controller.pub;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewm.model.compilation.CompilationDto;
import ru.practicum.ewm.service.pub.PublicCompilationService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(path = "/compilations")
@RequiredArgsConstructor
public class PublicCompilationController {
    private final PublicCompilationService service;

    // Получение подборок событий
    @GetMapping
    public List<CompilationDto> getCompilations() {
        log.trace("Получен GET-запрос на подборки.");
        return service.getCompilations();
    }

    //Получение подборки событий по его id
    @GetMapping("/{compId}")
    public CompilationDto getCompilation(@PathVariable int compId) {
        log.trace("Получен GET-запрос на подборку ID {}.", compId);
        return service.getCompilation(compId);
    }
}