package ru.practicum.ewm.model.event;

import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.ewm.model.Location;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AdminUpdateEventRequest {
    Long category;
    String title;
    String annotation;
    String description;
    String eventDate; // Дата и время на которые намечено событие (в формате "yyyy-MM-dd HH:mm:ss")
    Location location;
    Boolean paid;
    Boolean requestModeration; // Нужна ли пре-модерация заявок на участие
    Integer participantLimit; // Ограничение на количество участников (0 - отсутствие ограничения)
}
