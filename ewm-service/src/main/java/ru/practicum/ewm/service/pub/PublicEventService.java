package ru.practicum.ewm.service.pub;

import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.common.ForbiddenRequestException;
import ru.practicum.ewm.model.event.*;
import ru.practicum.ewm.repository.EventRepository;
import ru.practicum.ewm.repository.RequestRepository;
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
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PublicEventService {
    EventRepository eventRepository;
    RequestRepository requestRepository;
    StatClient client;
    static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // Получение событий с возможностью фильтрации
    public List<EventShortDto> getEvents(EventUserFilter filter, String sort, int from, int size, HttpServletRequest request) {
        Sort sortingBy = Sort.by(Sort.Direction.ASC, "id");
        Pageable page = PageRequest.of(from / size, size, sortingBy);
        Iterable<Event> events = eventRepository.findAll(formatExpression(filter), page);

        // Отправка статистики по запросам событий
        List<String> uris = new ArrayList<>();
        for (Event event : events) {
            String uri = "/events/" + event.getId();
            uris.add(uri);
            client.sendStat(new EndpointHitDto("ewm-main-service",
                    uri,
                    request.getRemoteAddr(),
                    LocalDateTime.now().format(formatter)));
        }

        // Получение данных о просмотрах
        String[] urisArray = uris.toArray(new String[0]);
        List<ViewStats> stats = client.receiveStat(LocalDateTime.now().minusHours(1), LocalDateTime.now(), urisArray, true);

        List<EventShortDto> result = new ArrayList<>();
        int counter = 0;
        for (Event event : events) {
            // Добавление данных о просмотрах события и подтверждённых заявках
            int views = stats.get(counter++).getHits();
            int confirmedRequests = requestRepository.getConfirmedRequestsAmount(event.getId());
            result.add(EventMapper.toShortDto(event, confirmedRequests, views));
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
        Optional<Event> event = eventRepository.findById(eventId);
        if (event.isEmpty()) {
            throw new EntityNotFoundException("Event with requested ID not found.");
        }
        if (event.get().getState() != Event.State.PUBLISHED) {
            throw new ForbiddenRequestException("Event " + eventId + " is not published yet.");
        }

        // Отправка статистики по запросу события
        client.sendStat(new EndpointHitDto("ewm-main-service",
                request.getRequestURI(),
                request.getRemoteAddr(),
                LocalDateTime.now().format(formatter)));

        // Получение данных о просмотрах
        String[] uri = new String[]{"/events/" + eventId};
        List<ViewStats> stats = client.receiveStat(LocalDateTime.now().minusHours(1), LocalDateTime.now(), uri, true);
        int views = stats.get(0).getHits();
        int confirmedRequests = requestRepository.getConfirmedRequestsAmount(eventId);
        return EventMapper.toFullDto(event.get(), confirmedRequests, views);
    }

    private BooleanExpression formatExpression(EventUserFilter filter) {
        // Статус PUBLISHED
        BooleanExpression result = QEvent.event.state.eq(Event.State.PUBLISHED);
        // Совпадение текста в description или в annotation
        result = result.and(QEvent.event.description.likeIgnoreCase(filter.getText())
                .or(QEvent.event.annotation.likeIgnoreCase(filter.getText())));

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
        // Если не указан диапазон дат, выгружаем события, которые произойдут позже текущей даты и времени
        if (filter.getRangeStart() != null && filter.getRangeEnd() != null) {
            result = result.and(QEvent.event.eventDate.after(LocalDateTime.now()));
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