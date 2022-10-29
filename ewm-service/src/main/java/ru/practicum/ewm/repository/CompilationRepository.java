package ru.practicum.ewm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.ewm.model.compilation.Compilation;

@Repository
public interface CompilationRepository extends JpaRepository<Compilation, Integer> {
}
