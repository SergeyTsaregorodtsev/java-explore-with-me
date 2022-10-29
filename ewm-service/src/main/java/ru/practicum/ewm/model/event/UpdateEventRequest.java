package ru.practicum.ewm.model.event;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateEventRequest {
    int eventId;
    Integer category;
    String title;
    String annotation;
    String description;
    String eventDate; // Дата и время на которые намечено событие (в формате "yyyy-MM-dd HH:mm:ss")
    Boolean paid;
    Integer participantLimit; // Ограничение на количество участников (0 - отсутствие ограничения)
}
