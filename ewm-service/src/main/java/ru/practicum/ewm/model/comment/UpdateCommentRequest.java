package ru.practicum.ewm.model.comment;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor
public class UpdateCommentRequest {
    @Positive
    long commentId;
    @Positive
    long eventId;
    @NotBlank
    @Length(min = 3, max = 2000)
    String text;
    @NotNull
    String estimation;
}