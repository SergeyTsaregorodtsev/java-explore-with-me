package ru.practicum.ewm.service.adm;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.common.ForbiddenRequestException;
import ru.practicum.ewm.model.compilation.Compilation;
import ru.practicum.ewm.model.compilation.CompilationDto;
import ru.practicum.ewm.model.compilation.CompilationMapper;
import ru.practicum.ewm.model.compilation.NewCompilationDto;
import ru.practicum.ewm.model.event.Event;
import ru.practicum.ewm.repository.CompilationRepository;
import ru.practicum.ewm.repository.EventRepository;
import ru.practicum.ewm.repository.RequestRepository;
import ru.practicum.ewm.statclient.StatClient;

import javax.persistence.EntityNotFoundException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminCompilationService {
    CompilationRepository compilationRepository;
    RequestRepository requestRepository;
    EventRepository eventRepository;
    StatClient client;

    // Добавление новой подборки
    public CompilationDto addCompilation(NewCompilationDto dto) {
        List<Long> ids = LongStream.of(dto.getEvents()).boxed().collect(Collectors.toList());
        Set<Event> events = new HashSet<>(eventRepository.findAllByIdIn(ids));
        Compilation compilation = compilationRepository.save(CompilationMapper.toCompilation(dto, events));
        log.trace("Добавлена подборка {}, ID {}. События подборки: {} ",
                compilation.getTitle(),
                compilation.getId(),
                Arrays.toString(dto.getEvents()));

        Map<Long,Integer> views = new HashMap<>();
        Map<Long,Integer> confirmedRequests = new HashMap<>();
        for (Event event : compilation.getEvents()) {
            views.put(event.getId(), client.getViews(event.getId()));
            confirmedRequests.put(event.getId(), requestRepository.getConfirmedRequestsAmount(event.getId()));
        }
        return CompilationMapper.toDto(compilation, confirmedRequests, views);
    }

    // Удаление подборки
    public void removeCompilation(long compId) {
        compilationRepository.deleteById(compId);
        log.trace("Удалена подборка ID {}.", compId);
    }

    // Удаление события из подборки
    public void removeEvent(long compId, long eventId) {
        Compilation compilation = compilationRepository.findById(compId).orElseThrow(() -> {
            throw new EntityNotFoundException("Подборка ID " + compId + " не найдена.");
        });
        Set<Event> events = compilation.getEvents();
        Optional<Event> event = eventRepository.findById(eventId);
        if (event.isEmpty() || !events.remove(event.get())) {
            throw new EntityNotFoundException("Event with id=" + eventId + " was not found.");
        }
        compilationRepository.save(compilation);
        log.trace("Удалено событие ID {} из подборки ID {}.", eventId, compId);
    }

    // Добавление события в подборку
    public void addEvent(long compId, long eventId) {
        Compilation compilation = compilationRepository.findById(compId).orElseThrow(() -> {
            throw new EntityNotFoundException("Подборка ID " + compId + " не найдена.");
        });
        Set<Event> events = compilation.getEvents();
        Event event = eventRepository.findById(eventId).orElseThrow(() -> {
            throw new EntityNotFoundException("Event with id=" + eventId + " was not found.");
        });
        events.add(event);
        compilationRepository.save(compilation);
    }

    // Открепить подборку на главной странице
    public void removePin(long compId) {
        addRemovePin(compId, false);
    }

    // Закрепить подборку на главной странице
    public void addPin(long compId) {
        addRemovePin(compId, true);
    }

    private void addRemovePin(long compId, boolean pin) {
        Compilation compilation = compilationRepository.findById(compId).orElseThrow(() -> {
            throw new EntityNotFoundException("Подборка ID " + compId + " не найдена.");
        });
        if (((compilation.getPinned()) && pin) || (!(compilation.getPinned()) && !pin)) {
            throw new ForbiddenRequestException("Compilation is already " + ((pin) ? "pinned" : "unpinned"));
        }
        compilation.setPinned(pin);
        compilationRepository.save(compilation);
    }
}