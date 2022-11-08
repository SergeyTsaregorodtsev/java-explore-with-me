package ru.practicum.ewm.model.comment;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import ru.practicum.ewm.model.user.UserShortDto;


@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
public final class CommentDto {
    long id;
    UserShortDto user;
    String created;
    String text;
    String estimation;
}