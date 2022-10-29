package ru.practicum.ewm.service.pub;

import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.model.event.*;
import ru.practicum.ewm.repository.EventRepository;
import ru.practicum.ewm.statclient.EndpointHitDto;
import ru.practicum.ewm.statclient.StatClient;
import ru.practicum.ewm.statclient.ViewStats;

import javax.persistence.EntityNotFoundException;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PublicEventService {
    private final EventRepository repository;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // Получение событий с возможностью фильтрации
    public List<EventShortDto> getEvents(EventUserFilter filter, String sort, int from, int size, HttpServletRequest request) {
        Sort sortingBy = Sort.by(Sort.Direction.ASC, "id");
        Pageable page = PageRequest.of(from / size, size, sortingBy);
        Iterable<Event> events = repository.findAll(formatExpression(filter), page);

        // Отправка статистики по запросам событий
        List<String> uris = new ArrayList<>();
        for (Event event : events) {
            String uri = "/events/" + event.getId();
            uris.add(uri);
            StatClient.sendStat(new EndpointHitDto("ewm-main-service",
                    uri,
                    request.getRemoteAddr(),
                    LocalDateTime.now().format(formatter)));
        }

        // Получение данных о просмотрах
        String[] urisArray = uris.toArray(new String[0]);
        List<ViewStats> stats = StatClient.receiveStat(LocalDateTime.now().minusHours(1), LocalDateTime.now(), urisArray, true);

        List<EventShortDto> result = new ArrayList<>();
        int counter = 0;
        for (Event event : events) {
            // Добавление данных о просмотрах события
            int views = stats.get(counter++).getHits();
            result.add(EventMapper.toShortDto(event, views));
        }
        try {
            switch (SortBy.valueOf(sort)) {
                case EVENT_DATE: {
                    result.sort(new EventDateComparator());
                    break;
                }
                case VIEWS: {
                    result.sort(new EventViewsComparator());
                    break;
                }
            }
        } catch (IllegalArgumentException ignore) { }
        log.trace("По запросу получены {} событий.", result.size());
        return result;
    }

    // Получение подробной информации об опубликованном событии по его идентификатору
    public EventFullDto getEvent(int eventId, HttpServletRequest request) {
        Optional<Event> event = repository.findById(eventId);
        if (event.isEmpty()) {
            throw new EntityNotFoundException("Event with requested ID not found.");
        }

        // Отправка статистики по запросу события
        StatClient.sendStat(new EndpointHitDto("ewm-main-service",
                request.getRequestURI(),
                request.getRemoteAddr(),
                LocalDateTime.now().format(formatter)));

        // Получение данных о просмотрах
        String[] uri = new String[]{"/events/" + eventId};
        List<ViewStats> stats = StatClient.receiveStat(LocalDateTime.now().minusHours(1), LocalDateTime.now(), uri, true);
        int views = stats.get(0).getHits();
        return EventMapper.toFullDto(event.get(), views);
    }

    private BooleanExpression formatExpression(EventUserFilter filter) {
        BooleanExpression result = QEvent.event.description.likeIgnoreCase(filter.getText())
                .or(QEvent.event.annotation.likeIgnoreCase(filter.getText()));
        if (filter.getCategories() != null) {
            List<Integer> categories = new ArrayList<>();
            for (Integer i: filter.getCategories()) {
                categories.add(i);
            }
            result = result.and(QEvent.event.category.id.in(categories));
        }
        if (filter.getPaid() != null) {
            result = result.and(QEvent.event.paid.eq(filter.getPaid()));
        }
        if (filter.getRangeStart() != null) {
            result = result.and(QEvent.event.eventDate.after(LocalDateTime.parse(filter.getRangeStart(), formatter)));
        }
        if (filter.getRangeEnd() != null) {
            result = result.and(QEvent.event.eventDate.before(LocalDateTime.parse(filter.getRangeEnd(), formatter)));
        }
        if (filter.isOnlyAvailable()) {
            result = result.and(QEvent.event.confirmedRequests.gt(QEvent.event.participantLimit));
        }
        return result;
    }

    public enum SortBy {
        EVENT_DATE, VIEWS
    }
}