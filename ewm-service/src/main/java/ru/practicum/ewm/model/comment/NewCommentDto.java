package ru.practicum.ewm.model.comment;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

@Getter
@Setter
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public final class NewCommentDto {
    @Positive
    long eventId;
    @NotBlank
    @Length(min = 3, max = 2000)
    String text;
    @NotNull
    String estimation;
}