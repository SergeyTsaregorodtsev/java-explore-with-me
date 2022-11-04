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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
            result.add(CompilationMapper.toDto(
                    compilation,
                    compilation.getEvents().isEmpty() ? null : getConfirmedRequests(compilation),
                    compilation.getEvents().isEmpty() ? null : getViews(compilation)));
        }
        log.trace("По запросу получены {} подборок.", result.size());
        return result;
    }

    // Получение подробной информации об опубликованном событии по его идентификатору
    public CompilationDto getCompilation(long compId) {
        Compilation compilation = compilationRepository.findById(compId).orElseThrow(() -> {
            throw new EntityNotFoundException("Compilation with requested ID not found.");
        });
        log.trace("По запросу получена подборка ID {}.", compId);
        return CompilationMapper.toDto(
                compilation,
                compilation.getEvents().isEmpty() ? null : getConfirmedRequests(compilation),
                compilation.getEvents().isEmpty() ? null : getViews(compilation));
    }

    private Map<Long, Integer> getViews (Compilation compilation) {
        List<Long> idsForViews = compilation.getEvents()
                .stream()
                .map(Event::getId)
                .collect(Collectors.toList());
        return client.getViews(idsForViews);
    }

    private Map<Long, Integer> getConfirmedRequests (Compilation compilation) {
        Map<Long,Integer> confirmedRequests = new HashMap<>();
        for (Event event : compilation.getEvents()) {
            confirmedRequests.put(event.getId(), requestRepository.getConfirmedRequestsAmount(event.getId()));
        }
        return confirmedRequests;
    }
}