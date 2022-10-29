package ru.practicum.ewm.model.request;

import ru.practicum.ewm.model.event.Event;
import ru.practicum.ewm.model.user.User;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ParticipationRequestMapper {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static ParticipationRequest toRequest(ParticipationRequestDto dto, Event event, User requester) {
        return new ParticipationRequest(
                event,
                requester,
                LocalDateTime.parse(dto.getCreated(), formatter),
                ParticipationRequest.Status.PENDING
        );
    }

    public static ParticipationRequestDto toDto(ParticipationRequest request) {
        return new ParticipationRequestDto(
                request.getId(),
                request.getEvent().getId(),
                request.getRequester().getId(),
                request.getCreated().format(formatter),
                request.getStatus().name());
    }
}