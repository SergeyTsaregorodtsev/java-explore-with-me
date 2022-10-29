package ru.practicum.ewm.service.priv;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.common.ForbiddenRequestException;
import ru.practicum.ewm.model.category.Category;
import ru.practicum.ewm.model.event.*;
import ru.practicum.ewm.model.request.ParticipationRequest;
import ru.practicum.ewm.model.request.ParticipationRequestDto;
import ru.practicum.ewm.model.request.ParticipationRequestMapper;
import ru.practicum.ewm.model.user.User;
import ru.practicum.ewm.repository.CategoryRepository;
import ru.practicum.ewm.repository.EventRepository;
import ru.practicum.ewm.repository.RequestRepository;
import ru.practicum.ewm.repository.UserRepository;
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
public class PrivateEventService {
    private final EventRepository eventRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final RequestRepository requestRepository;
    static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // Получение событий, добавленных текущим пользователем
    public List<EventShortDto> getEvents(int userId, int from, int size) {
        Sort sortById = Sort.by(Sort.Direction.ASC, "id");
        Pageable page = PageRequest.of(from / size, size, sortById);
        Page<Event> eventPage = eventRepository.findAllByInitiator_Id(userId, page);
        List<EventShortDto> result = new ArrayList<>();
        for (Event event : eventPage.getContent()) {
            result.add(EventMapper.toShortDto(event, StatClient.getViews(event.getId())));
        }
        log.trace("Получены {} событий для пользователя ID {}.", result.size(), userId);
        return result;
    }

    // Изменение события добавленного текущим пользователем
    public EventFullDto updateEvent(int userId, UpdateEventRequest request) {
        Event event = getEvent(request.getEventId());

        if (event.getState() != Event.State.PENDING && event.getState() != Event.State.CANCELED) {
            throw new ForbiddenRequestException("Only pending or canceled events can be changed");
        }

        User initiator = event.getInitiator();
        if (initiator.getId() != userId) {
            throw new ForbiddenRequestException("User ID " + userId + "not corresponding to requested event initiator.");
        }

        Integer categoryId = request.getCategory();
        if (categoryId != null) {
            Optional<Category> category = categoryRepository.findById(categoryId);
            if (category.isEmpty()) {
                throw new EntityNotFoundException("Category with requested ID not found.");
            }
            event.setCategory(category.get());
        }
        String title = request.getTitle();
        if (title != null) {
            event.setTitle(title);
        }
        String annotation = request.getAnnotation();
        if (annotation != null) {
            event.setAnnotation(annotation);
        }
        String description = request.getDescription();
        if (description != null) {
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
        if (event.getState() == Event.State.CANCELED) {
            event.setState(Event.State.PENDING);
        }
        eventRepository.save(event);

        log.trace("Event ID {} updated.", event.getId());
        return EventMapper.toFullDto(event, StatClient.getViews(event.getId()));
    }

    // Добавление нового события
    public EventFullDto addEvent(int userId, NewEventDto eventDto) {
        Optional<Category> category = categoryRepository.findById(eventDto.getCategory());
        if (category.isEmpty()) {
            throw new EntityNotFoundException("Category with requested ID not found.");
        }
        Optional<User> user = userRepository.findById(userId);

        if (user.isEmpty()) {
            throw new EntityNotFoundException("User with requested ID not found.");
        }

        LocalDateTime createdOn = LocalDateTime.now();
        LocalDateTime eventDate = LocalDateTime.parse(eventDto.getEventDate(), formatter);
        if (eventDate.minusHours(2).isBefore(createdOn)) {
            throw new ForbiddenRequestException("Событие не может быть раньше, чем через 2 часа от текущего момента");
        }

        Event event = eventRepository.save(EventMapper.toEvent(eventDto, user.get(), category.get(), createdOn));
        log.trace("Добавлено событие {}, ID {}.", event.getTitle(), event.getId());
        return EventMapper.toFullDto(event, 0);
    }

    // Получение полной информации о событии добавленном текущим пользователем
    public EventFullDto getEvent(int userId, int eventId) {
        Event event = getEvent(eventId);
        if (event.getInitiator().getId() != userId) {
            throw new ForbiddenRequestException("User ID " + userId + "not corresponding to requested event initiator.");
        }
        log.trace("Получено событие ID {} для пользователя ID {}.", eventId, userId);
        return EventMapper.toFullDto(event, StatClient.getViews(eventId));
    }

    // Отмена события добавленного текущим пользователем
    public EventFullDto cancelEvent(int userId, int eventId) {
        Event event = getEvent(eventId);
        if (event.getInitiator().getId() != userId) {
            throw new ForbiddenRequestException("User ID " + userId + "not corresponding to requested event initiator.");
        }
        event.setState(Event.State.CANCELED);
        log.trace("Отмена события ID {} пользователем ID {}.", eventId, userId);
        return EventMapper.toFullDto(eventRepository.save(event), StatClient.getViews(eventId));
    }

    // Получение информации о запросах на участие в событии текущего пользователя
    public List<ParticipationRequestDto> getRequests(int userId, int eventId) {
        Event event = getEvent(eventId);
        if (event.getInitiator().getId() != userId) {
            throw new ForbiddenRequestException("User ID " + userId + "not corresponding to requested event initiator.");
        }
        List<ParticipationRequest> requests = requestRepository.findAllByEvent_Id(eventId);
        List<ParticipationRequestDto> result = new ArrayList<>();
        for (ParticipationRequest request : requests) {
            result.add(ParticipationRequestMapper.toDto(request));
        }
        return result;
    }

    // Подтверждение-отклонение чужой заявки на участие в событии текущего пользователя
    public ParticipationRequestDto confirmRequest(int userId, int eventId, int reqId, boolean accept) {
        Event event = getEvent(eventId);
        if (event.getInitiator().getId() != userId) {
            throw new ForbiddenRequestException("User ID " + userId + "not corresponding to requested event initiator.");
        }
        Optional<ParticipationRequest> request = requestRepository.findById(reqId);
        if (request.isEmpty()) {
            throw new EntityNotFoundException("Request with requested ID not found.");
        }
        ParticipationRequest currentRequest = request.get();
        if (accept) {
            currentRequest.setStatus(ParticipationRequest.Status.CONFIRMED);
        } else {
            currentRequest.setStatus(ParticipationRequest.Status.REJECTED);
        }
        return ParticipationRequestMapper.toDto(requestRepository.save(currentRequest));
    }

    private Event getEvent(int eventId) {
        Optional<Event> event = eventRepository.findById(eventId);
        if (event.isEmpty()) {
            throw new EntityNotFoundException("Event with requested ID not found.");
        }
        return event.get();
    }
}