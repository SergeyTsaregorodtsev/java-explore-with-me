package ru.practicum.ewm.model.compilation;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.ewm.model.event.Event;
import ru.practicum.ewm.model.event.EventMapper;
import ru.practicum.ewm.model.event.EventShortDto;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CompilationMapper {

    public static Compilation toCompilation(NewCompilationDto dto, Set<Event> events) {
        return new Compilation(
                dto.getTitle(),
                (dto.getPinned() != null) ? dto.getPinned() : false,
                events
        );
    }

    public static CompilationDto toDto(Compilation compilation,
                                       Map<Long,Integer> confirmedRequests,
                                       Map<Long,Integer> views) {
        List<EventShortDto> events = new ArrayList<>();
        for (Event event : compilation.getEvents()) {
            events.add(EventMapper.toShortDto(event, confirmedRequests.get(event.getId()), views.get(event.getId())));
        }
        return new CompilationDto(
                compilation.getId(),
                compilation.getTitle(),
                events,
                compilation.getPinned()
        );
    }
}