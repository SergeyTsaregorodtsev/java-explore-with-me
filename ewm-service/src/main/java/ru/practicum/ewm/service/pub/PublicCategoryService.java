package ru.practicum.ewm.service.pub;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.model.category.*;
import ru.practicum.ewm.repository.CategoryRepository;

import javax.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PublicCategoryService {
    private final CategoryRepository repository;

    // Получение категорий
    public List<CategoryDto> getCategories() {
        List<CategoryDto> result = new ArrayList<>();
        for (Category category : repository.findAll()) {
            result.add(CategoryMapper.toDto(category));
        }
        log.trace("По запросу получены {} категорий.", result.size());
        return result;
    }

    // Получение информации о категории по её идентификатору
    public CategoryDto getCategory(int catId) {
        Optional<Category> category = repository.findById(catId);
        if (category.isEmpty()) {
            throw new EntityNotFoundException("Category with requested ID not found.");
        }
        log.trace("По запросу получена категория ID {}.", catId);
        return CategoryMapper.toDto(category.get());
    }
}