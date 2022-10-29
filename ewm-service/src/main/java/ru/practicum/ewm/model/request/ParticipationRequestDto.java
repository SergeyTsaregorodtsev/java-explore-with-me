package ru.practicum.ewm.model.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ParticipationRequestDto {
    int id;
    int event;
    int requester;
    String created;
    String status;
}
