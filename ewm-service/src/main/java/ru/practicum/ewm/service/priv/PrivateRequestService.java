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
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PrivateRequestService {
    RequestRepository requestRepository;
    UserRepository userRepository;
    EventRepository eventRepository;

    // Получение информации о заявках текущего пользователя на участие в чужих событиях
    public List<ParticipationRequestDto> getRequests(long userId) {
        List<ParticipationRequest> requests = requestRepository.findAllByRequesterId(userId);
        List<ParticipationRequestDto> result = requests
                .stream()
                .map(ParticipationRequestMapper::toDto)
                .collect(Collectors.toList());
        log.trace("Получены {} заявок для пользователя ID {}.", result.size(), userId);
        return result;
    }

    // Добавление запроса от текущего пользователя на участие в событии
    public ParticipationRequestDto addRequest(long userId, long eventId) {
        User user = userRepository.findById(userId).orElseThrow(() -> {
            throw new EntityNotFoundException("User with requested ID not found.");
        });
        Event event = eventRepository.findById(eventId).orElseThrow(() -> {
            throw new EntityNotFoundException("Event with requested ID not found.");
        });

        // Нельзя добавить повторный запрос
        if (requestRepository.findByEvent_IdAndRequester_Id(eventId, userId) != null) {
            throw new ForbiddenRequestException("Request already exists.");
        }
        // Инициатор события не может добавить запрос на участие в своём событии
        if (event.getInitiator().getId() == userId) {
            throw new ForbiddenRequestException("Participation request to own event.");
        }
        // Нельзя участвовать в неопубликованном событии
        if (event.getState() != Event.State.PUBLISHED) {
            throw new ForbiddenRequestException("Event is not published yet.");
        }
        // Если у события достигнут лимит запросов на участие - необходимо вернуть ошибку
        int confirmedRequests = requestRepository.getConfirmedRequestsAmount(eventId);
        if (confirmedRequests == event.getParticipantLimit()) {
            throw new ForbiddenRequestException("Participant limit reached.");
        }
        // Если для события отключена пре-модерация запросов на участие, то запрос должен перейти в состояние подтвержденного
        ParticipationRequest.Status status = (event.getParticipantLimit() == 0 || !event.isRequestModeration()) ?
                ParticipationRequest.Status.CONFIRMED :
                ParticipationRequest.Status.PENDING;
        ParticipationRequest request = new ParticipationRequest(event, user, LocalDateTime.now(), status);
        ParticipationRequest newRequest = requestRepository.save(request);
        log.trace("Добавлена заявка ID {} от пользователя ID {} на событие ID {}.", newRequest.getId(), userId, eventId);
        return ParticipationRequestMapper.toDto(newRequest);
    }

    // Отмена своего запроса на участие в событии
    public ParticipationRequestDto cancelRequest(long userId, long requestId) {
        ParticipationRequest request = requestRepository.findByIdAndRequester_Id(requestId, userId);
        if (request == null) {
            throw new EntityNotFoundException("Request ID " + requestId + " by requester ID " + userId + " not found.");
        }
        request.setStatus(ParticipationRequest.Status.CANCELED);
        log.trace("Отмена заявки ID {} от пользователя ID {}, статус - {}.", requestId, userId, request.getStatus());
        return ParticipationRequestMapper.toDto(requestRepository.save(request));
    }
}