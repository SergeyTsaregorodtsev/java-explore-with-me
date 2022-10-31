package ru.practicum.ewm.model.compilation;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
@NoArgsConstructor
public class NewCompilationDto {
    @NotBlank
    private String title;
    private long[] events;
    private Boolean pinned;
}