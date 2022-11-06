package ru.practicum.ewm.model.event;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.ewm.model.Location;
import ru.practicum.ewm.model.category.Category;
import ru.practicum.ewm.model.category.CategoryMapper;
import ru.practicum.ewm.model.user.User;
import ru.practicum.ewm.model.user.UserMapper;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class EventMapper {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static EventShortDto toShortDto(Event event, int confirmedRequests, int views) {
        return new EventShortDto(
                event.getId(),
                CategoryMapper.toDto(event.getCategory()),
                event.getTitle(),
                event.getAnnotation(),
                UserMapper.toShortDto(event.getInitiator()),
                confirmedRequests,
                event.getEventDate().format(formatter),
                event.isPaid(),
                views
        );
    }

    public static EventFullDto toFullDto(Event event, int confirmedRequests, int views) {
        return new EventFullDto(
                event.getId(),
                CategoryMapper.toDto(event.getCategory()),
                event.getTitle(),
                event.getAnnotation(),
                event.getDescription(),
                UserMapper.toShortDto(event.getInitiator()),
                confirmedRequests,
                event.getEventDate().format(formatter),
                event.getCreatedOn().format(formatter),
                (event.getState() == Event.State.PUBLISHED) ? event.getPublishedOn().format(formatter) : null,
                new Location(event.getLocationLat(), event.getLocationLon()),
                event.isPaid(),
                event.isRequestModeration(),
                event.getParticipantLimit(),
                event.getState(),
                views
        );
    }

    public static Event toEvent(NewEventDto dto, User user, Category category, LocalDateTime createdOn) {
        return new Event(
                category,
                dto.getTitle(),
                dto.getAnnotation(),
                dto.getDescription(),
                user,
                LocalDateTime.parse(dto.getEventDate(), formatter),
                createdOn,
                dto.getLocation().getLat(),
                dto.getLocation().getLon(),
                dto.getPaid() != null ? dto.getPaid() : false,
                dto.getRequestModeration() != null ? dto.getRequestModeration() : true,
                dto.getParticipantLimit() != null ? dto.getParticipantLimit() : 0,
                Event.State.PENDING
        );
    }
}