package ru.practicum.ewm.service.adm;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.model.compilation.Compilation;
import ru.practicum.ewm.model.compilation.CompilationDto;
import ru.practicum.ewm.model.compilation.CompilationMapper;
import ru.practicum.ewm.model.compilation.NewCompilationDto;
import ru.practicum.ewm.model.event.Event;
import ru.practicum.ewm.repository.CompilationRepository;
import ru.practicum.ewm.repository.EventRepository;
import ru.practicum.ewm.statclient.StatClient;

import javax.persistence.EntityNotFoundException;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminCompilationService {
    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;

    // Добавление новой подборки
    public CompilationDto addCompilation(NewCompilationDto dto) {
        Set<Event> events = new HashSet<>();
        if (dto.getEvents() != null && dto.getEvents().length > 0) {
            for (int id : dto.getEvents()) {
                Optional<Event> event = eventRepository.findById(id);
                if (event.isPresent()) {
                    events.add(event.get());
                } else {
                    log.trace("Событие ID {} не найдено.", id);
                }
            }
        }
        Compilation compilation = compilationRepository.save(CompilationMapper.toCompilation(dto, events));
        log.trace("Добавлена подборка {}, ID {}. События подборки: {} ", compilation.getTitle(), compilation.getId(), Arrays.toString(dto.getEvents()));
        Map<Integer,Integer> views = new HashMap<>();
        for (Event event : compilation.getEvents()) {
            views.put(event.getId(), StatClient.getViews(event.getId()));
        }
        return CompilationMapper.toDto(compilation, views);
    }

    // Удаление подборки
    public void removeCompilation(int compId) {
        compilationRepository.deleteById(compId);
        log.trace("Удалена пордборка ID {}.", compId);
    }

    // Удаление события из подборки
    public void removeEvent(int compId, int eventId) {
        Optional<Compilation> compilation = compilationRepository.findById(compId);
        if (compilation.isEmpty()) {
            throw new EntityNotFoundException("Подборка ID " + compId + " не найдена.");
        } else {
            Set<Event> events = compilation.get().getEvents();
            Optional<Event> event = eventRepository.findById(eventId);
            if (event.isEmpty() || !events.remove(event.get())) {
                    throw new EntityNotFoundException("Event with id=" + eventId + " was not found.");
            }
        }
        compilationRepository.save(compilation.get());
        log.trace("Удалено событие ID {} из пордборки ID {}.", eventId, compId);
    }

    // Добавление события в подборку
    public void addEvent(int compId, int eventId) {
        Optional<Compilation> compilation = compilationRepository.findById(compId);
        if (compilation.isEmpty()) {
            throw new EntityNotFoundException("Подборка ID " + compId + " не найдена.");
        }
        Set<Event> events = compilation.get().getEvents();
        Optional<Event> event = eventRepository.findById(eventId);
        if (event.isEmpty()) {
            throw new EntityNotFoundException("Event with id=" + eventId + " was not found.");
            }
        events.add(event.get());
        compilationRepository.save(compilation.get());
    }

    // Открепить-закрепить подборку на главной странице
    public void addRemovePin(int compId, boolean pin) {
        Optional<Compilation> compilation = compilationRepository.findById(compId);
        if (compilation.isEmpty()) {
            throw new EntityNotFoundException("Подборка ID " + compId + " не найдена.");
        }
        compilation.get().setPinned(pin);
        compilationRepository.save(compilation.get());
    }
}