package ru.practicum.ewm.service.pub;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.model.compilation.Compilation;
import ru.practicum.ewm.model.compilation.CompilationDto;
import ru.practicum.ewm.model.compilation.CompilationMapper;
import ru.practicum.ewm.model.event.Event;
import ru.practicum.ewm.repository.CompilationRepository;
import ru.practicum.ewm.repository.RequestRepository;
import ru.practicum.ewm.statclient.StatClient;

import javax.persistence.EntityNotFoundException;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PublicCompilationService {
    CompilationRepository compilationRepository;
    RequestRepository requestRepository;
    StatClient client;

    // Получение подборок событий
    public List<CompilationDto> getCompilations() {
        List<CompilationDto> result = new ArrayList<>();
        for (Compilation compilation : compilationRepository.findAll()) {
            Map<Long,Integer> views = new HashMap<>();
            Map<Long,Integer> confirmedRequests = new HashMap<>();
            for (Event event : compilation.getEvents()) {
                views.put(event.getId(), client.getViews(event.getId()));
                confirmedRequests.put(event.getId(), requestRepository.getConfirmedRequestsAmount(event.getId()));
            }
            result.add(CompilationMapper.toDto(compilation, confirmedRequests, views));
        }
        log.trace("Позапросу получены {} подборок.", result.size());
        return result;
    }

    // Получение подробной информации об опубликованном событии по его идентификатору
    public CompilationDto getCompilation(long compId) {
        Optional<Compilation> compilation = compilationRepository.findById(compId);
        if (compilation.isEmpty()) {
            throw new EntityNotFoundException("Compilation with requested ID not found.");
        }
        log.trace("По запросу получена подборка ID {}.", compId);
        Map<Long,Integer> views = new HashMap<>();
        Map<Long,Integer> confirmedRequests = new HashMap<>();
        for (Event event : compilation.get().getEvents()) {
            views.put(event.getId(), client.getViews(event.getId()));
            confirmedRequests.put(event.getId(), requestRepository.getConfirmedRequestsAmount(event.getId()));
        }
        return CompilationMapper.toDto(compilation.get(), confirmedRequests, views);
    }
}