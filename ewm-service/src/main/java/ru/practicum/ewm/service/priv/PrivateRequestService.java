package ru.practicum.ewm.service.priv;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.common.ForbiddenRequestException;
import ru.practicum.ewm.model.event.Event;
import ru.practicum.ewm.model.request.ParticipationRequest;
import ru.practicum.ewm.model.request.ParticipationRequestDto;
import ru.practicum.ewm.model.request.ParticipationRequestMapper;
import ru.practicum.ewm.model.user.User;
import ru.practicum.ewm.repository.EventRepository;
import ru.practicum.ewm.repository.RequestRepository;
import ru.practicum.ewm.repository.UserRepository;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PrivateRequestService {
    RequestRepository requestRepository;
    UserRepository userRepository;
    EventRepository eventRepository;

    // Получение информации о заявках текущего пользователя на участие в чужих событиях
    public List<ParticipationRequestDto> getRequests(int userId) {
        List<ParticipationRequest> requests = requestRepository.findAllByRequesterId(userId);
        List<ParticipationRequestDto> result = new ArrayList<>();
        for (ParticipationRequest request : requests) {
            result.add(ParticipationRequestMapper.toDto(request));
        }
        log.trace("Получены {} заявок для пользователя ID {}.", result.size(), userId);
        return result;
    }

    // Добавление запроса от текущего пользователя на участие в событии
    public ParticipationRequestDto addRequest(int userId, int eventId) {
        Optional<User> user = userRepository.findById(userId);
        Optional<Event> event = eventRepository.findById(eventId);
        if (user.isEmpty()) {
            throw new EntityNotFoundException("User with requested ID not found.");
        }
        if (event.isEmpty()) {
            throw new EntityNotFoundException("Event with requested ID not found.");
        }
        // Нельзя добавить повторный запрос
        if (requestRepository.findByEvent_IdAndRequester_Id(eventId, userId) != null) {
            throw new ForbiddenRequestException("Request already exists.");
        }
        // Инициатор события не может добавить запрос на участие в своём событии
        if (event.get().getInitiator().getId() == userId) {
            throw new ForbiddenRequestException("Participation request to own event.");
        }
        // Нельзя участвовать в неопубликованном событии
        if (event.get().getState() != Event.State.PUBLISHED) {
            throw new ForbiddenRequestException("Event is not published yet.");
        }
        // Если у события достигнут лимит запросов на участие - необходимо вернуть ошибку
        int confirmedRequests = requestRepository.getConfirmedRequestsAmount(eventId);
        if (confirmedRequests == event.get().getParticipantLimit()) {
            throw new ForbiddenRequestException("Participant limit reached.");
        }
        // Если для события отключена пре-модерация запросов на участие, то запрос должен перейти в состояние подтвержденного
        ParticipationRequest.Status status = (event.get().getParticipantLimit() == 0 || !event.get().isRequestModeration()) ?
                ParticipationRequest.Status.CONFIRMED :
                ParticipationRequest.Status.PENDING;
        ParticipationRequest request = new ParticipationRequest(
                event.get(),
                user.get(),
                LocalDateTime.now(),
                status);
        ParticipationRequest newRequest = requestRepository.save(request);
        log.trace("Добавлена заявка ID {} от пользователя ID {} на событие ID {}.", newRequest.getId(), userId, eventId);
        return ParticipationRequestMapper.toDto(newRequest);
    }

    // Отмена своего запроса на участие в событии
    public ParticipationRequestDto cancelRequest(int userId, int requestId) {
        Optional<ParticipationRequest> request = requestRepository.findById(requestId);
        if (request.isEmpty()) {
            throw new EntityNotFoundException("Request with requested ID not found.");
        }
        request.get().setStatus(ParticipationRequest.Status.CANCELED);
        log.trace("Отмена заявки ID {} от пользователя ID {}, статус - {}.", requestId, userId, request.get().getStatus());
        return ParticipationRequestMapper.toDto(requestRepository.save(request.get()));
    }
}