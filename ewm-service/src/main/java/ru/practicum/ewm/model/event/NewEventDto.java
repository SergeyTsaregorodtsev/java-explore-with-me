package ru.practicum.ewm.model.event;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.validator.constraints.Length;
import ru.practicum.ewm.model.Location;

import javax.validation.constraints.NotNull;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NewEventDto {
    @NotNull
    int category;
    @NotNull
    @Length(min = 3, max = 120)
    String title;
    @NotNull
    @Length(min = 20, max = 2000)
    String annotation;
    @NotNull
    @Length(min = 20, max = 7000)
    String description;
    @NotNull
    String eventDate;
    @NotNull
    Location location;
    Boolean paid;
    Boolean requestModeration;
    Integer participantLimit;
}