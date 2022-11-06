package ru.practicum.ewm.service.adm;

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
import ru.practicum.ewm.model.Location;
import ru.practicum.ewm.model.category.Category;
import ru.practicum.ewm.model.event.*;
import ru.practicum.ewm.repository.CategoryRepository;
import ru.practicum.ewm.repository.EventRepository;
import ru.practicum.ewm.repository.RequestRepository;
import ru.practicum.ewm.statclient.StatClient;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminEventService {
    EventRepository eventRepository;
    CategoryRepository categoryRepository;
    RequestRepository requestRepository;
    StatClient client;
    static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public List<EventFullDto> getEvents(EventAdminFilter filter,
                                        int from,
                                        int size) {
        Sort sortById = Sort.by(Sort.Direction.ASC, "id");
        Pageable page = PageRequest.of(from / size, size, sortById);
        Iterable<Event> events = eventRepository.findAll(formatExpression(filter), page);
        List<EventFullDto> result = new ArrayList<>();

        // Получаем информацию по просмотрам
        List<Long> idsForViews = new ArrayList<>();
        events.forEach((Event e) -> idsForViews.add(e.getId()));
        Map<Long, Integer> views = client.getViews(idsForViews);

        for (Event event : events) {
            result.add(EventMapper.toFullDto(
                    event,
                    requestRepository.getConfirmedRequestsAmount(event.getId()),
                    views.get(event.getId())));
        }
        log.trace("По запросу получены {} событий.", result.size());
        return result;
    }

    public EventFullDto updateEvent(long eventId, AdminUpdateEventRequest request) {
        Event event = eventRepository.findById(eventId).orElseThrow(() -> {
            throw new EntityNotFoundException("Event with requested ID not found.");
        });
        Long categoryId = request.getCategory();
        if (categoryId != null) {
            Category category = categoryRepository.findById(categoryId).orElseThrow(() -> {
                throw new EntityNotFoundException("Category with requested ID not found.");
            });
            event.setCategory(category);
        }
        String title = request.getTitle();
        if (title != null && !title.isBlank()) {
            event.setTitle(title);
        }
        String annotation = request.getAnnotation();
        if (annotation != null && !annotation.isBlank()) {
            event.setAnnotation(annotation);
        }
        String description = request.getDescription();
        if (description != null && !description.isBlank()) {
            event.setDescription(description);
        }
        String eventDate = request.getEventDate();
        if (eventDate != null) {
            event.setEventDate(LocalDateTime.parse(eventDate, formatter));
        }
        Boolean paid = request.getPaid();
        if (paid != null) {
            event.setPaid(paid);
        }
        Integer participantLimit = request.getParticipantLimit();
        if (participantLimit != null) {
            event.setParticipantLimit(participantLimit);
        }
        Boolean requestModeration = request.getRequestModeration();
        if (requestModeration != null) {
            event.setRequestModeration(requestModeration);
        }
        Location location = request.getLocation();
        if (location != null) {
            event.setLocationLat(location.getLat());
            event.setLocationLon(location.getLon());
        }
        eventRepository.save(event);

        log.trace("Event ID {} updated.", event.getId());
        return EventMapper.toFullDto(
                event,
                requestRepository.getConfirmedRequestsAmount(event.getId()),
                client.getViews(event.getId()));
    }

    public EventFullDto publishEvent(long eventId) {
        Event event = eventRepository.findById(eventId).orElseThrow(() -> {
            throw new EntityNotFoundException("Event with requested ID not found.");
        });
        // Дата начала события должна быть не ранее чем за час от даты публикации
        LocalDateTime eventDate = event.getEventDate();
        if (LocalDateTime.now().plusHours(1).isAfter(eventDate)) {
            throw new ForbiddenRequestException("Error: Дата начала события должна быть не ранее чем за час от даты публикации.");
        }
        // Событие должно быть в состоянии ожидания публикации
        if (event.getState() != Event.State.PENDING) {
            throw new ForbiddenRequestException("Error: Событие должно быть в состоянии ожидания публикации.");
        }

        event.setState(Event.State.PUBLISHED);
        event.setPublishedOn(LocalDateTime.now());
        log.trace("Event ID {} published.", eventId);
        return EventMapper.toFullDto(
                eventRepository.save(event),
                requestRepository.getConfirmedRequestsAmount(eventId),
                client.getViews(eventId));
    }

    public EventFullDto rejectEvent(long eventId) {
        Optional<Event> event = eventRepository.findById(eventId);
        if (event.isEmpty()) {
            throw new EntityNotFoundException("Event with requested ID not found.");
        }
        Event rejectedEvent = event.get();
        // Событие не должно быть опубликовано
        if (rejectedEvent.getState() == Event.State.PUBLISHED) {
            throw new ForbiddenRequestException("Error: Событие не должно быть опубликовано.");
        }

        rejectedEvent.setState(Event.State.CANCELED);
        log.trace("Event ID {} cancelled.", eventId);
        return EventMapper.toFullDto(
                eventRepository.save(rejectedEvent),
                requestRepository.getConfirmedRequestsAmount(eventId),
                client.getViews(eventId));
    }

    private BooleanExpression formatExpression(EventAdminFilter filter) {
        BooleanExpression result = null;
        if (filter.getUsers() != null) {
            List<Long> users = new ArrayList<>();
            for (Long i : filter.getUsers()) {
                users.add(i);
            }
            result = QEvent.event.initiator.id.in(users);
        }
        if (filter.getStates() != null) {
            List<Event.State> states = new ArrayList<>();
            for (String state : filter.getStates()) {
                states.add(Event.State.valueOf(state));
            }
            result = (result == null) ? QEvent.event.state.in(states)
                    : result.and(QEvent.event.state.in(states));
        }
        if (filter.getCategories() != null) {
            List<Long> categories = new ArrayList<>();
            for (Long i : filter.getCategories()) {
                categories.add(i);
            }
            result = (result == null) ? QEvent.event.category.id.in(categories)
                    : result.and(QEvent.event.category.id.in(categories));
        }
        if (filter.getRangeStart() != null) {
            result = (result == null) ? QEvent.event.eventDate.after(LocalDateTime.parse(filter.getRangeStart(), formatter))
                    : result.and(QEvent.event.eventDate.after(LocalDateTime.parse(filter.getRangeStart(), formatter)));
        }
        if (filter.getRangeEnd() != null) {
            result = (result == null) ? QEvent.event.eventDate.before(LocalDateTime.parse(filter.getRangeEnd(), formatter))
                    : result.and(QEvent.event.eventDate.after(LocalDateTime.parse(filter.getRangeStart(), formatter)));
        }
        return result;
    }
}