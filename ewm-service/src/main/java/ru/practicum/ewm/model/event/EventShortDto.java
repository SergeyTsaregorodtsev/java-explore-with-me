package ru.practicum.ewm.model.event;

import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.ewm.model.category.CategoryDto;
import ru.practicum.ewm.model.user.UserShortDto;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EventShortDto {
    int id;
    CategoryDto category;
    String title;
    String annotation;
    UserShortDto initiator;
    int confirmedRequests;
    String eventDate;
    boolean paid;
    int views;
}
