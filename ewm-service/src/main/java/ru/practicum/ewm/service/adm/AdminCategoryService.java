package ru.practicum.ewm.service.adm;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.common.ForbiddenRequestException;
import ru.practicum.ewm.model.category.Category;
import ru.practicum.ewm.model.category.CategoryDto;
import ru.practicum.ewm.model.category.CategoryMapper;
import ru.practicum.ewm.repository.CategoryRepository;
import ru.practicum.ewm.repository.EventRepository;

import javax.persistence.EntityNotFoundException;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminCategoryService {
    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;

    public CategoryDto addCategory(CategoryDto categoryDto) {
        Category category = categoryRepository.save(CategoryMapper.toCategory(categoryDto));
        log.trace("Добавлена категория {}, ID {}.", category.getName(), category.getId());
        return CategoryMapper.toDto(category);
    }

    public CategoryDto updateCategory(CategoryDto categoryDto) {
        Category category = categoryRepository.findById(categoryDto.getId())
                .orElseThrow(() -> {
                    throw new EntityNotFoundException("Category with requested ID not found.");
        });
        String name = categoryDto.getName();
        category.setName(name);
        log.trace("Category ID {} updated.", categoryDto.getId());
        return CategoryMapper.toDto(categoryRepository.save(category));
    }

    public void removeCategory(long catId) {
        // С категорией не должно быть связано ни одного события
        if (eventRepository.getCategoryEventAmount(catId) != 0) {
            throw new ForbiddenRequestException("Error: с категорией не должно быть связано ни одного события.");
        }
        categoryRepository.deleteById(catId);
        log.trace("Category ID {} removed.", catId);
    }
}