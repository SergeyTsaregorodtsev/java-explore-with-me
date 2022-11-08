package ru.practicum.ewm.model.event;

import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.ewm.model.Location;
import ru.practicum.ewm.model.category.CategoryDto;
import ru.practicum.ewm.model.comment.CommentDto;
import ru.practicum.ewm.model.user.UserShortDto;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EventFullDto {
    long id;
    CategoryDto category;
    String title;
    String annotation;
    String description;
    UserShortDto initiator;
    int confirmedRequests;
    String eventDate;
    String createdOn;
    String publishedOn;
    Location location;
    boolean paid;
    boolean requestModeration;
    int participantLimit;
    Event.State state;
    int views;
    List<CommentDto> comments;
}