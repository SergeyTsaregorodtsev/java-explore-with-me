package ru.practicum.ewm.service.pub;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.model.compilation.Compilation;
import ru.practicum.ewm.model.compilation.CompilationDto;
import ru.practicum.ewm.model.compilation.CompilationMapper;
import ru.practicum.ewm.model.event.Event;
import ru.practicum.ewm.repository.CompilationRepository;
import ru.practicum.ewm.statclient.StatClient;

import javax.persistence.EntityNotFoundException;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class PublicCompilationService {
    private final CompilationRepository repository;

    // Получение подборок событий
    public List<CompilationDto> getCompilations() {
        List<CompilationDto> result = new ArrayList<>();
        for (Compilation compilation : repository.findAll()) {
            Map<Integer,Integer> views = new HashMap<>();
            for (Event event : compilation.getEvents()) {
                views.put(event.getId(), StatClient.getViews(event.getId()));
            }
            result.add(CompilationMapper.toDto(compilation, views));
        }
        log.trace("Позапросу получены {} подборок.", result.size());
        return result;
    }

    // Получение подробной информации об опубликованном событии по его идентификатору
    public CompilationDto getCompilation(int compId) {
        Optional<Compilation> compilation = repository.findById(compId);
        if (compilation.isEmpty()) {
            throw new EntityNotFoundException("Compilation with requested ID not found.");
        }
        log.trace("По запросу получена подборка ID {}.", compId);
        Map<Integer,Integer> views = new HashMap<>();
        for (Event event : compilation.get().getEvents()) {
            views.put(event.getId(), StatClient.getViews(event.getId()));
        }
        return CompilationMapper.toDto(compilation.get(), views);
    }
}