package ru.practicum.ewm.model.compilation;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@NoArgsConstructor
public class NewCompilationDto {
    @NotNull
    String title;
    int[] events;
    Boolean pinned;
}
