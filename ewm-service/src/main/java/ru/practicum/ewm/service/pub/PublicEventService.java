package ru.practicum.ewm.service.pub;

import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.common.ForbiddenRequestException;
import ru.practicum.ewm.model.comment.CommentMapper;
import ru.practicum.ewm.model.event.*;
import ru.practicum.ewm.model.request.ParticipationRequest;
import ru.practicum.ewm.repository.CommentRepository;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PublicEventService {
    EventRepository eventRepository;
    RequestRepository requestRepository;
    CommentRepository commentRepository;
    StatClient client;
    static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    String appName;

    public PublicEventService(EventRepository eventRepository,
                              RequestRepository requestRepository,
                              CommentRepository commentRepository1,
                              StatClient client,
                              @Value("${appName}") String appName) {
        this.eventRepository = eventRepository;
        this.requestRepository = requestRepository;
        this.commentRepository = commentRepository1;
        this.client = client;
        this.appName = appName;
    }

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
            client.sendStat(new EndpointHitDto(appName,
                    uri,
                    request.getRemoteAddr(),
                    LocalDateTime.now().format(formatter)));
        }

        // Получение данных о просмотрах
        String[] urisArray = uris.toArray(new String[0]);
        List<ViewStats> stats = client.receiveStat(LocalDateTime.now().minusHours(1), LocalDateTime.now(), urisArray, true);

        // Получение данных о подтверждённых заявках
        List<Long> idsForViews = new ArrayList<>();
        events.forEach((Event e) -> idsForViews.add(e.getId()));
        List<ParticipationRequest> requestList = requestRepository.findAllByEvent_IdInAndStatus(idsForViews, ParticipationRequest.Status.CONFIRMED);
        Map<Long, Integer> confirmedRequests = new HashMap<>();
        for (long eventId : idsForViews) {
            long amount = requestList.stream().filter(x -> x.getEvent().getId() == eventId).count();
            confirmedRequests.put(eventId, (int)amount);
        }

        List<EventShortDto> result = new ArrayList<>();
        int counter = 0;
        for (Event event : events) {
            // Добавление данных о просмотрах события и подтверждённых заявках
            int views = stats.get(counter++).getHits();
            result.add(EventMapper.toShortDto(event, confirmedRequests.get(event.getId()), views));
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
    public EventFullDto getEvent(long eventId, HttpServletRequest request) {
        Event event = eventRepository.findById(eventId).orElseThrow(() -> {
            throw new EntityNotFoundException("Event with requested ID not found.");
        });
        if (event.getState() != Event.State.PUBLISHED) {
            throw new ForbiddenRequestException("Event " + eventId + " is not published yet.");
        }

        // Отправка статистики по запросу события
        client.sendStat(new EndpointHitDto(appName,
                request.getRequestURI(),
                request.getRemoteAddr(),
                LocalDateTime.now().format(formatter)));

        // Получение данных о просмотрах
        String[] uri = new String[]{"/events/" + eventId};
        List<ViewStats> stats = client.receiveStat(LocalDateTime.now().minusHours(1), LocalDateTime.now(), uri, true);
        int views = stats.get(0).getHits();
        int confirmedRequests = requestRepository.getConfirmedRequestsAmount(eventId);
        return EventMapper.toFullDto(event, confirmedRequests, views, commentRepository.findAllByEvent_Id(eventId)
                .stream()
                .map(CommentMapper::toDto)
                .collect(Collectors.toList()));
    }

    private BooleanExpression formatExpression(EventUserFilter filter) {
        // Статус PUBLISHED
        BooleanExpression result = QEvent.event.state.eq(Event.State.PUBLISHED);
        // Совпадение текста в description или в annotation
        result = result.and(QEvent.event.description.likeIgnoreCase(filter.getText())
                .or(QEvent.event.annotation.likeIgnoreCase(filter.getText())));

        if (filter.getCategories() != null) {
            List<Long> categories = new ArrayList<>();
            for (Long i: filter.getCategories()) {
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