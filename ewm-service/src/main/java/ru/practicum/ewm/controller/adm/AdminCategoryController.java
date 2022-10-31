package ru.practicum.ewm.controller.adm;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.model.category.CategoryDto;
import ru.practicum.ewm.service.adm.AdminCategoryService;

import javax.validation.Valid;

@Slf4j
@Validated
@RestController
@RequestMapping(path = "/admin/categories")
@RequiredArgsConstructor
public class AdminCategoryController {
    private final AdminCategoryService service;

    @PostMapping
    public CategoryDto addCategory(@Valid @RequestBody CategoryDto categoryDto) {
        log.trace("Получен POST-запрос на добавление категории {}.", categoryDto.getName());
        return service.addCategory(categoryDto);
    }

    @PatchMapping
    public CategoryDto updateCategory(@Valid @RequestBody CategoryDto categoryDto) {
        log.trace("Получен PATCH-запрос на обновление категории {}.", categoryDto.getName());
        return service.updateCategory(categoryDto);
    }

    @DeleteMapping("/{catId}")
    public void removeCategory(@PathVariable long catId) {
        log.trace("Получен DELETE-запрос на удаление категории ID {}.", catId);
        service.removeCategory(catId);
    }
}