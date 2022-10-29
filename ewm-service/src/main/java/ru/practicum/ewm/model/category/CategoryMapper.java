package ru.practicum.ewm.model.category;

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