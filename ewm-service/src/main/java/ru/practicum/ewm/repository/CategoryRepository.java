package ru.practicum.ewm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.ewm.model.category.Category;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {
}
