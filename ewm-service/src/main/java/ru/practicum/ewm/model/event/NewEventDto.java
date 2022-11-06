package ru.practicum.ewm.model.event;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.validator.constraints.Length;
import ru.practicum.ewm.model.Location;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NewEventDto {
    @Positive
    long category;
    @NotBlank
    @Length(min = 3, max = 120)
    String title;
    @NotBlank
    @Length(min = 20, max = 2000)
    String annotation;
    @NotBlank
    @Length(min = 20, max = 7000)
    String description;
    @NotBlank
    String eventDate;
    @NotNull
    Location location;
    Boolean paid;
    Boolean requestModeration;
    Integer participantLimit;
}