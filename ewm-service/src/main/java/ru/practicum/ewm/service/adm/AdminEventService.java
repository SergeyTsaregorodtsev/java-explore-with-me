package ru.practicum.ewm.service.adm;

import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.model.Location;
import ru.practicum.ewm.model.category.Category;
import ru.practicum.ewm.model.event.*;
import ru.practicum.ewm.repository.CategoryRepository;
import ru.practicum.ewm.repository.EventRepository;
import ru.practicum.ewm.statclient.StatClient;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminEventService {
    private final EventRepository eventRepository;
    private final CategoryRepository categoryRepository;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public List<EventFullDto> getEvents(EventAdminFilter filter,
                                        int from,
                                        int size) {
        Sort sortById = Sort.by(Sort.Direction.ASC, "id");
        Pageable page = PageRequest.of(from / size, size, sortById);
        Iterable<Event> events = eventRepository.findAll(formatExpression(filter), page);
        List<EventFullDto> result = new ArrayList<>();
        for (Event event : events) {
            result.add(EventMapper.toFullDto(event, StatClient.getViews(event.getId())));
        }
        log.trace("Позапросу получены {} событий.", result.size());
        return result;
    }

    public EventFullDto updateEvent(int eventId, AdminUpdateEventRequest request) {
        Optional<Event> event = eventRepository.findById(eventId);
        if (event.isEmpty()) {
            throw new EntityNotFoundException("Event with requested ID not found.");
        }
        Event updEvent = event.get();

        Integer categoryId = request.getCategory();
        if (categoryId != null) {
            Optional<Category> category = categoryRepository.findById(categoryId);
            if (category.isEmpty()) {
                throw new EntityNotFoundException("Category with requested ID not found.");
            }
            updEvent.setCategory(category.get());
        }
        String title = request.getTitle();
        if (title != null) {
            updEvent.setTitle(title);
        }
        String annotation = request.getAnnotation();
        if (annotation != null) {
            updEvent.setAnnotation(annotation);
        }
        String description = request.getDescription();
        if (description != null) {
            updEvent.setDescription(description);
        }
        String eventDate = request.getEventDate();
        if (eventDate != null) {
            updEvent.setEventDate(LocalDateTime.parse(eventDate, formatter));
        }
        Boolean paid = request.getPaid();
        if (paid != null) {
            updEvent.setPaid(paid);
        }
        Integer participantLimit = request.getParticipantLimit();
        if (participantLimit != null) {
            updEvent.setParticipantLimit(participantLimit);
        }
        Boolean requestModeration = request.getRequestModeration();
        if (requestModeration != null) {
            updEvent.setRequestModeration(requestModeration);
        }
        Location location = request.getLocation();
        if (location != null) {
            updEvent.setLocationLat(location.getLat());
            updEvent.setLocationLon(location.getLon());
        }
        eventRepository.save(updEvent);

        log.trace("Event ID {} updated.", updEvent.getId());
        return EventMapper.toFullDto(updEvent, StatClient.getViews(updEvent.getId()));
    }

    public EventFullDto publishEvent(int eventId) {
        Optional<Event> event = eventRepository.findById(eventId);
        if (event.isEmpty()) {
            throw new EntityNotFoundException("Event with requested ID not found.");
        }
        Event pubEvent = event.get();
        pubEvent.setState(Event.State.PUBLISHED);
        pubEvent.setPublishedOn(LocalDateTime.now());
        log.trace("Event ID {} published.", eventId);
        return EventMapper.toFullDto(eventRepository.save(pubEvent), StatClient.getViews(eventId));
    }

    public EventFullDto rejectEvent(int eventId) {
        Optional<Event> event = eventRepository.findById(eventId);
        if (event.isEmpty()) {
            throw new EntityNotFoundException("Event with requested ID not found.");
        }
        Event pubEvent = event.get();
        pubEvent.setState(Event.State.CANCELED);
        log.trace("Event ID {} cancelled.", eventId);
        return EventMapper.toFullDto(eventRepository.save(pubEvent), StatClient.getViews(eventId));
    }

    private BooleanExpression formatExpression(EventAdminFilter filter) {
        BooleanExpression result = null;
        if (filter.getUsers() != null) {
            List<Integer> users = new ArrayList<>();
            for (Integer i : filter.getUsers()) {
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
            List<Integer> categories = new ArrayList<>();
            for (Integer i : filter.getCategories()) {
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