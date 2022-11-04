package ru.practicum.ewm.service.priv;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
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
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PrivateEventService {
    EventRepository eventRepository;
    CategoryRepository categoryRepository;
    UserRepository userRepository;
    RequestRepository requestRepository;
    StatClient client;
    static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // Получение событий, добавленных текущим пользователем
    public List<EventShortDto> getEvents(long userId, int from, int size) {
        Sort sortById = Sort.by(Sort.Direction.ASC, "id");
        Pageable page = PageRequest.of(from / size, size, sortById);
        Page<Event> eventPage = eventRepository.findAllByInitiator_Id(userId, page);
        List<EventShortDto> result = new ArrayList<>();

        // Получаем информацию по просмотрам
        List<Long> idsForViews = new ArrayList<>();
        eventPage.getContent().forEach((Event e) -> idsForViews.add(e.getId()));
        Map<Long, Integer> views = client.getViews(idsForViews);

        for (Event event : eventPage.getContent()) {
            result.add(EventMapper.toShortDto(
                    event,
                    requestRepository.getConfirmedRequestsAmount(event.getId()),
                    views.get(event.getId())));
        }
        log.trace("Получены {} событий для пользователя ID {}.", result.size(), userId);
        return result;
    }

    // Изменение события добавленного текущим пользователем
    public EventFullDto updateEvent(long userId, UpdateEventRequest request) {
        Event event = getEvent(request.getEventId());

        // Изменить можно только отмененные события или события в состоянии ожидания модерации
        if (event.getState() != Event.State.PENDING && event.getState() != Event.State.CANCELED) {
            throw new ForbiddenRequestException("Only pending or canceled events can be changed");
        }

        User initiator = event.getInitiator();
        if (initiator.getId() != userId) {
            throw new ForbiddenRequestException("User ID " + userId + "not corresponding to requested event initiator.");
        }

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
            LocalDateTime newDate = LocalDateTime.parse(eventDate, formatter);
            // Дата и время на которые намечено событие не может быть раньше, чем через два часа от текущего момента
            if (newDate.isBefore(LocalDateTime.now().plusHours(2))) {
                throw new ForbiddenRequestException("Invalid new date and time for event: must be later than " +
                        LocalDateTime.now().plusHours(2).format(formatter));
            }
            event.setEventDate(newDate);
        }
        Boolean paid = request.getPaid();
        if (paid != null) {
            event.setPaid(paid);
        }
        Integer participantLimit = request.getParticipantLimit();
        if (participantLimit != null) {
            event.setParticipantLimit(participantLimit);
        }
        // Отменённое событие автоматически переходит в состояние ожидания модерации
        if (event.getState() == Event.State.CANCELED) {
            event.setState(Event.State.PENDING);
        }
        eventRepository.save(event);

        log.trace("Event ID {} updated.", event.getId());
        return EventMapper.toFullDto(
                event,
                requestRepository.getConfirmedRequestsAmount(event.getId()),
                client.getViews(event.getId()));
    }

    // Добавление нового события
    public EventFullDto addEvent(long userId, NewEventDto eventDto) {
        Category category = categoryRepository.findById(eventDto.getCategory()).orElseThrow(() -> {
            throw new EntityNotFoundException("Category with requested ID not found.");
        });
        User user = userRepository.findById(userId).orElseThrow(() -> {
            throw new EntityNotFoundException("User with requested ID not found.");
        });
        // Дата и время на которые намечено событие не может быть раньше, чем через два часа от текущего момента
        LocalDateTime createdOn = LocalDateTime.now();
        LocalDateTime eventDate = LocalDateTime.parse(eventDto.getEventDate(), formatter);
        if (eventDate.minusHours(2).isBefore(createdOn)) {
            throw new ForbiddenRequestException("Событие не может быть раньше, чем через 2 часа от текущего момента");
        }

        Event event = eventRepository.save(EventMapper.toEvent(eventDto, user, category, createdOn));
        log.trace("Добавлено событие {}, ID {}.", event.getTitle(), event.getId());
        return EventMapper.toFullDto(event, 0,0);
    }

    // Получение полной информации о событии добавленном текущим пользователем
    public EventFullDto getEvent(long userId, long eventId) {
        Event event = getEvent(eventId);
        if (event.getInitiator().getId() != userId) {
            throw new ForbiddenRequestException("User ID " + userId + "not corresponding to requested event initiator.");
        }
        log.trace("Получено событие ID {} для пользователя ID {}.", eventId, userId);
        return EventMapper.toFullDto(
                event,
                requestRepository.getConfirmedRequestsAmount(event.getId()),
                client.getViews(eventId));
    }

    // Отмена события добавленного текущим пользователем
    public EventFullDto cancelEvent(long userId, long eventId) {
        Event event = getEvent(eventId);
        if (event.getInitiator().getId() != userId) {
            throw new ForbiddenRequestException("User ID " + userId + "not corresponding to requested event initiator.");
        }
        // Отменить можно только событие в состоянии ожидания модерации
        if (event.getState() != Event.State.PENDING) {
            throw new ForbiddenRequestException("Error: can't cancel event in " + event.getState() + "state.");
        }
        event.setState(Event.State.CANCELED);
        log.trace("Отмена события ID {} пользователем ID {}.", eventId, userId);
        return EventMapper.toFullDto(
                eventRepository.save(event),
                requestRepository.getConfirmedRequestsAmount(event.getId()),
                client.getViews(eventId));
    }

    // Получение информации о запросах на участие в событии текущего пользователя
    public List<ParticipationRequestDto> getRequests(long userId, long eventId) {
        Event event = getEvent(eventId);
        if (event.getInitiator().getId() != userId) {
            throw new ForbiddenRequestException("User ID " + userId + "not corresponding to requested event initiator.");
        }
        List<ParticipationRequest> requests = requestRepository.findAllByEvent_Id(eventId);
        List<ParticipationRequestDto> result = requests
                .stream()
                .map(ParticipationRequestMapper::toDto)
                .collect(Collectors.toList());
        log.trace("Получено запросов на участие в событии ID {} - {}.", eventId, result.size());
        return result;
    }

    // Подтверждение чужой заявки на участие в событии текущего пользователя
    public ParticipationRequestDto confirmRequest(long userId, long eventId, long reqId) {
        Event event = getEvent(eventId);
        if (event.getInitiator().getId() != userId) {
            throw new ForbiddenRequestException("User ID " + userId + "not corresponding to requested event initiator.");
        }
        ParticipationRequest request = requestRepository.findById(reqId).orElseThrow(() -> {
            throw new EntityNotFoundException("Request with requested ID not found.");
        });
        if (request.getStatus().equals(ParticipationRequest.Status.CONFIRMED)) {
            throw new ForbiddenRequestException("Error: request ID " + reqId + " CONFIRMED already.");
        }
        // Проверка на свободные места (лимит по заявкам - подтверждённые заявки)
        int acceptedRequests = requestRepository.getConfirmedRequestsAmount(eventId);
        int vacancies = event.getParticipantLimit() - acceptedRequests;
        log.trace("Проверка одобренных заявок: {} из {}.", acceptedRequests, event.getParticipantLimit());

        if (vacancies == 0) {
            throw new ForbiddenRequestException("Sorry, participation limit " + event.getParticipantLimit() + " reached.");
        }
        if (vacancies == 1) {
            // Отмена остальных заявок на событие
            List<ParticipationRequest> requests = requestRepository.findAllByEvent_IdAndStatus(
                    eventId,
                    ParticipationRequest.Status.PENDING);
            for (ParticipationRequest req : requests) {
                req.setStatus(ParticipationRequest.Status.REJECTED);
            }
            requestRepository.saveAll(requests);
        }
        request.setStatus(ParticipationRequest.Status.CONFIRMED);

        ParticipationRequest confirmedRequest = requestRepository.save(request);
        log.trace("Заявка {} на событие {} {} ({} из {}).",
                reqId, eventId, confirmedRequest.getStatus(),
                requestRepository.getConfirmedRequestsAmount(eventId), event.getParticipantLimit());
        return ParticipationRequestMapper.toDto(confirmedRequest);
    }

    // Отклонение чужой заявки на участие в событии текущего пользователя
    public ParticipationRequestDto rejectRequest(long userId, long eventId, long reqId) {
        Event event = getEvent(eventId);
        if (event.getInitiator().getId() != userId) {
            throw new ForbiddenRequestException("User ID " + userId + "not corresponding to requested event initiator.");
        }
        ParticipationRequest request = requestRepository.findById(reqId).orElseThrow(() -> {
            throw new EntityNotFoundException("Request with requested ID not found.");
        });
        if (request.getStatus().equals(ParticipationRequest.Status.REJECTED)) {
            throw new ForbiddenRequestException("Error: request ID " + reqId + " REJECTED already.");
        }
        request.setStatus(ParticipationRequest.Status.REJECTED);
        ParticipationRequest confirmedRequest = requestRepository.save(request);
        log.trace("Заявка {} на событие {} {}.", reqId, eventId, confirmedRequest.getStatus());
        return ParticipationRequestMapper.toDto(confirmedRequest);
    }

    private Event getEvent(long eventId) {
        return eventRepository.findById(eventId).orElseThrow(() -> {
            throw new EntityNotFoundException("Event with ID " + eventId + " not found.");
        });
    }
}