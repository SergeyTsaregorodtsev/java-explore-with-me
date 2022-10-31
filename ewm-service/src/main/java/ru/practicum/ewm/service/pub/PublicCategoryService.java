package ru.practicum.ewm.service.pub;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.model.category.Category;
import ru.practicum.ewm.model.category.CategoryDto;
import ru.practicum.ewm.model.category.CategoryMapper;
import ru.practicum.ewm.repository.CategoryRepository;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PublicCategoryService {
    private final CategoryRepository repository;

    // Получение категорий
    public List<CategoryDto> getCategories(int from, int size) {
        Sort sortingBy = Sort.by(Sort.Direction.ASC, "id");
        Pageable page = PageRequest.of(from / size, size, sortingBy);
        Page<Category> categories = repository.findAll(page);
        List<CategoryDto> result = categories
                .stream()
                .map(CategoryMapper::toDto)
                .collect(Collectors.toList());
        log.trace("По запросу получены {} категорий.", result.size());
        return result;
    }

    // Получение информации о категории по её идентификатору
    public CategoryDto getCategory(long catId) {
        Category category = repository.findById(catId).orElseThrow(() -> {
        throw new EntityNotFoundException("Category with requested ID not found.");
        });
        log.trace("По запросу получена категория ID {}.", catId);
        return CategoryMapper.toDto(category);
    }
}