package ru.practicum.ewm.model.event;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Positive;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateEventRequest {
    @Positive
    long eventId;
    Long category;
    @Length(min = 3, max = 120)
    String title;
    @Length(min = 20, max = 2000)
    String annotation;
    @Length(min = 20, max = 7000)
    String description;
    String eventDate; // Дата и время на которые намечено событие (в формате "yyyy-MM-dd HH:mm:ss")
    Boolean paid;
    Integer participantLimit; // Ограничение на количество участников (0 - отсутствие ограничения)
}