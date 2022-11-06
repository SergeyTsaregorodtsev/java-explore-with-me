package ru.practicum.ewm.model.category;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
@AllArgsConstructor
public class CategoryDto {
    private long id;
    @NotBlank
    private String name;
}