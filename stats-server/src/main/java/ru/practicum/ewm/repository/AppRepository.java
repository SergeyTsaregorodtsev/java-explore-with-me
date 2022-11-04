package ru.practicum.ewm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.ewm.model.App;

@Repository
public interface AppRepository extends JpaRepository<App, Long> {

    App findByName(String name);
}