package ru.practicum.ewm.controller.pub;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.model.category.CategoryDto;
import ru.practicum.ewm.service.pub.PublicCategoryService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(path = "/categories")
@RequiredArgsConstructor
public class PublicCategoryController {
    private final PublicCategoryService service;

    // Получение категорий
    @GetMapping
    public List<CategoryDto> getCategories(@RequestParam(name = "from", defaultValue = "0") int from,
                                           @RequestParam(name = "size", defaultValue = "10") int size) {
        log.trace("Получен GET-запрос на категории, from [{}], size [{}].", from, size);
        return service.getCategories(from, size);
    }

    //Получение информации о категории по её идентификатору
    @GetMapping("/{catId}")
    public CategoryDto getCategory(@PathVariable int catId) {
        log.trace("Получен GET-запрос по категории ID {}.", catId);
        return service.getCategory(catId);
    }
}