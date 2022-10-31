package ru.practicum.ewm.model.category;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CategoryMapper {

    public static CategoryDto toDto(Category category) {
        return new CategoryDto(
                category.getId(),
                category.getName());
    }

    public static Category toCategory(CategoryDto dto) {
        return new Category(
                dto.getName()
        );
    }
}